package l10_flow.lesson06_combining

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.runBlocking

/**
 * =====================================================================
 *  LESSON 10.6 — Combining flows: zip, combine, merge, flatMap*
 * =====================================================================
 *  GOAL   : Join two flows pair by pair (zip), keep the latest of each
 *           (combine), interleave them (merge), and turn each value into
 *           a whole inner flow (flatMapConcat / flatMapMerge /
 *           flatMapLatest).
 *  RUN    : ./gradlew -q run -Plesson=l10_flow.lesson06_combining.CombiningKt
 *  OUTPUT :
 *           zip: [1a, 2b, 3c]
 *           combine: [1a, 2a, 2b, 3b]
 *           merge: [a1, b1, a2, b2]
 *           flatMapConcat: [1-start, 1-end, 2-start, 2-end]
 *           flatMapMerge: [1-start, 2-start, 1-end, 2-end]
 *           flatMapLatest: [1-start, 2-start, 2-end]
 *  NEXT   : lesson07_errors_completion/ErrorsAndCompletion.kt
 * =====================================================================
 */

// One time step. Values that race each other are at least one step apart
// (250 ms), so the output order is the same even on a busy machine.
private const val STEP_MILLIS = 250L
private const val SHORT_MILLIS = 20L

// Emits the given values, each one at its own time (in steps).
fun timed(vararg values: Pair<Int, String>): Flow<String> =
    flow {
        var now = 0
        for ((step, value) in values) {
            delay((step - now) * STEP_MILLIS)
            now = step
            emit(value)
        }
    }

// The inner flow used by the flatMap examples: "n-start", wait, "n-end".
fun startAndEnd(
    n: Int,
    waitMillis: Long,
): Flow<String> =
    flow {
        emit("$n-start")
        delay(waitMillis)
        emit("$n-end")
    }

@OptIn(ExperimentalCoroutinesApi::class) // flatMapMerge/flatMapLatest/flatMapConcat
fun main() =
    runBlocking {
        // ── 1. CONCEPT: zip = pair values by POSITION ────────────────────
        // 1st with 1st, 2nd with 2nd... It stops when the shorter flow ends,
        // so "d" has no partner and is dropped. Timing does not matter.
        val numbers = flowOf(1, 2, 3)
        val letters = flowOf("a", "b", "c", "d")
        println("zip: ${numbers.zip(letters) { n, l -> "$n$l" }.toList()}")

        // ── 2. CONCEPT: combine = latest of EACH, on every change ────────
        // Once both flows have a value, every new value from EITHER side
        // emits a new pair with the latest value of the other side.
        // Timeline (steps): 1@0  a@1  2@2  b@3  3@4
        val numbersOverTime = timed(0 to "1", 2 to "2", 4 to "3")
        val lettersOverTime = timed(1 to "a", 3 to "b")
        println("combine: ${numbersOverTime.combine(lettersOverTime) { n, l -> "$n$l" }.toList()}")
        // Typical use: combine(searchText, selectedFilter) -> show results.

        // ── 3. CONCEPT: merge = all values from all flows, by time ───────
        val first = timed(0 to "a1", 2 to "a2")
        val second = timed(1 to "b1", 3 to "b2")
        println("merge: ${merge(first, second).toList()}")

        // ── 4. CONCEPT: flatMap* = each value starts an inner flow ───────
        // flatMapConcat: run inner flows ONE AFTER ANOTHER (in order).
        val concat = flowOf(1, 2).flatMapConcat { n -> startAndEnd(n, SHORT_MILLIS) }
        println("flatMapConcat: ${concat.toList()}")

        // flatMapMerge: run inner flows AT THE SAME TIME; values arrive as
        // they come. Inner 1 waits 1 step, inner 2 waits 2 steps.
        val merged = flowOf(1, 2).flatMapMerge { n -> startAndEnd(n, n * STEP_MILLIS) }
        println("flatMapMerge: ${merged.toList()}")

        // flatMapLatest: a new outer value CANCELS the running inner flow
        // (like collectLatest, lesson05_buffering_timing). No timers here:
        // the outer flow sends 2 only after inner 1 has started, and inner 1
        // then waits forever, so "1-end" can never come - it gets cancelled.
        val firstStarted = CompletableDeferred<Unit>()
        val outer =
            flow {
                emit(1)
                firstStarted.await() // wait until inner 1 is running
                emit(2)
            }
        val latest =
            outer.flatMapLatest { n ->
                flow {
                    emit("$n-start")
                    if (n == 1) {
                        firstStarted.complete(Unit)
                        awaitCancellation() // suspends until value 2 cancels it
                    }
                    emit("$n-end")
                }
            }
        println("flatMapLatest: ${latest.toList()}")

        // ── 5. WHY: which one? ───────────────────────────────────────────
        // zip           -> two lists of matching items (question + answer)
        // combine       -> "latest state" of several inputs (form fields)
        // merge         -> several event sources into one stream
        // flatMapConcat -> one request after another, order matters
        // flatMapMerge  -> many requests at once, order does not matter
        // flatMapLatest -> only the newest request matters (search box)

        // ── 6. PITFALL: combine does not see every PAIR ──────────────────
        // If both sides change at the same moment, combine may skip the
        // in-between pair. It is about the latest STATE, not about every
        // event. Use zip when values must be matched one to one.

        // ── 7. TRY IT ────────────────────────────────────────────────────
        // a) Swap zip for combine in section 1 (flowOf has no delays).
        //    Which pairs appear, and why are some skipped? (The exact
        //    result depends on scheduling: combine is about the latest
        //    state, not about pairs.)
        // b) In section 4 give flatMapMerge a concurrency of 1:
        //    flatMapMerge(concurrency = 1) { ... }. What does it act like?
        // c) In flatMapLatest, let inner 1 emit "1-end" BEFORE it completes
        //    firstStarted (and remove awaitCancellation). What changes?
    }
