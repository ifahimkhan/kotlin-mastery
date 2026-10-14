package l09_coroutines.lesson06_dispatchers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.time.measureTime

/**
 * =====================================================================
 *  LESSON 09.6 — Dispatchers: which thread runs my coroutine?
 * =====================================================================
 *  GOAL   : Choose a dispatcher (Default, IO, Main, Unconfined), limit
 *           parallelism, and see why blocking calls like Thread.sleep
 *           ruin concurrency.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.lesson06_dispatchers.DispatchersKt
 *  OUTPUT :
 *           Default runs on a worker thread: true
 *           IO runs on a worker thread: true
 *           Main on a plain JVM -> IllegalStateException
 *           Unconfined starts on the caller's thread: true
 *           Unconfined after delay is still on the caller's thread: false
 *           8 tasks with Thread.sleep on 2 threads took more than 3 rounds: true
 *           8 tasks with delay on 2 threads took less than 3 rounds: true
 *  NEXT   : lesson07_with_context/WithContext.kt
 * =====================================================================
 */

private const val WORKER_PREFIX = "DefaultDispatcher-worker"
private const val TASK_COUNT = 8
private const val THREADS = 2
private const val TASK_MILLIS = 100L
private const val SHORT_DELAY = 10L

// If every task BLOCKS its thread for 100 ms, 8 tasks on 2 threads need
// 8 / 2 = 4 "rounds" of 100 ms = about 400 ms. We compare with 3 rounds,
// not exactly 4: timers are not perfectly precise (Thread.sleep may wake
// a millisecond early on some systems), so leave a safety margin.
private const val SAFE_ROUNDS = 3
private const val THREE_ROUNDS_MILLIS = SAFE_ROUNDS * TASK_MILLIS

private fun onWorkerThread(): Boolean = Thread.currentThread().name.startsWith(WORKER_PREFIX)

fun main() = runBlocking {
    // ── 1. CONCEPT: the built-in dispatchers ─────────────────────────
    // A dispatcher decides which thread(s) a coroutine runs on.
    // | Dispatcher   | Threads                         | Use it for                |
    // |--------------|---------------------------------|---------------------------|
    // | Default      | one per CPU core (at least 2)   | CPU work: sorting, parsing|
    // | IO           | 64 or #cores, whichever is more | blocking I/O: files, JDBC |
    //                  (IO shares threads with Default's pool)
    // | Main         | the UI thread (Android/desktop) | updating the screen       |
    // | Unconfined   | whatever thread resumes it      | rare, special cases only  |
    // runBlocking itself uses the thread that called it.
    launch(Dispatchers.Default) {
        println("Default runs on a worker thread: ${onWorkerThread()}")
    }.join()
    launch(Dispatchers.IO) {
        println("IO runs on a worker thread: ${onWorkerThread()}") // same pool, other rules
    }.join()

    // Dispatchers.Main only exists when a UI library (Android, Swing,
    // JavaFX) provides it. On a plain JVM, using it fails:
    try { // (try/catch: later level)
        withContext(Dispatchers.Main) { println("never printed") }
    } catch (e: IllegalStateException) {
        println("Main on a plain JVM -> ${e::class.simpleName}")
    }

    // ── 2. CONCEPT: Unconfined (just so you recognise it) ────────────
    // It starts on the caller's thread, but after a suspension it
    // continues on WHATEVER thread resumed it. Hard to reason about.
    // We compare Thread objects with === (same thread?), not their names:
    // in debug mode, names change to e.g. "main @coroutine#3".
    val callerThread = Thread.currentThread()
    launch(Dispatchers.Unconfined) {
        println("Unconfined starts on the caller's thread: ${Thread.currentThread() === callerThread}")
        delay(SHORT_DELAY)
        println("Unconfined after delay is still on the caller's thread: ${Thread.currentThread() === callerThread}")
    }.join()

    // ── 3. CONCEPT: limitedParallelism ───────────────────────────────
    // A "view" of Default where at most N tasks RUN at the same time, on
    // at most N threads. It limits running work (blocking calls, CPU work),
    // NOT suspended coroutines: a coroutine waiting in delay() gives its
    // thread back, so section 5 below has all 8 coroutines waiting at once.
    // - Cap blocking calls (e.g. 2 JDBC queries at once):
    //   Dispatchers.IO.limitedParallelism(2)
    // - Cap SUSPENDING calls in flight (e.g. 3 downloads at once): use a
    //   Semaphore instead, see practice/downloader/Downloader.kt.
    val twoThreads = Dispatchers.Default.limitedParallelism(THREADS)

    // ── 4. PITFALL: Thread.sleep in a coroutine (blocking) ───────────
    // This is the classic mistake: Thread.sleep holds the thread, so only
    // 2 tasks can make progress at a time -> 4 "rounds" of 100 ms.
    val blockingTime =
        measureTime {
            List(TASK_COUNT) { launch(twoThreads) { Thread.sleep(TASK_MILLIS) } }.forEach { it.join() }
        }
    println(
        "$TASK_COUNT tasks with Thread.sleep on $THREADS threads took more than 3 rounds: " +
            "${blockingTime.inWholeMilliseconds >= THREE_ROUNDS_MILLIS}",
    )

    // ── 5. FIX: delay suspends and frees the thread ──────────────────
    // While one task waits, the thread runs the next one. All 8 wait at
    // the same time: about 1 round (100 ms) in total.
    val suspendingTime =
        measureTime {
            List(TASK_COUNT) { launch(twoThreads) { delay(TASK_MILLIS) } }.forEach { it.join() }
        }
    println(
        "$TASK_COUNT tasks with delay on $THREADS threads took less than 3 rounds: " +
            "${suspendingTime.inWholeMilliseconds < THREE_ROUNDS_MILLIS}",
    )

    // ── 6. WHY: what if I MUST call blocking code? ───────────────────
    // Some libraries only offer blocking calls (JDBC, old file APIs).
    // Run them on Dispatchers.IO: it has many threads, so blocking a few
    // doesn't starve everyone else. Lesson 09.7 shows withContext(IO).

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Change THREADS to 8. How long does the Thread.sleep version take?
    // b) Print Thread.currentThread().name inside the tasks. How many
    //    different names do you see? (They vary run to run.) Run with the
    //    JVM option -Dkotlinx.coroutines.debug to see "@coroutine#N" added.
    // c) Use Dispatchers.IO instead of twoThreads in section 4.
}
