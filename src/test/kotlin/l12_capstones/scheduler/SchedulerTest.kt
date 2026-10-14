package l12_capstones.scheduler

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Acceptance tests for the scheduler capstone, grouped by milestone.
 * The scheduler runs in the test's backgroundScope on virtual time, so
 * currentTime shows exactly when each task ran.
 */
// currentTime / advanceTimeBy / runCurrent are still experimental in kotlinx-coroutines-test.
@OptIn(ExperimentalCoroutinesApi::class)
@TestMethodOrder(MethodOrderer.MethodName::class)
abstract class SchedulerTest(
    private val factory: SchedulerFactory,
) {
    private fun TestScope.scheduler(maxConcurrent: Int = 2): TaskScheduler =
        factory.create(backgroundScope, SchedulerConfig(maxConcurrent))

    private fun TaskScheduler.status(id: TaskId): TaskStatus? = statuses.value[id]

    // ── Milestone 1: run tasks on a worker pool ──────────────────────

    @Test
    fun `m1 a task runs once and ends as Succeeded`() =
        runTest {
            val scheduler = scheduler()
            var runs = 0
            val id = scheduler.schedule(TaskSpec("hello") { runs++ })
            assertEquals(TaskId(1), id, "Hint: ids start at 1")
            scheduler.shutdown(ShutdownMode.DRAIN)
            assertEquals(1, runs)
            assertEquals(TaskStatus.Succeeded(1), scheduler.status(id))
        }

    @Test
    fun `m1 statuses go Pending, Running, Succeeded`() =
        runTest {
            val scheduler = scheduler()
            val seen = mutableListOf<TaskStatus>()
            scheduler.statuses.test {
                assertEquals(emptyMap(), awaitItem())
                val id = scheduler.schedule(TaskSpec("watched") { delay(100) })
                // Collect this task's statuses until it is final. Extra
                // updates in between are fine; the order is what counts.
                while (seen.lastOrNull()?.isFinal != true) {
                    awaitItem()[id]?.let { if (it != seen.lastOrNull()) seen += it }
                }
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(TaskStatus.Pending, seen.first(), "Hint: a new task starts as Pending")
            assertTrue(TaskStatus.Running(1) in seen, "Hint: publish Running(1) when a worker starts it")
            assertEquals(TaskStatus.Succeeded(1), seen.last())
        }

    @Test
    fun `m1 initial delay and the concurrency limit are respected`() =
        runTest {
            val scheduler = scheduler(maxConcurrent = 2)
            val startTimes = mutableListOf<Long>()
            repeat(4) { scheduler.schedule(TaskSpec("job$it") { startTimes += currentTime; delay(100) }) }
            var delayedStart = -1L
            scheduler.schedule(TaskSpec("late", initialDelayMillis = 500) { delayedStart = currentTime })
            scheduler.shutdown(ShutdownMode.DRAIN)
            assertEquals(listOf(0L, 0L, 100L, 100L), startTimes, "Hint: only 2 workers, so 2 rounds of 100 ms")
            assertEquals(500, delayedStart)
        }

    // ── Milestone 2: priorities and repetition ───────────────────────

    @Test
    fun `m2 higher priority runs first, equal priority in schedule order`() =
        runTest {
            val scheduler = scheduler(maxConcurrent = 1)
            val order = mutableListOf<String>()
            fun spec(name: String, priority: Int) = TaskSpec(name, priority) { order += name; delay(10) }
            scheduler.schedule(spec("low", 1))
            scheduler.schedule(spec("high", 5))
            scheduler.schedule(spec("low-2", 1))
            scheduler.schedule(spec("top", 9))
            scheduler.shutdown(ShutdownMode.DRAIN)
            assertEquals(listOf("top", "high", "low", "low-2"), order)
        }

    @Test
    fun `m2 a repeating task runs every period after the previous run`() =
        runTest {
            val scheduler = scheduler()
            val runs = mutableListOf<Long>()
            val tick =
                TaskSpec("tick", repeat = Repeat.Every(1_000, times = 3)) {
                    runs += currentTime
                    delay(50)
                }
            val id = scheduler.schedule(tick)
            advanceTimeBy(100)
            runCurrent()
            assertEquals(TaskStatus.Pending, scheduler.status(id), "Between runs the task is Pending")
            scheduler.shutdown(ShutdownMode.DRAIN)
            assertEquals(listOf(0L, 1_050L, 2_100L), runs, "Hint: the period starts when the previous run ends")
            assertEquals(TaskStatus.Succeeded(3), scheduler.status(id))
        }

    @Test
    fun `m2 equal priorities run in the order they became ready`() =
        runTest {
            val scheduler = scheduler(maxConcurrent = 1)
            val order = mutableListOf<String>()
            fun spec(name: String, delayMillis: Long) =
                TaskSpec(name, priority = 1, initialDelayMillis = delayMillis) { order += name }
            scheduler.schedule(TaskSpec("blocker", priority = 1) { delay(200) })
            scheduler.schedule(spec("late", 100)) // scheduled first, but ready at 100
            scheduler.schedule(spec("early-a", 50)) // ready at 50
            scheduler.schedule(spec("early-b", 50)) // ready at 50, after early-a
            scheduler.shutdown(ShutdownMode.DRAIN)
            assertEquals(listOf("early-a", "early-b", "late"), order, "Hint: ties go first-ready, first-served")
        }

    // ── Milestone 3: failures and retries ────────────────────────────

    @Test
    fun `m3 a failing run is retried with exponential backoff`() =
        runTest {
            val scheduler = scheduler()
            val attempts = mutableListOf<Long>()
            val id =
                scheduler.schedule(TaskSpec("flaky", maxAttempts = 3, retryBackoffMillis = 100) {
                    attempts += currentTime
                    check(attempts.size == 3) { "not yet" }
                })
            scheduler.shutdown(ShutdownMode.DRAIN)
            assertEquals(listOf(0L, 100L, 300L), attempts, "Hint: wait 100, then 200 ms")
            assertEquals(TaskStatus.Succeeded(1), scheduler.status(id))
        }

    @Test
    fun `m3 a task that keeps failing ends as Failed and others keep running`() =
        runTest {
            val scheduler = scheduler(maxConcurrent = 1)
            val doomed =
                scheduler.schedule(TaskSpec("doomed", maxAttempts = 2, retryBackoffMillis = 0) { error("disk full") })
            var survived = false
            val fine = scheduler.schedule(TaskSpec("fine") { survived = true })
            scheduler.shutdown(ShutdownMode.DRAIN)
            assertEquals(TaskStatus.Failed("disk full", attempts = 2), scheduler.status(doomed))
            assertTrue(survived, "Hint: one failing task must not stop the workers")
            assertEquals(TaskStatus.Succeeded(1), scheduler.status(fine))
        }

    @Test
    fun `m3 a task's own timeout is a failed attempt, not a cancel`() =
        runTest {
            val scheduler = scheduler()
            val id =
                scheduler.schedule(TaskSpec("slow-call", maxAttempts = 2, retryBackoffMillis = 0) {
                    withTimeout(10) { delay(100) }
                })
            scheduler.shutdown(ShutdownMode.DRAIN) // must not hang
            val status = assertIs<TaskStatus.Failed>(
                scheduler.status(id),
                "Hint: withTimeout throws a CancellationException from the TASK's code - count it as a failure",
            )
            assertEquals(2, status.attempts)
        }

    @Test
    fun `m3 an Error thrown by a task is a failure and the worker survives`() =
        runTest {
            val scheduler = scheduler(maxConcurrent = 1)
            val broken = scheduler.schedule(TaskSpec("broken") { TODO("not written yet") })
            var survived = false
            val fine = scheduler.schedule(TaskSpec("fine") { survived = true })
            scheduler.shutdown(ShutdownMode.DRAIN) // must not hang
            assertIs<TaskStatus.Failed>(scheduler.status(broken), "Hint: TODO() throws an Error - catch Throwable")
            assertTrue(survived, "Hint: the only worker must keep working after an Error")
            assertEquals(TaskStatus.Succeeded(1), scheduler.status(fine))
        }

    // ── Milestone 4: cancellation and shutdown ───────────────────────

    @Test
    fun `m4 cancel stops pending and running tasks`() =
        runTest {
            val scheduler = scheduler()
            var pendingRan = false
            var runningFinished = false
            val pending = scheduler.schedule(TaskSpec("later", initialDelayMillis = 1_000) { pendingRan = true })
            val running = scheduler.schedule(TaskSpec("long") { delay(1_000); runningFinished = true })
            advanceTimeBy(100)
            runCurrent()
            assertEquals(TaskStatus.Running(1), scheduler.status(running))

            assertTrue(scheduler.cancel(pending))
            assertTrue(scheduler.cancel(running))
            assertEquals(TaskStatus.Cancelled, scheduler.status(running), "Hint: Cancelled at once, not later")
            assertFalse(scheduler.cancel(running), "Already final")
            assertFalse(scheduler.cancel(TaskId(99)), "Unknown id")

            scheduler.shutdown(ShutdownMode.DRAIN)
            assertFalse(pendingRan)
            assertFalse(runningFinished)
            assertEquals(TaskStatus.Cancelled, scheduler.status(pending))
        }

    @Test
    fun `m4 CANCEL_PENDING lets running work finish and drops the rest`() =
        runTest {
            val scheduler = scheduler(maxConcurrent = 1)
            val running = scheduler.schedule(TaskSpec("running") { delay(300) })
            val waiting = scheduler.schedule(TaskSpec("waiting") {})
            val repeating =
                scheduler.schedule(TaskSpec("repeating", initialDelayMillis = 50, repeat = Repeat.Every(100, 5)) {})
            advanceTimeBy(10)
            runCurrent()
            scheduler.shutdown(ShutdownMode.CANCEL_PENDING)
            assertEquals(TaskStatus.Succeeded(1), scheduler.status(running))
            assertEquals(TaskStatus.Cancelled, scheduler.status(waiting))
            assertEquals(TaskStatus.Cancelled, scheduler.status(repeating))
            assertEquals(300, currentTime, "shutdown returns when the running task is done")
        }

    @Test
    fun `m4 CANCEL_ALL stops everything and no task can be added afterwards`() =
        runTest {
            val scheduler = scheduler()
            var finished = false
            val running = scheduler.schedule(TaskSpec("running") { delay(1_000); finished = true })
            advanceTimeBy(10)
            runCurrent()
            scheduler.shutdown(ShutdownMode.CANCEL_ALL)
            assertFalse(finished)
            assertEquals(TaskStatus.Cancelled, scheduler.status(running))
            assertEquals(10, currentTime, "Hint: CANCEL_ALL does not wait for running work")
            assertFailsWith<IllegalStateException> { scheduler.schedule(TaskSpec("too late") {}) }
        }

    @Test
    fun `m4 cancel works while a task waits for its retry`() =
        runTest {
            val scheduler = scheduler()
            var attempts = 0
            val id =
                scheduler.schedule(TaskSpec("retrying", maxAttempts = 3, retryBackoffMillis = 1_000) {
                    attempts++
                    error("try again")
                })
            advanceTimeBy(10)
            runCurrent()
            assertEquals(TaskStatus.Pending, scheduler.status(id), "Waiting for the retry")
            assertTrue(scheduler.cancel(id))
            scheduler.shutdown(ShutdownMode.DRAIN)
            assertEquals(1, attempts, "Hint: a cancelled task must not be retried")
            assertEquals(TaskStatus.Cancelled, scheduler.status(id))
            assertEquals(10, currentTime, "Hint: stop the retry timer when the task is cancelled")
        }

    @Test
    fun `m4 CANCEL_PENDING cancels a failing task instead of retrying it`() =
        runTest {
            val scheduler = scheduler(maxConcurrent = 1)
            val id =
                scheduler.schedule(TaskSpec("retrying", maxAttempts = 3, retryBackoffMillis = 100) {
                    delay(50)
                    error("boom")
                })
            advanceTimeBy(10)
            runCurrent()
            scheduler.shutdown(ShutdownMode.CANCEL_PENDING)
            assertEquals(TaskStatus.Cancelled, scheduler.status(id), "Hint: it WOULD run again, so it is Cancelled")
            assertEquals(50, currentTime, "The attempt in progress finished first")
        }

    @Test
    fun `m4 calling shutdown twice is harmless`() =
        runTest {
            val scheduler = scheduler()
            val id = scheduler.schedule(TaskSpec("once") {})
            scheduler.shutdown(ShutdownMode.DRAIN)
            scheduler.shutdown(ShutdownMode.CANCEL_ALL)
            assertEquals(TaskStatus.Succeeded(1), scheduler.status(id))
        }
}
