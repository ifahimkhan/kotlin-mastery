package l12_capstones.scheduler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * The contract of the scheduler capstone. YOU write a class implementing
 * TaskScheduler and return it from starter/SchedulerStarter.create();
 * reference/ holds one finished answer. The acceptance tests (virtual
 * time) check both.
 */
interface TaskScheduler {
    /**
     * Every task ever scheduled and its current status. A StateFlow, so a
     * UI (or a test) can read `.value` or collect every change.
     * Finished tasks stay in the map: simple, but a scheduler that runs for
     * months would need to prune them (a stretch goal).
     */
    val statuses: StateFlow<Map<TaskId, TaskStatus>>

    /**
     * Adds a task (status Pending) and returns its id. Ids start at 1 and
     * count up. Throws IllegalStateException after shutdown has started:
     * scheduling then is a programming mistake.
     */
    fun schedule(spec: TaskSpec): TaskId

    /**
     * Cancels a task that is not finished yet: pending tasks never run,
     * a running task is cancelled mid-run, a task waiting for a retry is
     * not retried, and a repeating task has no further runs. Its status
     * becomes Cancelled at once. Returns false if the id is unknown or
     * already final.
     */
    fun cancel(id: TaskId): Boolean

    /**
     * Stops accepting tasks, handles unfinished work as [mode] says, and
     * returns once every task is final and the workers are stopped.
     * Calling it again later is harmless: it returns at once.
     */
    suspend fun shutdown(mode: ShutdownMode)
}

/**
 * Creates schedulers. The scheduler runs all its coroutines inside
 * [scope], so tests can pass a test scope (virtual time) and apps a real
 * one. At most config.maxConcurrent runs execute at the same time.
 */
interface SchedulerFactory {
    fun create(scope: CoroutineScope, config: SchedulerConfig): TaskScheduler
}
