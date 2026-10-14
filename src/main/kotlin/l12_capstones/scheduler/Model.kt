package l12_capstones.scheduler

/**
 * A task's id. A value class (l05_oop lesson 10): type-safe like a class,
 * as cheap as an Int at runtime, and impossible to mix up with other Ints.
 */
@JvmInline
value class TaskId(val value: Int)

/** How often a task runs. */
sealed interface Repeat {
    data object Once : Repeat

    /**
     * [times] runs in total. The next run is queued [periodMillis] after
     * the previous run FINISHED (so slow runs never pile up).
     */
    data class Every(val periodMillis: Long, val times: Int) : Repeat {
        init {
            require(periodMillis > 0) { "periodMillis must be > 0, was $periodMillis" }
            require(times >= 1) { "times must be >= 1, was $times" }
        }
    }
}

/**
 * What to run and how.
 *
 * - Higher [priority] runs first when several tasks are waiting for a
 *   worker. Equal priorities run first-ready, first-served: in the order
 *   they became ready (their delay ended), not the order they were scheduled.
 * - A failing run is tried again up to [maxAttempts] attempts in total,
 *   waiting retryBackoffMillis, then twice that, then four times that, ...
 *   between attempts (the doubling stops growing after 30 steps).
 * - "Failing" means the action threw ANY Throwable: an Exception, an Error
 *   such as TODO()'s NotImplementedError, or a CancellationException from
 *   its own code (for example a withTimeout inside the action). Only
 *   cancel() and shutdown() make a task Cancelled.
 * - The [action] runs on the dispatcher of the scope given to the
 *   scheduler. Blocking work (Thread.sleep, blocking IO) holds a worker
 *   the whole time and can starve the pool: wrap it in
 *   withContext(Dispatchers.IO) { ... } (l09_coroutines/lesson07_with_context).
 */
class TaskSpec(
    val name: String,
    val priority: Int = 0,
    val initialDelayMillis: Long = 0,
    val repeat: Repeat = Repeat.Once,
    val maxAttempts: Int = 1,
    val retryBackoffMillis: Long = DEFAULT_BACKOFF_MILLIS,
    val action: suspend () -> Unit,
) {
    init {
        require(name.isNotBlank()) { "name must not be blank" }
        require(initialDelayMillis >= 0) { "initialDelayMillis must be >= 0, was $initialDelayMillis" }
        require(maxAttempts >= 1) { "maxAttempts must be >= 1, was $maxAttempts" }
        require(retryBackoffMillis >= 0) { "retryBackoffMillis must be >= 0, was $retryBackoffMillis" }
    }

    override fun toString() = "TaskSpec($name)"

    private companion object {
        const val DEFAULT_BACKOFF_MILLIS = 100L
    }
}

/** Where a task is in its life. Succeeded, Failed and Cancelled are final. */
sealed interface TaskStatus {
    /** Waiting: for its start delay, a free worker, a retry, or its next repetition. */
    data object Pending : TaskStatus

    /** Running on a worker. [attempt] counts from 1 and starts again at 1 for each repetition. */
    data class Running(val attempt: Int) : TaskStatus

    /** Done. [runs] = how many runs succeeded (1 for Repeat.Once, `times` for Repeat.Every). */
    data class Succeeded(val runs: Int) : TaskStatus

    /**
     * Gave up after [attempts] attempts. [message] is the last failure's
     * message, or its class name (e.g. "IllegalStateException") when the
     * failure has no message.
     */
    data class Failed(val message: String, val attempts: Int) : TaskStatus

    data object Cancelled : TaskStatus
}

val TaskStatus.isFinal: Boolean
    get() = this is TaskStatus.Succeeded || this is TaskStatus.Failed || this == TaskStatus.Cancelled

/** What shutdown does with work that is not finished yet. */
enum class ShutdownMode {
    /** Run everything already scheduled to the end (delays, retries, repetitions). */
    DRAIN,

    /**
     * Let the runs in progress finish, cancel everything else. A task
     * that would have run again (retry or repetition) ends as Cancelled.
     */
    CANCEL_PENDING,

    /** Cancel everything at once, including the runs in progress. */
    CANCEL_ALL,
}

data class SchedulerConfig(
    val maxConcurrent: Int = 2,
) {
    init {
        require(maxConcurrent >= 1) { "maxConcurrent must be >= 1, was $maxConcurrent" }
    }
}
