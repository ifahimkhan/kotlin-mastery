package l09_coroutines.lesson07_with_context

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.time.measureTime

/**
 * =====================================================================
 *  LESSON 09.7 — withContext: switch threads, get a result back
 * =====================================================================
 *  GOAL   : Move a piece of work to another dispatcher with withContext,
 *           return its result, and write "main-safe" suspend functions.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.lesson07_with_context.WithContextKt
 *  OUTPUT :
 *           inside IO: on a worker thread = true
 *           report = REPORT(3 lines)
 *           back on the caller's thread: true
 *           name outside = main-flow, inside = report-loader
 *           two withContext calls in a row took more than 1.5 reads: true
 *           two async calls took less than 2 reads: true
 *           the async version was faster: true
 *  NEXT   : lesson08_structured_concurrency/StructuredConcurrency.kt
 * =====================================================================
 */

private const val WORKER_PREFIX = "DefaultDispatcher-worker"
// Generous timings so the comparisons below hold even on a slow, busy
// machine: each "read" takes 300 ms, and every check has >= 150 ms slack.
private const val READ_MILLIS = 300L
private const val TWO_READS_MILLIS = 2 * READ_MILLIS
private const val ONE_AND_A_HALF_READS_MILLIS = READ_MILLIS * 3 / 2

// A "main-safe" function: callers can call it from ANY thread (even a UI
// thread), because the function itself moves the blocking part to IO.
suspend fun loadReport(): String =
    withContext(Dispatchers.IO) {
        println("inside IO: on a worker thread = ${Thread.currentThread().name.startsWith(WORKER_PREFIX)}")
        Thread.sleep(READ_MILLIS) // pretend: a blocking file read (OK on IO, lesson 09.6)
        "REPORT(3 lines)" // the last expression is returned
    }

suspend fun readFileBlocking(): Int =
    withContext(Dispatchers.IO) {
        Thread.sleep(READ_MILLIS)
        1
    }

fun main() = runBlocking(CoroutineName("main-flow")) {
    // ── 1. CONCEPT: switch, run, switch back ─────────────────────────
    // withContext suspends the caller, runs the block in the new context,
    // and RESUMES THE CALLER IN ITS OWN CONTEXT with the block's result.
    val callerThread = Thread.currentThread()
    val report = loadReport()
    println("report = $report")
    println("back on the caller's thread: ${Thread.currentThread() === callerThread}") // === : the same Thread object

    // ── 2. CONCEPT: change more than the thread ──────────────────────
    // withContext accepts any context element. Here: a debug name.
    val outsideName = currentCoroutineContext()[CoroutineName]?.name
    val insideName = withContext(CoroutineName("report-loader")) { currentCoroutineContext()[CoroutineName]?.name }
    println("name outside = $outsideName, inside = $insideName")

    // ── 3. PITFALL: withContext is NOT concurrent ────────────────────
    // It waits for its block before continuing, so two calls in a row
    // take 300 + 300 ms. (We compare with 1.5 reads, not exactly 2, because
    // timers are not perfectly precise.)
    val sequentialTime =
        measureTime {
            readFileBlocking()
            readFileBlocking()
        }
    println(
        "two withContext calls in a row took more than 1.5 reads: " +
            "${sequentialTime.inWholeMilliseconds > ONE_AND_A_HALF_READS_MILLIS}",
    )

    // For concurrency, combine it with async (lesson 09.5):
    val concurrentTime =
        measureTime {
            val first = async { readFileBlocking() }
            val second = async { readFileBlocking() }
            first.await() + second.await()
        }
    println("two async calls took less than 2 reads: ${concurrentTime.inWholeMilliseconds < TWO_READS_MILLIS}")
    println("the async version was faster: ${concurrentTime < sequentialTime}")

    // ── 4. WHY "main-safe" functions? ────────────────────────────────
    // The function that does the blocking work knows it blocks, so IT
    // should choose the dispatcher. Callers then never have to think about
    // threads: they just call `loadReport()`. Android's guidelines say the
    // same: suspend functions should be safe to call from the main thread.

    // ── 5. TRY IT ────────────────────────────────────────────────────
    // a) Change loadReport to use Dispatchers.Default. Is it still OK?
    //    (Default has fewer threads; blocking them hurts CPU work.)
    // b) Return a Pair from the withContext block.
    // c) Nest withContext(Dispatchers.Default) inside withContext(IO).
}
