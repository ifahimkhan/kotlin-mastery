package l09_coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import l09_coroutines.lesson16_testing.PriceWatcher
import l09_coroutines.lesson16_testing.ReportWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Examples for lesson 09.16. Every test below "waits" minutes or hours
 * of virtual time, yet the whole class runs in milliseconds.
 *
 * currentTime, advanceTimeBy, runCurrent and UnconfinedTestDispatcher are
 * still marked experimental in kotlinx-coroutines-test, so we opt in.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Lesson16ExampleTest {
    @Test
    fun `polling once a minute takes no real time`() = runTest {
        val watcher = PriceWatcher(fetchPrice = { EXPENSIVE }, pollEveryMs = ONE_MINUTE)

        val result = watcher.waitUntilBelow(limit = LIMIT, maxPolls = 3)

        assertNull(result)
        assertEquals(2 * ONE_MINUTE, testScheduler.currentTime, "3 polls = 2 waits between them")
    }

    @Test
    fun `returns the first cheap price without extra calls`() = runTest {
        val prices = ArrayDeque(listOf(EXPENSIVE, EXPENSIVE, CHEAP, CHEAP))
        var calls = 0
        val watcher = PriceWatcher(fetchPrice = { calls++; prices.removeFirst() }, pollEveryMs = ONE_MINUTE)

        assertEquals(CHEAP, watcher.waitUntilBelow(limit = LIMIT, maxPolls = 10))
        assertEquals(3, calls)
    }

    @Test
    fun `advanceTimeBy runs only what is due`() = runTest {
        var done = false
        launch {
            delay(ONE_MINUTE)
            done = true
        }

        advanceTimeBy(ONE_MINUTE - 1)
        assertFalse(done, "one millisecond early: not done yet")

        advanceTimeBy(1)
        runCurrent() // run the task that just became due
        assertTrue(done)
    }

    @Test
    fun `standard dispatcher queues new coroutines`() = runTest {
        var started = false
        launch(StandardTestDispatcher(testScheduler)) { started = true }
        assertFalse(started, "queued until the scheduler runs it")
        runCurrent()
        assertTrue(started)
    }

    @Test
    fun `unconfined dispatcher starts coroutines at once`() = runTest {
        var started = false
        launch(UnconfinedTestDispatcher(testScheduler)) { started = true }
        assertTrue(started, "ran immediately, no runCurrent needed")
    }

    @Test
    fun `an injected dispatcher keeps work on the test scheduler`() = runTest {
        val writer = ReportWriter(StandardTestDispatcher(testScheduler))
        assertEquals("a\nb", writer.write(listOf("a", "b")))
    }

    private companion object {
        const val ONE_MINUTE = 60_000L
        const val LIMIT = 100
        const val EXPENSIVE = 120
        const val CHEAP = 80
    }
}
