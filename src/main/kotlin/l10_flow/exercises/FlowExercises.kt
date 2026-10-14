package l10_flow.exercises

import kotlinx.coroutines.flow.Flow
import l10_flow.FlowTasks

/**
 * =====================================================================
 *  LEVEL 10 — EXERCISES (your turn!)
 * =====================================================================
 *  HOW    : Replace each TODO("...") with your answer. Most answers are
 *           a short operator chain: numbers.filter { ... }.map { ... }
 *  CHECK  : ./gradlew exercises --tests "l10_*"
 *           or open src/test/kotlin/l10_flow/FlowExercisesTest.kt
 *           and click ▶ next to the class name.
 *           The tests use virtual time, so debounce and combine are
 *           checked exactly — and instantly.
 *  STUCK? : Re-read the lesson in the hint. Still stuck? Peek at
 *           ../solutions/FlowSolutions.kt — but try first!
 *  NOTE   : debounce needs @OptIn(FlowPreview::class) on the function
 *           (lesson05_buffering_timing shows it).
 * =====================================================================
 */
object FlowExercises : FlowTasks {
    // Hint: lesson02_intermediate_operators (filter, map)
    override fun evenSquares(numbers: Flow<Int>): Flow<Int> = TODO("Ex 1: even numbers, squared")

    // Hint: lesson02_intermediate_operators (scan / runningReduce)
    override fun runningTotal(numbers: Flow<Int>): Flow<Int> = TODO("Ex 2: running total")

    // Hint: lesson02_intermediate_operators (withIndex) — the index starts at 0
    override fun numbered(items: Flow<String>): Flow<String> = TODO("Ex 3: \"1. a\", \"2. b\", ...")

    // Hint: lesson02_intermediate_operators (transformWhile)
    override fun untilFirstNegative(numbers: Flow<Int>): Flow<Int> = TODO("Ex 4: up to the first negative")

    // Hint: l05_oop/lesson04_visibility (require) BEFORE building the flow, then
    //       lesson03_terminal_operators (your own operator: flow { collect { ... } })
    override fun <T> batches(
        items: Flow<T>,
        size: Int,
    ): Flow<List<T>> = TODO("Ex 5: lists of size, last one may be shorter")

    // Hint: lesson03_terminal_operators (fold, or toList) + l04_null_safety (null for "no answer")
    override suspend fun average(numbers: Flow<Int>): Double? = TODO("Ex 6: average or null")

    // Hint: lesson02_intermediate_operators (scan, drop) + l06_collections/lesson04_maps (+ on maps)
    //       scan also emits its start value: skip it with drop(1).
    override fun latestPerKey(events: Flow<Pair<String, Int>>): Flow<Map<String, Int>> =
        TODO("Ex 7: latest value per key")

    // Hint: require + lesson02_intermediate_operators (map, filter, distinctUntilChanged)
    //       + lesson05_buffering_timing (debounce). Order matters!
    override fun searchQueries(
        typed: Flow<String>,
        waitMillis: Long,
    ): Flow<String> = TODO("Ex 8: trim, skip blank, debounce, no repeats")

    // Hint: lesson06_combining (combine)
    override fun sumOfLatest(
        a: Flow<Int>,
        b: Flow<Int>,
    ): Flow<Int> = TODO("Ex 9: sum of the latest values")

    // Hint: require + lesson07_errors_completion (retry, retryWhen).
    //       Careful: retry(n) needs n > 0, but retries = 0 is allowed here.
    override fun withRetries(
        read: Flow<String>,
        retries: Long,
    ): Flow<String> = TODO("Ex 10: retry a failing flow")
}
