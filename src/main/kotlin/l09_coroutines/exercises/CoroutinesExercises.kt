package l09_coroutines.exercises

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import l09_coroutines.CoroutinesTasks

/**
 * =====================================================================
 *  LEVEL 09 — EXERCISES (your turn!)
 * =====================================================================
 *  HOW    : Replace each TODO("...") with your answer.
 *           Many functions are `suspend`, so you can call delay, async,
 *           coroutineScope, ... directly inside them.
 *  CHECK  : ./gradlew exercises --tests "l09_*"
 *           or open src/test/kotlin/l09_coroutines/CoroutinesExercisesTest.kt
 *           and click ▶ next to the class name.
 *           The tests use virtual time: they can tell if your code runs
 *           things one after the other instead of at the same time.
 *  STUCK? : Re-read the lesson in the hint. Still stuck? Peek at
 *           ../solutions/CoroutinesSolutions.kt — but try first!
 * =====================================================================
 */
object CoroutinesExercises : CoroutinesTasks {
    // Hint: lesson05_async_await (async, await) + lesson03_builders (coroutineScope)
    override suspend fun fetchBoth(
        first: suspend () -> String,
        second: suspend () -> String,
    ): Pair<String, String> = TODO("Ex 1: run both at the same time")

    // Hint: lesson05_async_await (map to async, awaitAll)
    override suspend fun sumAll(producers: List<suspend () -> Int>): Int = TODO("Ex 2: concurrent sum")

    // Hint: lesson05_async_await (awaitAll keeps the order of the list)
    override suspend fun <T, R> parallelMap(
        items: List<T>,
        transform: suspend (T) -> R,
    ): List<R> = TODO("Ex 3: concurrent map, same order")

    // Hint: l05_oop/lesson04_visibility (require) + lesson10_timeouts (withTimeoutOrNull)
    //       + l04_null_safety/lesson02_safe_call (?:)
    override suspend fun loadOrDefault(
        timeoutMillis: Long,
        default: String,
        load: suspend () -> String,
    ): String = TODO("Ex 4: give up after the timeout")

    // Hint: l05_oop/lesson04_visibility (require) + a loop with try/catch around block()
    //       + lesson11_exceptions (never swallow CancellationException)
    override suspend fun <T> retryWithBackoff(
        times: Int,
        initialDelayMillis: Long,
        factor: Int,
        block: suspend () -> T,
    ): T = TODO("Ex 5: retry with growing delays")

    // Hint: lesson09_cancellation (yield). On ONE thread, nobody else can run
    //       (not even the code that cancels you) until you suspend.
    override suspend fun countCooperatively(
        limit: Int,
        onTick: (Int) -> Unit,
    ): Unit = TODO("Ex 6: tick, then let others run")

    // Hint: lesson08_structured_concurrency (supervisorScope) + lesson11_exceptions
    //       (await rethrows the job's exception: catch it per job)
    override suspend fun countSuccesses(jobs: List<suspend () -> Unit>): Int = TODO("Ex 7: count jobs that succeed")

    // Hint: lesson07_with_context (withContext(Dispatchers.Default)) + lesson13_shared_state (Mutex, withLock)
    override suspend fun safeCount(
        workers: Int,
        incrementsPerWorker: Int,
    ): Int = TODO("Ex 8: a counter that is safe across threads")

    // Hint: l05_oop/lesson04_visibility (require) + lesson14_channels (produce, send).
    //       Call require BEFORE produce { }: an exception thrown inside the
    //       produce block travels through the channel, not to the caller.
    override fun CoroutineScope.squares(upTo: Int): ReceiveChannel<Int> = TODO("Ex 9: a channel of squares")

    // Hint: lesson17_parallelism (withContext(Dispatchers.Default), async per slice, awaitAll)
    //       + l06_collections/lesson07_aggregation (chunked, sumOf). Round the slice size UP,
    //       so you get at most `chunks` slices.
    override suspend fun parallelSumOf(
        items: List<Int>,
        chunks: Int,
        selector: (Int) -> Long,
    ): Long = TODO("Ex 10: sum the slices on several cores at once")
}
