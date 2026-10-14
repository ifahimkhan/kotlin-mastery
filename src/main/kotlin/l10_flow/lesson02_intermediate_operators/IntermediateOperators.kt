package l10_flow.lesson02_intermediate_operators

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningReduce
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.runBlocking

/**
 * =====================================================================
 *  LESSON 10.2 — Intermediate operators: transform a flow
 * =====================================================================
 *  GOAL   : Chain map, filter, transform, take, onEach, withIndex,
 *           distinctUntilChanged, mapNotNull and scan — and see that
 *           an operator chain does NOTHING until a terminal operator
 *           (like collect) runs it.
 *  RUN    : ./gradlew -q run -Plesson=l10_flow.lesson02_intermediate_operators.IntermediateOperatorsKt
 *  OUTPUT :
 *           built the chain, nothing printed yet
 *           map saw 1
 *           map saw 2
 *           collected 20
 *           map saw 3
 *           collected 30
 *           number 1
 *           number 2
 *           -> 2 is even
 *           number 3
 *           transformWhile: [3, 7, -1]
 *           take got 1
 *           take got 2
 *           upstream stopped early, finally ran
 *           takeWhile: [1, 2]
 *           onEach saw a
 *           onEach saw b
 *           0: x
 *           1: y
 *           distinctUntilChanged: [1, 2, 1]
 *           mapNotNull: [1, 3]
 *           drop(1): [2, 3]
 *           scan: [0, 1, 3, 6]
 *           scan + drop(1): [1, 3, 6]
 *           runningReduce: [1, 3, 6]
 *  NEXT   : lesson03_terminal_operators/TerminalOperators.kt
 * =====================================================================
 */

// Small helper: collect every value into a list and show it.
// (Kotlin has toList() for this: lesson03_terminal_operators.)
suspend fun <T> Flow<T>.asText(): String {
    val items = mutableListOf<T>()
    collect { items += it }
    return items.toString()
}

fun main() =
    runBlocking {
        // ── 1. CONCEPT: operators are lazy ───────────────────────────────
        // map and filter only DESCRIBE steps. Nothing runs until collect.
        // Then each value travels through the WHOLE chain before the next
        // one starts: 1 is mapped (and filtered out), then 2, then 3.
        val chain =
            flowOf(1, 2, 3)
                .map { number ->
                    println("map saw $number")
                    number * 10
                }.filter { it > 10 }
        println("built the chain, nothing printed yet")
        chain.collect { println("collected $it") }

        // ── 2. CONCEPT: transform = emit 0, 1 or many values per input ───
        // map is "exactly one out per one in". transform lets you emit as
        // many values as you like (or none).
        flowOf(1, 2, 3)
            .transform { number ->
                emit("number $number")
                if (number % 2 == 0) emit("-> $number is even")
            }.collect { println(it) }

        // transformWhile keeps going while the lambda returns true. Here the
        // first negative number is still emitted, then the flow stops.
        val untilNegative = flowOf(3, 7, -1, 5).transformWhile { number -> emit(number); number >= 0 }
        println("transformWhile: ${untilNegative.asText()}")

        // ── 3. CONCEPT: take stops the UPSTREAM early ────────────────────
        // After 2 values, take cancels the flow above it. The flow's code
        // never reaches its third emit, but its finally block still runs,
        // so cleanup (closing a file, a connection) is safe.
        // (try/finally: l09_coroutines/lesson11_exceptions)
        flow {
            try {
                emit(1)
                emit(2)
                println("never printed")
                emit(3)
            } finally {
                println("upstream stopped early, finally ran")
            }
        }.take(2).collect { println("take got $it") }

        // takeWhile: take values while the condition holds, then stop.
        println("takeWhile: ${flowOf(1, 2, 10, 3).takeWhile { it < 5 }.asText()}")

        // ── 4. CONCEPT: more everyday operators ──────────────────────────
        // onEach: do something with each value (log it) and pass it on.
        flowOf("a", "b").onEach { println("onEach saw $it") }.collect { }

        // withIndex: pair each value with its position (0, 1, 2, ...).
        flowOf("x", "y").withIndex().collect { (index, value) -> println("$index: $value") }

        // distinctUntilChanged: drop a value that equals the one just
        // before it. Not a full "distinct": 1 may come back later.
        println("distinctUntilChanged: ${flowOf(1, 1, 2, 2, 1).distinctUntilChanged().asText()}")

        // mapNotNull: map, and drop the nulls in one step.
        println("mapNotNull: ${flowOf("1", "two", "3").mapNotNull { it.toIntOrNull() }.asText()}")

        // drop(n): skip the first n values, pass the rest (the opposite of take).
        println("drop(1): ${flowOf(1, 2, 3).drop(1).asText()}")

        // ── 5. CONCEPT: scan = a running fold ────────────────────────────
        // Like runningFold on a list (l06_collections/lesson07_aggregation):
        // emit the accumulated value after every input. scan(initial)
        // emits the initial value first; runningReduce starts from the
        // first item instead.
        println("scan: ${flowOf(1, 2, 3).scan(0) { total, n -> total + n }.asText()}")
        // Don't want the initial value? Skip it with drop(1).
        println("scan + drop(1): ${flowOf(1, 2, 3).scan(0) { total, n -> total + n }.drop(1).asText()}")
        println("runningReduce: ${flowOf(1, 2, 3).runningReduce { total, n -> total + n }.asText()}")

        // ── 6. PITFALL: a chain without a terminal operator does nothing ─
        //   flowOf(1, 2).onEach { println(it) }   // prints NOTHING
        // onEach only describes a step. Add .collect { } (or launchIn,
        // lesson03_terminal_operators) to actually run it.

        // ── 7. TRY IT ────────────────────────────────────────────────────
        // a) Move filter BEFORE map in section 1. Which "map saw" lines vanish?
        // b) Use transform to emit every word of "a b c" as its own value.
        // c) Count how many times "upstream stopped early" prints with take(5).
    }
