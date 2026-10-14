package l10_flow.lesson12_testing_flows

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

/**
 * =====================================================================
 *  LESSON 10.12 — Testing flows
 * =====================================================================
 *  GOAL   : Test finite flows with toList, step through any flow with
 *           Turbine (awaitItem, awaitComplete, awaitError), test hot
 *           flows and StateFlow, and test time-based operators on a
 *           virtual clock.
 *  RUN    : ./gradlew -q run -Plesson=l10_flow.lesson12_testing_flows.TestingFlowsKt
 *           Tests: src/test/kotlin/l10_flow/Lesson12ExampleTest.kt
 *  OUTPUT :
 *           toList: [3, 2, 1]
 *           first: 3
 *           take(2): [3, 2]
 *           thermostat starts at 20, then 22
 *           the real tests are in Lesson12ExampleTest.kt
 *  NEXT   : practice/ticker/StockTicker.kt
 * =====================================================================
 */

private const val DEMO_STEP_MS = 10L
private const val DEMO_START = 3
private const val START_TEMPERATURE = 20
private const val WARMER = 22

/** Counts down from [from] to 1, waiting [stepMs] between numbers. */
fun countdown(from: Int, stepMs: Long): Flow<Int> = flow {
    require(from >= 0) { "from must not be negative, was $from" }
    for (n in from downTo 1) {
        emit(n)
        if (n > 1) delay(stepMs)
    }
}

/** Emits a query only after the user stopped typing for [quietMs]. */
@OptIn(FlowPreview::class) // debounce is still marked as a preview API
fun Flow<String>.settledQueries(quietMs: Long): Flow<String> = debounce(quietMs)

/** A tiny state holder to test: state in, state out. */
class Thermostat(initial: Int) {
    private val _target = MutableStateFlow(initial)
    val target: StateFlow<Int> = _target.asStateFlow()

    fun set(value: Int) {
        _target.value = value
    }
}

fun main() =
    runBlocking {
        // ── 1. CONCEPT: finite flows - just collect them ─────────────────
        // toList(), first() and take(n) turn a flow into plain values you
        // can compare with assertEquals. Enough for most finite flows.
        println("toList: ${countdown(DEMO_START, DEMO_STEP_MS).toList()}")
        println("first: ${countdown(DEMO_START, DEMO_STEP_MS).first()}")
        println("take(2): ${countdown(DEMO_START, DEMO_STEP_MS).take(2).toList()}")

        // ── 2. CONCEPT: Turbine - step through a flow ────────────────────
        // Turbine (app.cash.turbine, a TEST library) collects a flow for you
        // and lets you check it one event at a time:
        //
        //   flow.test {
        //       assertEquals(3, awaitItem())   // the next value
        //       awaitComplete()                // the flow finished normally
        //       // awaitError()                // ...or failed: returns the exception
        //       // expectNoEvents()            // nothing arrived (yet)
        //       // cancelAndIgnoreRemainingEvents() // for flows that never end
        //   }
        //
        // If an event you didn't check arrives, the test FAILS - so tests
        // can't silently ignore extra values. Each await has a timeout
        // (default 3 s of REAL time) so a test can't hang forever.

        // ── 3. CONCEPT: testing StateFlow ────────────────────────────────
        // A StateFlow always starts with its current value, so the first
        // awaitItem() is the initial state. It never completes: end with
        // cancelAndIgnoreRemainingEvents(). This main only reads .value;
        // see the Turbine version in Lesson12ExampleTest
        // ("stateflow - first item is the current value").
        val thermostat = Thermostat(START_TEMPERATURE)
        val before = thermostat.target.value
        thermostat.set(WARMER)
        println("thermostat starts at $before, then ${thermostat.target.value}")

        // ── 4. PITFALL: conflation in tests ──────────────────────────────
        // StateFlow keeps only the latest value (lesson08_stateflow). Set two
        // values with no suspension in between and a collector on a normal
        // test dispatcher may see only the second one. Turbine collects with
        // an unconfined dispatcher, so it sees each value as it is set - but
        // don't write tests that depend on seeing every intermediate state.

        // ── 5. CONCEPT: hot flows and backgroundScope ────────────────────
        // A collector of a hot flow never finishes. In runTest, start it in
        // backgroundScope: it is cancelled automatically when the test ends.

        // ── 6. CONCEPT: virtual time ─────────────────────────────────────
        // Inside runTest, delay() and debounce() use a virtual clock
        // (l09_coroutines/lesson16_testing): a 5-minute debounce test runs in
        // milliseconds. Make waits parameters (like stepMs above) anyway, so
        // the code is easy to configure.
        println("the real tests are in Lesson12ExampleTest.kt")

        // ── 7. TRY IT ────────────────────────────────────────────────────
        // a) Run Lesson12ExampleTest. Then change one expected value and read
        //    the failure message.
        // b) In the countdown test, delete awaitComplete(). What does Turbine say?
        // c) Add a test for settledQueries: "k" then "ko" 400 ms later, with
        //    quietMs = 300. Which values come out, and at what virtual time?
    }
