package l10_flow.lesson01_flow_basics

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking

/**
 * =====================================================================
 *  LESSON 10.1 — Flow basics: a stream of values over time
 * =====================================================================
 *  GOAL   : Build a Flow with flow { emit(...) }, flowOf and asFlow,
 *           collect it, and understand "cold": the code inside a flow
 *           runs again for EVERY collector, and only when collected.
 *  RUN    : ./gradlew -q run -Plesson=l10_flow.lesson01_flow_basics.FlowBasicsKt
 *  OUTPUT :
 *           flow created, nothing ran yet
 *           flow started
 *           first collector got 1
 *           first collector got 2
 *           first collector got 3
 *           flow started
 *           second collector got 1
 *           second collector got 2
 *           second collector got 3
 *           flowOf: apple
 *           flowOf: banana
 *           asFlow: 10 20 30
 *           flow started
 *           collect returned after 3 values
 *           sequence (no suspending): [1, 2, 3]
 *  NEXT   : lesson02_intermediate_operators/IntermediateOperators.kt
 * =====================================================================
 */

private const val STEP_MILLIS = 50L

// A function that RETURNS a Flow. Calling it runs nothing yet: it only
// describes "what to do when someone collects".
fun countToThree(): Flow<Int> =
    flow {
        println("flow started")
        for (number in 1..3) {
            delay(STEP_MILLIS) // a flow may suspend between values
            emit(number) // hand one value to the collector
        }
    }

fun main() =
    runBlocking {
        // ── 1. CONCEPT: a Flow is a stream of values over time ───────────
        // A suspend function returns ONE value later. A Flow emits MANY
        // values, one after another, and may suspend (wait) in between.
        // Think "a List whose items arrive over time".
        val numbers = countToThree()
        println("flow created, nothing ran yet")

        // ── 2. CONCEPT: collect = "run the flow and give me each value" ──
        numbers.collect { value -> println("first collector got $value") }

        // ── 3. CONCEPT: flows are COLD ───────────────────────────────────
        // Collecting again runs the flow { } block again from the start:
        // "flow started" prints twice. Each collector gets its own run.
        // (Hot flows that are shared by many collectors: lesson08_stateflow.)
        numbers.collect { value -> println("second collector got $value") }

        // ── 4. CONCEPT: other ways to build a flow ───────────────────────
        flowOf("apple", "banana").collect { println("flowOf: $it") }

        val tens = mutableListOf<Int>()
        listOf(10, 20, 30).asFlow().collect { tens += it }
        println("asFlow: ${tens.joinToString(" ")}")

        // ── 5. CONCEPT: collect is a suspend function ────────────────────
        // It returns only after the flow has emitted everything, so the
        // next line runs after all 3 values.
        var count = 0
        numbers.collect { count++ }
        println("collect returned after $count values")
        // This third collect printed "flow started" again: cold, as always.

        // ── 6. WHY not just a Sequence? ──────────────────────────────────
        // A Sequence (l06_collections/lesson09_sequences) is also lazy, but
        // its block can NOT suspend: no delay, no network call.
        //   sequence { delay(100); yield(1) }
        //   // ❌ Restricted suspending functions can invoke member or extension
        //   //    suspending functions only on their restricted coroutine scope.
        // Flow = Sequence + suspension. Use a Sequence for in-memory work,
        // a Flow when values come from slow or asynchronous sources.
        val fromSequence = sequence { for (i in 1..3) yield(i) }.toList()
        println("sequence (no suspending): $fromSequence")

        // ── 7. PITFALL: collect needs a coroutine ────────────────────────
        //   fun notSuspending() { countToThree().collect { println(it) } }
        //   // ❌ Suspend function 'suspend fun collect(collector: FlowCollector<Int>): Unit'
        //   //    can only be called from a coroutine or another suspend function.
        // Collect inside runBlocking, launch, or a suspend function.

        // ── 8. TRY IT ────────────────────────────────────────────────────
        // a) Remove the delay in countToThree. Does the output change? Why not?
        // b) Write fun countdown(from: Int): Flow<Int> that emits from..1.
        // c) Collect countToThree() three times and count "flow started".
    }
