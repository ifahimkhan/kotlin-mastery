package l10_flow.lesson03_terminal_operators

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.runBlocking

/**
 * =====================================================================
 *  LESSON 10.3 — Terminal operators: run the flow, get a result
 * =====================================================================
 *  GOAL   : Use the operators that actually RUN a flow — collect,
 *           toList, first, single, reduce, fold, count, collectIndexed,
 *           launchIn — and write your own operator with flow { collect }.
 *  RUN    : ./gradlew -q run -Plesson=l10_flow.lesson03_terminal_operators.TerminalOperatorsKt
 *  OUTPUT :
 *           toList: [1, 2, 3]
 *           toSet: [1, 2]
 *           first: 1
 *           firstOrNull { it > 10 }: null
 *           first() on an empty flow -> NoSuchElementException
 *           single: 42
 *           single() on two values -> IllegalArgumentException
 *           reduce (sum): 6
 *           fold (sum starting at 100): 106
 *           count: 3, even count: 1
 *           collectIndexed: 0=a
 *           collectIndexed: 1=b
 *           launchIn got 1
 *           launchIn got 2
 *           launchIn job completed: true
 *           everySecond: [a, c, e]
 *  NEXT   : lesson04_flow_context/FlowContext.kt
 * =====================================================================
 */

// ── Helper for section 6: your own operator ───────────────────────────
// An operator is just a function that returns a new flow. Inside
// flow { }, collect the upstream and emit what you want downstream.
fun <T> Flow<T>.everySecond(): Flow<T> =
    flow {
        var keep = true
        collect { value ->
            if (keep) emit(value)
            keep = !keep
        }
    }

fun main() =
    runBlocking {
        // ── 1. CONCEPT: terminal = "run it now" ──────────────────────────
        // Intermediate operators (lesson02) return a new Flow. Terminal
        // operators are suspend functions: they collect the flow and
        // return a plain value when it is done.
        println("toList: ${flowOf(1, 2, 3).toList()}")
        println("toSet: ${flowOf(1, 2, 1, 2).toSet()}")

        // ── 2. CONCEPT: first values ─────────────────────────────────────
        // first() stops the flow right after the first value (like take(1)).
        println("first: ${flowOf(1, 2, 3).first()}")
        println("firstOrNull { it > 10 }: ${flowOf(1, 2, 3).firstOrNull { it > 10 }}")

        // PITFALL: first() on an empty flow throws.
        // (try/catch: l09_coroutines/lesson11_exceptions)
        try {
            emptyFlow<Int>().first()
        } catch (e: NoSuchElementException) {
            println("first() on an empty flow -> ${e::class.simpleName}")
        }

        // ── 3. CONCEPT: single — exactly one value expected ──────────────
        println("single: ${flowOf(42).single()}")
        try {
            flowOf(1, 2).single()
        } catch (e: IllegalArgumentException) {
            println("single() on two values -> ${e::class.simpleName}")
        }

        // ── 4. CONCEPT: reduce, fold, count ──────────────────────────────
        // Same ideas as on lists (l06_collections/lesson07_aggregation).
        // reduce throws on an empty flow; fold has a start value, so it
        // works on an empty flow too.
        println("reduce (sum): ${flowOf(1, 2, 3).reduce { total, n -> total + n }}")
        println("fold (sum starting at 100): ${flowOf(1, 2, 3).fold(100) { total, n -> total + n }}")
        println("count: ${flowOf(1, 2, 3).count()}, even count: ${flowOf(1, 2, 3).count { it % 2 == 0 }}")

        flowOf("a", "b").collectIndexed { index, value -> println("collectIndexed: $index=$value") }

        // ── 5. CONCEPT: launchIn — collect in a NEW coroutine ────────────
        // collect suspends the caller until the flow ends. launchIn(scope)
        // starts the collection in its own coroutine and returns its Job,
        // so the caller can go on. Put the work in onEach before it.
        val job =
            flowOf(1, 2)
                .onEach { println("launchIn got $it") }
                .launchIn(this) // `this` = the runBlocking scope
        job.join() // wait for it here, so the output order is fixed
        println("launchIn job completed: ${job.isCompleted}")

        // ── 6. CONCEPT: build your own operator ──────────────────────────
        // See everySecond above: flow { upstream.collect { ... emit(...) } }.
        // This pattern is how most real operators are written.
        println("everySecond: ${flowOf("a", "b", "c", "d", "e").everySecond().toList()}")

        // ── 7. PITFALL: forgetting the terminal operator ─────────────────
        //   val job = flowOf(1).onEach { println(it) }   // a Flow, not a Job!
        // Without collect/launchIn/toList, the flow never runs.

        // ── 8. TRY IT ────────────────────────────────────────────────────
        // a) Write fun Flow<Int>.sumAll(): Int with fold. What does it
        //    return for emptyFlow()? And with reduce instead?
        // b) Write your own operator fun <T> Flow<T>.twice(): Flow<T> that
        //    emits every value two times.
        // c) Remove job.join() in section 5. Where does "launchIn got 1" print now?
    }
