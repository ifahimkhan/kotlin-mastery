package l10_flow

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import l10_flow.lesson12_testing_flows.Thermostat
import l10_flow.lesson12_testing_flows.countdown
import l10_flow.lesson12_testing_flows.settledQueries
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * The tests that lesson 10.12 talks about. Read them next to the lesson.
 * (testScheduler.currentTime and UnconfinedTestDispatcher are still marked
 * experimental in kotlinx-coroutines-test, hence the opt-in.)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Lesson12ExampleTest {
    @Test
    fun `finite flow - toList is enough`() = runTest {
        assertEquals(listOf(3, 2, 1), countdown(from = 3, stepMs = 1_000).toList())
        // Two 1-second waits took no real time, only virtual time:
        assertEquals(2_000, testScheduler.currentTime)
    }

    @Test
    fun `turbine - one item at a time, then completion`() = runTest {
        countdown(from = 2, stepMs = 1_000).test {
            assertEquals(2, awaitItem())
            assertEquals(1, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `turbine - awaitError returns the failure`() = runTest {
        val failing = flow {
            emit("partial")
            throw IOException("connection lost")
        }
        failing.test {
            assertEquals("partial", awaitItem())
            val error = awaitError()
            assertIs<IOException>(error)
            assertEquals("connection lost", error.message)
        }
    }

    @Test
    fun `stateflow - first item is the current value`() = runTest {
        val thermostat = Thermostat(initial = 20)
        thermostat.target.test {
            assertEquals(20, awaitItem()) // the initial state comes first
            thermostat.set(22)
            assertEquals(22, awaitItem())
            thermostat.set(22) // equal value: StateFlow does not emit it
            expectNoEvents()
            cancelAndIgnoreRemainingEvents() // a StateFlow never completes
        }
    }

    @Test
    fun `hot flow - collect in backgroundScope`() = runTest {
        val events = MutableSharedFlow<String>()
        val received = mutableListOf<String>()
        // Unconfined: the collector subscribes right away, before we emit.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            events.collect { received += it }
        }
        events.emit("a")
        events.emit("b")
        assertEquals(listOf("a", "b"), received)
        // No cancel needed: backgroundScope ends with the test.
    }

    @Test
    fun `debounce on virtual time - only the settled query survives`() = runTest {
        val typing = flow {
            emit("k")
            delay(100)
            emit("ko")
            delay(100)
            emit("kot")
            delay(1_000) // the user stops typing
        }
        typing.settledQueries(quietMs = 300).test {
            assertEquals("kot", awaitItem())
            // "kot" arrived at 200 ms and waited 300 ms of quiet:
            assertEquals(500, testScheduler.currentTime)
            awaitComplete()
        }
    }
}
