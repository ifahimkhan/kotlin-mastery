package l10_flow.lesson08_stateflow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/**
 * =====================================================================
 *  LESSON 10.8 — StateFlow: a value that can be watched
 * =====================================================================
 *  GOAL   : Hold state in a MutableStateFlow, expose it read-only,
 *           update it atomically, and know its two surprises:
 *           conflation and "equal values are not emitted".
 *  RUN    : ./gradlew -q run -Plesson=l10_flow.lesson08_stateflow.StateFlowBasicsKt
 *  OUTPUT :
 *           initial value: idle
 *           collector saw: idle
 *           collector saw: done
 *           value is still done, nothing new collected
 *           collector saw: idle
 *           count after 4 x 2500 updates: 10000
 *           read-only view can be cast back to Mutable: false
 *           a late collector starts with: 10000
 *  NEXT   : lesson09_sharedflow/SharedFlowBasics.kt
 * =====================================================================
 */

private const val WORKERS = 4
private const val UPDATES_PER_WORKER = 2_500

/**
 * The usual pattern: a private MutableStateFlow, a public read-only
 * StateFlow. Only this class can change the count.
 */
class Counter {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    // update { } reads the current value and writes the new one ATOMICALLY:
    // if another thread changed it in between, update retries.
    fun increment() = _count.update { it + 1 }
}

fun main() =
    runBlocking {
        // ── 1. CONCEPT: a StateFlow always has a value ───────────────────
        // Unlike a plain Flow, a StateFlow has a current value you can read
        // at any time with .value - it needs an initial value.
        val state = MutableStateFlow("idle")
        println("initial value: ${state.value}")

        // A collector first gets the CURRENT value, then every change.
        val watcher = launch { state.collect { println("collector saw: $it") } }
        yield() // let the collector start (runBlocking has one thread)

        // ── 2. SURPRISE 1: conflation ────────────────────────────────────
        // Two changes with no suspension in between: the slow collector
        // only sees the LATEST value. StateFlow is about "what is the state
        // now", not "every step that happened".
        state.value = "loading"
        state.value = "done"
        yield() // the collector runs once and reads "done"

        // ── 3. SURPRISE 2: equal values are not emitted ──────────────────
        // StateFlow compares with == (equals). Setting an equal value does
        // nothing. Careful with mutable objects: changing a list INSIDE the
        // state is invisible - always set a new value (level 05 data class copy).
        state.value = "done"
        yield()
        println("value is still ${state.value}, nothing new collected")

        state.value = "idle"
        yield()
        watcher.cancel() // a StateFlow never completes: stop collectors yourself

        // ── 4. CONCEPT: update { } for safe read-modify-write ────────────
        // `state.value = state.value + 1` is READ then WRITE: two threads can
        // read the same old value and one update is lost (the race from
        // l09_coroutines/lesson13_shared_state). update { } retries instead.
        val counter = Counter()
        withContext(Dispatchers.Default) {
            repeat(WORKERS) {
                launch { repeat(UPDATES_PER_WORKER) { counter.increment() } }
            }
        } // withContext waits for all its children
        println("count after $WORKERS x $UPDATES_PER_WORKER updates: ${counter.count.value}")

        // ── 5. CONCEPT: read-only exposure ───────────────────────────────
        // asStateFlow() wraps the state, so callers can't cast it back
        // (compare the List cast trick in l06_collections/lesson10_pitfalls).
        val canCastBack = counter.count is MutableStateFlow<*>
        println("read-only view can be cast back to Mutable: $canCastBack")
        // counter.count.value = 0 // ❌ 'val' cannot be reassigned.

        // ── 6. CONCEPT: late collectors get the latest value ─────────────
        val late = launch { counter.count.collect { println("a late collector starts with: $it") } }
        yield()
        late.cancel()

        // ── 7. WHY StateFlow? ────────────────────────────────────────────
        // It is the standard way to hold UI or app state: one current value,
        // any number of watchers, no "missed the event" problems (every new
        // collector gets the current value). For one-off EVENTS, see
        // lesson09_sharedflow.

        // ── 8. TRY IT ────────────────────────────────────────────────────
        // a) Put yield() between "loading" and "done" in section 2. What changes?
        // b) Uncomment `counter.count.value = 0`. Why is `value` a val here?
        // c) Replace update { it + 1 } in Counter with `_count.value = _count.value + 1`
        //    and run a few times. Is the count still 10000?
    }
