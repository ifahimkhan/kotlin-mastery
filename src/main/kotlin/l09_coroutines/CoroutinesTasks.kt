package l09_coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * The list of Level 09 exercises.
 *
 * Both [l09_coroutines.exercises.CoroutinesExercises] (your answers) and
 * [l09_coroutines.solutions.CoroutinesSolutions] (reference answers)
 * implement this interface, so the SAME tests can check both.
 *
 * The tests run in `runTest` with VIRTUAL time: a `delay(1000)` takes no
 * real time, and the tests can check how long your code "took". So they
 * notice when work that should run at the same time runs one after the
 * other.
 */
interface CoroutinesTasks {
    /**
     * Ex 1. Run [first] and [second] AT THE SAME TIME and return both
     * results as a Pair. If they take 1 s and 2 s, the total must be 2 s.
     */
    suspend fun fetchBoth(
        first: suspend () -> String,
        second: suspend () -> String,
    ): Pair<String, String>

    /** Ex 2. Run all [producers] concurrently and return the sum. Empty list -> 0. */
    suspend fun sumAll(producers: List<suspend () -> Int>): Int

    /**
     * Ex 3. Apply [transform] to every item concurrently. The result keeps
     * the ORDER of [items], no matter which transform finishes first.
     */
    suspend fun <T, R> parallelMap(
        items: List<T>,
        transform: suspend (T) -> R,
    ): List<R>

    /**
     * Ex 4. Run [load]. If it takes longer than [timeoutMillis], stop
     * waiting and return [default]. [timeoutMillis] must be positive
     * (IllegalArgumentException otherwise).
     */
    suspend fun loadOrDefault(
        timeoutMillis: Long,
        default: String,
        load: suspend () -> String,
    ): String

    /**
     * Ex 5. Call [block] up to [times] times until it succeeds. Wait
     * between attempts: [initialDelayMillis] after the first failure, then
     * multiply the wait by [factor] each time (100, 200, 400, ... for
     * factor 2). No wait after the last attempt. If every attempt fails,
     * throw the LAST exception.
     * Invalid arguments (times < 1, negative delay, factor < 1) ->
     * IllegalArgumentException.
     */
    suspend fun <T> retryWithBackoff(
        times: Int,
        initialDelayMillis: Long,
        factor: Int,
        block: suspend () -> T,
    ): T

    /**
     * Ex 6. Call [onTick] with 0, 1, 2, ... up to [limit] - 1, but be
     * COOPERATIVE: give other coroutines a chance to run after every tick,
     * so that the loop stops quickly when it's cancelled.
     */
    suspend fun countCooperatively(
        limit: Int,
        onTick: (Int) -> Unit,
    )

    /**
     * Ex 7. Run all [jobs] concurrently and return how many finished
     * without throwing. A failing job must NOT cancel the others.
     */
    suspend fun countSuccesses(jobs: List<suspend () -> Unit>): Int

    /**
     * Ex 8. Start [workers] coroutines on Dispatchers.Default (real
     * threads!). Each adds 1 to a shared counter [incrementsPerWorker]
     * times. Return the final count; it must be exactly
     * workers * incrementsPerWorker. Both arguments must be positive.
     */
    suspend fun safeCount(
        workers: Int,
        incrementsPerWorker: Int,
    ): Int

    /**
     * Ex 9. Return a channel that sends 1, 4, 9, ... up to [upTo]² and
     * then closes. squares(3) sends 1, 4, 9. [upTo] must not be negative:
     * throw IllegalArgumentException right away, at the call, BEFORE any
     * coroutine starts (an exception inside produce { } would instead
     * cancel the channel and its scope).
     */
    fun CoroutineScope.squares(upTo: Int): ReceiveChannel<Int>
}
