package l10_flow

import kotlinx.coroutines.flow.Flow

/**
 * The list of Level 10 exercises.
 *
 * Both [l10_flow.exercises.FlowExercises] (your answers) and
 * [l10_flow.solutions.FlowSolutions] (reference answers) implement this
 * interface, so the SAME tests can check both.
 *
 * Most functions take a Flow and return a NEW Flow: you build an operator
 * chain, the tests collect it. The tests run in `runTest` with VIRTUAL
 * time, so time-based operators (debounce, combine) are checked exactly
 * and instantly.
 */
interface FlowTasks {
    /** Ex 1. Keep the even numbers and square them: 1, 2, 3, 4 -> 4, 16. */
    fun evenSquares(numbers: Flow<Int>): Flow<Int>

    /** Ex 2. The running total after each number: 1, 2, 3 -> 1, 3, 6. Empty in -> empty out. */
    fun runningTotal(numbers: Flow<Int>): Flow<Int>

    /** Ex 3. Number each item, starting at 1: "a", "b" -> "1. a", "2. b". */
    fun numbered(items: Flow<String>): Flow<String>

    /**
     * Ex 4. Emit the numbers up to AND INCLUDING the first negative one,
     * then stop collecting the upstream: 3, 7, -1, 5 -> 3, 7, -1.
     */
    fun untilFirstNegative(numbers: Flow<Int>): Flow<Int>

    /**
     * Ex 5. Group the items into lists of [size]; the last list may be
     * shorter: 1..5 with size 2 -> [1, 2], [3, 4], [5].
     * [size] < 1 is a caller bug: throw IllegalArgumentException right
     * away, when batches(...) is CALLED — not later, when it is collected.
     */
    fun <T> batches(
        items: Flow<T>,
        size: Int,
    ): Flow<List<T>>

    /** Ex 6. The average of all numbers, or null for an empty flow. */
    suspend fun average(numbers: Flow<Int>): Double?

    /**
     * Ex 7. Events are (key, value) pairs. After EACH event, emit a map of
     * the latest value for every key seen so far:
     * ("a",1), ("b",2), ("a",3) -> {a=1}, {a=1, b=2}, {a=3, b=2}.
     */
    fun latestPerKey(events: Flow<Pair<String, Int>>): Flow<Map<String, Int>>

    /**
     * Ex 8. Turn raw text from a search box into search queries:
     * trim each value, ignore blank ones, emit a query only after the user
     * paused typing for [waitMillis], and never emit the same query twice
     * in a row. [waitMillis] < 1 -> IllegalArgumentException when called.
     */
    fun searchQueries(
        typed: Flow<String>,
        waitMillis: Long,
    ): Flow<String>

    /**
     * Ex 9. Each time [a] or [b] emits (once both have emitted at least
     * once), emit the sum of their LATEST values.
     * a: 1 at 0 ms, 5 at 200 ms; b: 10 at 100 ms -> 11, 15.
     */
    fun sumOfLatest(
        a: Flow<Int>,
        b: Flow<Int>,
    ): Flow<Int>

    /**
     * Ex 10. If collecting [read] fails, collect it again — up to [retries]
     * more times. If it still fails, the error reaches the collector.
     * [retries] < 0 -> IllegalArgumentException when called.
     */
    fun withRetries(
        read: Flow<String>,
        retries: Long,
    ): Flow<String>
}
