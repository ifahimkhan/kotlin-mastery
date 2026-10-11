package l09_coroutines.lesson17_parallelism

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.BrokenBarrierException
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * =====================================================================
 *  LESSON 09.17 — Parallelism: using every CPU core
 * =====================================================================
 *  GOAL   : Tell concurrency from parallelism, run CPU-heavy work on
 *           several cores at once with async + Dispatchers.Default, and
 *           see why async/await alone does not make code parallel.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.lesson17_parallelism.ParallelismKt
 *  OUTPUT :
 *           async on runBlocking's one thread: tasks ran together = false
 *           async on Dispatchers.Default: tasks ran together = true
 *           async on Default.limitedParallelism(1): tasks ran together = false
 *           parallel sum equals sequential sum: true
 *  NEXT   : practice/downloader/Downloader.kt
 * =====================================================================
 */

private const val PARTIES = 2
private const val MEET_TIMEOUT_MILLIS = 1_000L
private const val LAST_NUMBER = 4_000_000L
private const val CHUNKS = 8
private const val MODULO = 1_000L

/**
 * A tiny proof of parallelism. A CyclicBarrier only opens when [PARTIES]
 * threads are waiting at it AT THE SAME MOMENT. If the tasks take turns
 * on one thread, the first one waits alone until it times out.
 * (Blocking a thread on purpose is a demo trick, never do it in real code.)
 */
private fun meetAtBarrier(barrier: CyclicBarrier): Boolean =
    try {
        barrier.await(MEET_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        true
    } catch (e: TimeoutException) {
        false // nobody else came: we were alone
    } catch (e: BrokenBarrierException) {
        false // an earlier task gave up waiting
    }

/** Starts [PARTIES] async tasks on [context] and reports whether they all ran at once. */
private suspend fun ranTogether(context: CoroutineContext): Boolean =
    coroutineScope {
        val barrier = CyclicBarrier(PARTIES)
        List(PARTIES) { async(context) { meetAtBarrier(barrier) } }
            .awaitAll()
            .all { it }
    }

/** Pure CPU work: no suspension point, it holds its thread until done. */
private fun sumOfSquares(range: LongRange): Long = range.sumOf { n -> n * n % MODULO }

/** Splits [range] into [chunks] slices, sums each slice on its own core, adds the results. */
private suspend fun parallelSumOfSquares(range: LongRange, chunks: Int): Long =
    withContext(Dispatchers.Default) {
        val chunkSize = (range.last - range.first + 1) / chunks
        List(chunks) { i ->
            val start = range.first + i * chunkSize
            val end = if (i == chunks - 1) range.last else start + chunkSize - 1
            async { sumOfSquares(start..end) } // inherits Default from withContext
        }.awaitAll().sum()
    }

fun main() = runBlocking {
    // ── 1. CONCEPT: concurrency is not parallelism ───────────────────
    // | Concurrency                        | Parallelism                       |
    // |------------------------------------|-----------------------------------|
    // | many tasks IN PROGRESS at once     | many tasks RUNNING at one instant |
    // | possible on ONE thread (take turns)| needs several threads AND cores   |
    // | helps WAITING work: network, delay | helps CPU work: math, parsing     |
    // | tool: async/launch                 | tool: async/launch + a dispatcher |
    //                                        with several threads (Default)
    // Think of a cook: one cook juggling 4 pots is concurrency. Four cooks
    // each stirring a pot is parallelism.

    // ── 2. PITFALL: async alone does NOT mean parallel ───────────────
    // runBlocking has exactly ONE thread. async only says "start this and
    // give me the result later". With CPU work (no suspension point) each
    // task keeps the thread until it ends, so the tasks run one by one.
    println("async on runBlocking's one thread: tasks ran together = ${ranTogether(EmptyCoroutineContext)}")
    // In lesson 09.5 async DID speed things up on one thread. Why? Those
    // tasks called delay(): they suspended and handed the thread to the
    // next task. Waiting overlaps on one thread; computing does not.

    // ── 3. CONCEPT: Dispatchers.Default runs on several cores ────────
    // Default has one thread per CPU core (at least 2). Give each async a
    // Default thread and the OS runs them on different cores at the same
    // instant: real parallelism.
    println("async on Dispatchers.Default: tasks ran together = ${ranTogether(Dispatchers.Default)}")

    // ── 4. CONCEPT: limitedParallelism caps how many cores you use ───
    // Want to leave cores free for others (a server handling requests)?
    // A limited view of Default runs at most N tasks at one instant.
    // With N = 1 we are back to taking turns.
    val oneAtATime = Dispatchers.Default.limitedParallelism(1)
    println("async on Default.limitedParallelism(1): tasks ran together = ${ranTogether(oneAtATime)}")

    // ── 5. CONCEPT: the parallel recipe ──────────────────────────────
    // 1. Split the work into independent chunks (one per core or a few more).
    // 2. Start each chunk with async on Dispatchers.Default.
    // 3. awaitAll() and combine the partial results.
    // See parallelSumOfSquares above. The answer must match the plain loop.
    val numbers = 1..LAST_NUMBER
    val sequential = sumOfSquares(numbers)
    val parallel = parallelSumOfSquares(numbers, CHUNKS)
    println("parallel sum equals sequential sum: ${parallel == sequential}")

    // ── 6. PITFALL: sharing one variable between parallel chunks ─────
    // var total = 0L
    // withContext(Dispatchers.Default) {
    //     repeat(CHUNKS) { launch { total += sumOfSquares(...) } } // RACE!
    // }
    // Two cores may read the same old total and both write back: one sum
    // is lost, and the result changes from run to run (lesson 09.13).
    // Fix used above: every chunk RETURNS its own result, nothing is shared.

    // ── 7. PITFALL: wrong dispatcher, no cancellation checks ─────────
    // - Dispatchers.IO is for BLOCKING waits (files, JDBC). It has 64+
    //   threads, but you still only have N cores: 64 CPU tasks fighting
    //   over 8 cores is not faster, just more switching. CPU -> Default.
    // - A long CPU loop never suspends, so it can't notice cancellation.
    //   Call ensureActive() or yield() every so often (lesson 09.9).
    // - Tiny chunks cost more to schedule than they save. Chunks should
    //   take at least a few milliseconds each.

    // ── 8. WHY: so how do async/await and parallelism differ? ────────
    // async/await is the SHAPE of the code: "start these, then collect
    // the results". The DISPATCHER decides WHERE they run:
    //   async { }                        -> caller's thread(s); one thread = take turns
    //   async(Dispatchers.Default) { }   -> worker threads on all cores = parallel
    //   withContext(Dispatchers.Default) { many async { } } -> same, children inherit
    // So: async/await gives concurrency; async/await + a multi-threaded
    // dispatcher + CPU work gives parallelism. Same code, other context.

    // ── 9. TRY IT ────────────────────────────────────────────────────
    // a) Time sequential vs parallel with measureTime (lesson 09.5). Print
    //    Runtime.getRuntime().availableProcessors(). Is the speed-up close
    //    to your core count? (It is rarely exact.)
    // b) Try CHUNKS = 1, 2, 64, 100_000. When does it stop getting faster?
    // c) In parallelSumOfSquares, replace Dispatchers.Default with
    //    EmptyCoroutineContext. Same answer? Same speed?
}
