package l10_flow.solutions

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.runningReduce
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.flow.withIndex
import l10_flow.FlowTasks

/**
 * Reference answers for Level 10. Try the exercises first!
 * Each answer explains WHY it is written this way.
 */
object FlowSolutions : FlowTasks {
    // filter first, so map only squares the numbers we keep.
    override fun evenSquares(numbers: Flow<Int>): Flow<Int> = numbers.filter { it % 2 == 0 }.map { it * it }

    // runningReduce starts from the first value, so there is no extra 0
    // at the front (scan(0) would emit 0 first).
    override fun runningTotal(numbers: Flow<Int>): Flow<Int> = numbers.runningReduce { total, n -> total + n }

    // withIndex counts from 0; people count from 1.
    override fun numbered(items: Flow<String>): Flow<String> =
        items.withIndex().map { (index, item) -> "${index + 1}. $item" }

    // transformWhile emits the negative number BEFORE deciding to stop.
    // takeWhile would drop it. Returning false cancels the upstream.
    override fun untilFirstNegative(numbers: Flow<Int>): Flow<Int> =
        numbers.transformWhile { number ->
            emit(number)
            number >= 0
        }

    // require runs NOW, when batches(...) is called. Code inside flow { }
    // only runs when someone collects, so a check there would fail late
    // and travel through the flow instead of pointing at the bad call.
    override fun <T> batches(
        items: Flow<T>,
        size: Int,
    ): Flow<List<T>> {
        require(size >= 1) { "size must be at least 1, was $size" }
        return flow {
            var current = emptyList<T>()
            items.collect { item ->
                current = current + item
                if (current.size == size) {
                    emit(current)
                    current = emptyList()
                }
            }
            if (current.isNotEmpty()) emit(current) // the shorter last batch
        }
    }

    // One pass with fold: count and sum together. An empty flow keeps
    // count 0, which means "no average" -> null (never divide by zero).
    override suspend fun average(numbers: Flow<Int>): Double? {
        val (count, sum) = numbers.fold(0 to 0L) { (count, sum), n -> (count + 1) to (sum + n) }
        return if (count == 0) null else sum.toDouble() / count
    }

    // scan keeps the map so far; `map + pair` returns a NEW map with the
    // key replaced (l06_collections/lesson04_maps). drop(1) skips the
    // empty start map that scan emits first.
    override fun latestPerKey(events: Flow<Pair<String, Int>>): Flow<Map<String, Int>> =
        events.scan(emptyMap<String, Int>()) { latest, event -> latest + event }.drop(1)

    // Order matters:
    // 1. trim + skip blank FIRST, so "   " can't reset the debounce wait;
    // 2. debounce waits for the pause;
    // 3. distinctUntilChanged LAST, so "kot" then "kot " (same after trim,
    //    typed again after a pause) is not searched twice.
    @OptIn(FlowPreview::class)
    override fun searchQueries(
        typed: Flow<String>,
        waitMillis: Long,
    ): Flow<String> {
        require(waitMillis >= 1) { "waitMillis must be at least 1, was $waitMillis" }
        return typed
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .debounce(waitMillis)
            .distinctUntilChanged()
    }

    // combine waits until both have a value, then emits on every change.
    override fun sumOfLatest(
        a: Flow<Int>,
        b: Flow<Int>,
    ): Flow<Int> = a.combine(b) { x, y -> x + y }

    // retryWhen collects the flow again while the lambda returns true.
    // attempt counts from 0, so `attempt < retries` allows exactly
    // `retries` extra tries. Why not retry(retries)? retry(n) itself
    // requires n > 0, so retries = 0 ("try once, never again") would crash.
    override fun withRetries(
        read: Flow<String>,
        retries: Long,
    ): Flow<String> {
        require(retries >= 0) { "retries must not be negative, was $retries" }
        return read.retryWhen { _, attempt -> attempt < retries }
    }
}
