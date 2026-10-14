package l12_capstones.scheduler.reference

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import l12_capstones.scheduler.Repeat
import l12_capstones.scheduler.SchedulerConfig
import l12_capstones.scheduler.SchedulerFactory
import l12_capstones.scheduler.ShutdownMode
import l12_capstones.scheduler.TaskId
import l12_capstones.scheduler.TaskScheduler
import l12_capstones.scheduler.TaskSpec
import l12_capstones.scheduler.TaskStatus
import l12_capstones.scheduler.isFinal
import java.util.PriorityQueue

/** One finished answer to the scheduler capstone. Try starter/ first! */
object SchedulerReference : SchedulerFactory {
    override fun create(scope: CoroutineScope, config: SchedulerConfig): TaskScheduler =
        ReferenceScheduler(scope, config)
}

/*
 * How it works:
 *  - A task waiting for its delay has a TIMER coroutine. When the timer
 *    fires, the task goes into the READY queue (highest priority first).
 *  - maxConcurrent WORKER coroutines wait on a channel of wake-up tokens.
 *    One token is sent per ready task; a worker takes the best task from
 *    the queue and runs ONE attempt of it.
 *  - After the attempt, the task is either final, or gets a new timer
 *    (retry backoff / next repetition) and the worker is free again.
 *    Workers never sit idle inside a delay.
 *
 * Shared state lives behind one lock and is only touched in short,
 * non-suspending blocks, so the class is safe on any dispatcher. Every
 * decision that reads state and then acts on it (check, then cancel or
 * start a timer) happens inside ONE lock block, so cancel() can never
 * slip in between the check and the action.
 *
 * Trade-off: finished tasks are kept forever (statuses shows "every task
 * ever scheduled"), and each status change copies the map. Fine for a
 * demo or a short-lived batch; a long-running service would prune final
 * tasks after a while.
 */
private class ReferenceScheduler(
    parentScope: CoroutineScope,
    config: SchedulerConfig,
) : TaskScheduler {
    // A SupervisorJob that is a CHILD of the caller's job: one failing
    // task cannot kill the scheduler, and cancelling the caller's scope
    // still stops everything (structured concurrency, l09 lesson 08).
    private val job = SupervisorJob(parentScope.coroutineContext[Job])
    private val scope = CoroutineScope(parentScope.coroutineContext + job)

    private val lock = Any()
    private val entries = mutableMapOf<TaskId, Entry>()
    private val ready = PriorityQueue(compareByDescending<Ready> { it.priority }.thenBy { it.sequence })
    private val wakeUps = Channel<Unit>(Channel.UNLIMITED)
    private var nextId = 1
    private var nextSequence = 0L
    private var accepting = true
    private var stopping = false

    private val mutableStatuses = MutableStateFlow<Map<TaskId, TaskStatus>>(emptyMap())
    override val statuses: StateFlow<Map<TaskId, TaskStatus>> = mutableStatuses.asStateFlow()

    private class Entry(val spec: TaskSpec) {
        var status: TaskStatus = TaskStatus.Pending
        var attempt = 1
        var runsDone = 0
        var timer: Job? = null
        var run: Deferred<Unit>? = null
    }

    private data class Ready(val id: TaskId, val priority: Int, val sequence: Long)

    init {
        repeat(config.maxConcurrent) { scope.launch { workerLoop() } }
    }

    override fun schedule(spec: TaskSpec): TaskId =
        synchronized(lock) {
            check(accepting) { "the scheduler is shutting down; cannot schedule ${spec.name}" }
            val id = TaskId(nextId++)
            entries[id] = Entry(spec)
            publish(id, TaskStatus.Pending)
            queueAfter(id, spec.initialDelayMillis)
            id
        }

    // The lock is reentrant, so shutdown() can call this while holding it.
    override fun cancel(id: TaskId): Boolean =
        synchronized(lock) {
            val entry = entries[id] ?: return false
            if (entry.status.isFinal) return false
            entry.status = TaskStatus.Cancelled
            publish(id, TaskStatus.Cancelled)
            entry.timer?.cancel()
            entry.run?.cancel()
            ready.removeIf { it.id == id }
            true
        }

    override suspend fun shutdown(mode: ShutdownMode) {
        synchronized(lock) {
            accepting = false
            if (mode == ShutdownMode.CANCEL_PENDING) stopping = true
            // Choose AND cancel under one lock: a worker cannot start one of
            // these tasks in between ("let runs in progress finish" holds).
            entries
                .filter { (_, entry) ->
                    !entry.status.isFinal &&
                        when (mode) {
                            ShutdownMode.DRAIN -> false
                            ShutdownMode.CANCEL_PENDING -> entry.run == null
                            ShutdownMode.CANCEL_ALL -> true
                        }
                }.keys
                .toList()
                .forEach(::cancel)
        }
        // Wait until nothing is left to do, THEN stop the workers.
        // A second shutdown() finds everything final and returns at once.
        statuses.first { map -> map.values.all { it.isFinal } }
        job.cancelAndJoin()
    }

    private suspend fun workerLoop() {
        for (token in wakeUps) {
            val id = synchronized(lock) { ready.poll()?.id } ?: continue
            runOneAttempt(id)
        }
    }

    private suspend fun runOneAttempt(id: TaskId) {
        val run =
            synchronized(lock) {
                val entry = entries.getValue(id)
                if (entry.status.isFinal) return
                // async (not launch): a failure stays inside the Deferred
                // and is rethrown by await(), instead of being reported to
                // the scope as an uncaught exception.
                val run = scope.async(start = CoroutineStart.LAZY) { entry.spec.action() }
                entry.run = run
                entry.status = TaskStatus.Running(entry.attempt)
                publish(id, entry.status)
                run
            }
        val failure: Throwable? =
            try {
                run.await()
                null
            } catch (e: CancellationException) {
                // Three possible reasons:
                //  1. the whole scheduler is being cancelled -> ensureActive
                //     rethrows and this worker stops;
                //  2. cancel(id) stopped this task -> it is already final;
                //  3. the task's OWN code threw it (e.g. withTimeout inside
                //     the action) -> that is just a failed attempt.
                currentCoroutineContext().ensureActive()
                if (synchronized(lock) { entries.getValue(id).status.isFinal }) return
                e
            } catch (e: Throwable) {
                // Throwable, not Exception: TODO() or a failed assert throws
                // an Error. It must not kill the worker or leave the task
                // Running forever.
                e
            }
        synchronized(lock) {
            val nextDelay = afterAttempt(id, failure)
            if (nextDelay != null) queueAfter(id, nextDelay)
        }
    }

    /** Decides what happens after one attempt. Returns the delay before the task runs again, or null. */
    private fun afterAttempt(id: TaskId, failure: Throwable?): Long? {
        val entry = entries.getValue(id)
        entry.run = null
        if (entry.status.isFinal) return null // cancelled while running
        val spec = entry.spec

        val (status, nextDelay) =
            if (failure == null) {
                entry.runsDone++
                entry.attempt = 1
                val repeat = spec.repeat
                val moreRuns = repeat is Repeat.Every && entry.runsDone < repeat.times
                when {
                    !moreRuns -> TaskStatus.Succeeded(entry.runsDone) to null
                    stopping -> TaskStatus.Cancelled to null
                    else -> TaskStatus.Pending to repeat.periodMillis
                }
            } else {
                val retriesLeft = entry.attempt < spec.maxAttempts
                when {
                    // It WOULD run again, but CANCEL_PENDING drops further runs.
                    retriesLeft && stopping -> TaskStatus.Cancelled to null
                    retriesLeft -> {
                        val wait = backoffMillis(spec.retryBackoffMillis, entry.attempt)
                        entry.attempt++
                        TaskStatus.Pending to wait
                    }
                    else -> {
                        val message = failure.message ?: failure::class.simpleName.orEmpty()
                        TaskStatus.Failed(message, entry.attempt) to null
                    }
                }
            }
        entry.status = status
        publish(id, status)
        return nextDelay
    }

    /**
     * Puts the task in the ready queue now, or after [delayMillis] using a
     * timer coroutine. Call with the lock held: the isFinal check and the
     * timer assignment then happen together, so cancel() always finds (and
     * stops) the timer instead of leaving a stray coroutine asleep.
     */
    private fun queueAfter(id: TaskId, delayMillis: Long) {
        val entry = entries[id] ?: return
        if (entry.status.isFinal) return
        if (delayMillis == 0L) {
            enqueue(id)
            return
        }
        entry.timer =
            scope.launch {
                delay(delayMillis)
                enqueue(id)
            }
    }

    private fun enqueue(id: TaskId) {
        synchronized(lock) {
            val entry = entries[id] ?: return
            if (entry.status.isFinal) return // cancelled while waiting
            entry.timer = null
            ready.add(Ready(id, entry.spec.priority, nextSequence++))
            wakeUps.trySend(Unit) // UNLIMITED channel: never fails while open
        }
    }

    // Called with the lock held, so status changes are published in order.
    private fun publish(id: TaskId, status: TaskStatus) {
        mutableStatuses.update { it + (id to status) }
    }

    private companion object {
        // 2^30 x base is already absurdly long; capping the shift avoids
        // `1L shl 64` wrapping around to a zero or negative delay.
        const val MAX_BACKOFF_SHIFT = 30

        /** base, 2 x base, 4 x base, ... with no Long overflow. */
        fun backoffMillis(baseMillis: Long, attempt: Int): Long {
            val shift = minOf(attempt - 1, MAX_BACKOFF_SHIFT)
            return if (baseMillis > (Long.MAX_VALUE shr shift)) Long.MAX_VALUE else baseMillis shl shift
        }
    }
}
