package l09_coroutines.solutions

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import l09_coroutines.CoroutinesTasks

/**
 * Reference answers for Level 09. Try the exercises first!
 * Each answer explains WHY it is written this way.
 */
object CoroutinesSolutions : CoroutinesTasks {
    // coroutineScope gives us a scope for async AND waits for both children.
    // Starting both before the first await is what makes them concurrent.
    override suspend fun fetchBoth(
        first: suspend () -> String,
        second: suspend () -> String,
    ): Pair<String, String> =
        coroutineScope {
            val a = async { first() }
            val b = async { second() }
            a.await() to b.await()
        }

    // map starts ALL producers first; awaitAll then waits for all of them.
    // sum() of an empty list is 0, so no special case is needed.
    override suspend fun sumAll(producers: List<suspend () -> Int>): Int =
        coroutineScope {
            producers.map { producer -> async { producer() } }.awaitAll().sum()
        }

    // awaitAll returns results in the order of the Deferred list, which is
    // the order of `items`, whatever order they finish in.
    override suspend fun <T, R> parallelMap(
        items: List<T>,
        transform: suspend (T) -> R,
    ): List<R> =
        coroutineScope {
            items.map { item -> async { transform(item) } }.awaitAll()
        }

    // withTimeoutOrNull cancels `load` when time is up and returns null;
    // the Elvis operator turns that null into the default.
    override suspend fun loadOrDefault(
        timeoutMillis: Long,
        default: String,
        load: suspend () -> String,
    ): String {
        require(timeoutMillis > 0) { "timeoutMillis must be positive, was $timeoutMillis" }
        return withTimeoutOrNull(timeoutMillis) { load() } ?: default
    }

    // The loop remembers the last error. CancellationException is rethrown
    // at once: it means "stop", not "this attempt failed", and swallowing
    // it would make the coroutine impossible to cancel (lesson11_exceptions).
    override suspend fun <T> retryWithBackoff(
        times: Int,
        initialDelayMillis: Long,
        factor: Int,
        block: suspend () -> T,
    ): T {
        require(times >= 1) { "times must be at least 1, was $times" }
        require(initialDelayMillis >= 0) { "initialDelayMillis must not be negative, was $initialDelayMillis" }
        require(factor >= 1) { "factor must be at least 1, was $factor" }
        var wait = initialDelayMillis
        var lastError: Exception? = null
        for (attempt in 1..times) {
            try {
                return block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e
            }
            if (attempt < times) {
                delay(wait)
                wait *= factor
            }
        }
        throw checkNotNull(lastError) // times >= 1, so at least one error was stored
    }

    // yield() is a suspension point: it lets other coroutines on the same
    // thread run, AND it throws CancellationException if we were cancelled.
    // ensureActive() alone would only check; on one thread, the code that
    // cancels us could never run, because we never let go of the thread.
    override suspend fun countCooperatively(
        limit: Int,
        onTick: (Int) -> Unit,
    ) {
        for (i in 0 until limit) {
            onTick(i)
            yield()
        }
    }

    // In a supervisorScope a failing child doesn't cancel its siblings.
    // async keeps the error inside the Deferred; await() rethrows it, so
    // each job's result is checked on its own.
    override suspend fun countSuccesses(jobs: List<suspend () -> Unit>): Int =
        supervisorScope {
            val running = jobs.map { job -> async { job() } }
            running.count { deferred ->
                try {
                    deferred.await()
                    true
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    false
                }
            }
        }

    // Many threads doing count++ at once lose updates (read, add, write can
    // interleave). Mutex.withLock lets only one coroutine at a time do it.
    // withContext waits for all launched children before returning.
    override suspend fun safeCount(
        workers: Int,
        incrementsPerWorker: Int,
    ): Int {
        require(workers > 0) { "workers must be positive, was $workers" }
        require(incrementsPerWorker > 0) { "incrementsPerWorker must be positive, was $incrementsPerWorker" }
        val mutex = Mutex()
        var count = 0
        withContext(Dispatchers.Default) {
            repeat(workers) {
                launch {
                    repeat(incrementsPerWorker) {
                        mutex.withLock { count++ }
                    }
                }
            }
        }
        return count
    }

    // produce starts a coroutine that sends into a channel and closes the
    // channel automatically when the block ends. It is still marked
    // "experimental" by the library, so using it needs an explicit opt-in.
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun CoroutineScope.squares(upTo: Int): ReceiveChannel<Int> {
        require(upTo >= 0) { "upTo must not be negative, was $upTo" }
        return produce {
            for (n in 1..upTo) send(n * n)
        }
    }

    // async alone is not parallel: on one thread, CPU-only slices would take
    // turns. withContext(Dispatchers.Default) moves us to the multi-core pool,
    // and every async started inside inherits it, so the slices run on
    // different cores at the same instant. Each slice RETURNS its own sum,
    // so nothing is shared and no Mutex is needed (compare Ex 8).
    override suspend fun parallelSumOf(
        items: List<Int>,
        chunks: Int,
        selector: (Int) -> Long,
    ): Long {
        require(chunks >= 1) { "chunks must be at least 1, was $chunks" }
        if (items.isEmpty()) return 0
        val sliceSize = (items.size + chunks - 1) / chunks // round up -> at most `chunks` slices
        return withContext(Dispatchers.Default) {
            items
                .chunked(sliceSize)
                .map { slice -> async { slice.sumOf(selector) } }
                .awaitAll()
                .sum()
        }
    }
}
