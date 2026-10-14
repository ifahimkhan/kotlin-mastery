package l09_coroutines.lesson01_threads_vs_coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * =====================================================================
 *  LESSON 09.1 — Why coroutines? Threads vs coroutines
 * =====================================================================
 *  GOAL   : See why a thread is expensive, why a coroutine is cheap, and
 *           what "suspending" means: pausing a task WITHOUT blocking
 *           the thread it runs on.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.lesson01_threads_vs_coroutines.ThreadsVsCoroutinesKt
 *  OUTPUT :
 *           500 threads finished: 500
 *           100000 coroutines finished: 100000
 *           A: step 1
 *           B: step 1
 *           A: step 2
 *           B: step 2
 *           Both tasks shared ONE thread: true
 *  NEXT   : lesson02_suspend/SuspendFunctions.kt
 * =====================================================================
 */

private const val THREAD_COUNT = 500
private const val COROUTINE_COUNT = 100_000
private const val WAIT_MILLIS = 100L

fun main() {
    // ── 1. CONCEPT: a thread is a heavy, real OS resource ────────────
    // Every thread gets its own stack (often around 1 MB of reserved
    // memory) and the operating system schedules it. A few thousand are
    // fine; a few hundred thousand would exhaust memory. That's why we
    // only start 500 here.
    val threadsDone = AtomicInteger(0) // a thread-safe counter (lesson13_shared_state)
    val threads =
        List(THREAD_COUNT) {
            thread {
                Thread.sleep(WAIT_MILLIS) // the thread does NOTHING but is still occupied
                threadsDone.incrementAndGet()
            }
        }
    threads.forEach { it.join() } // wait for every thread to finish
    println("$THREAD_COUNT threads finished: ${threadsDone.get()}")

    // ── 2. CONCEPT: a coroutine is a cheap task ──────────────────────
    // A coroutine is just an object that remembers "where was I?". While
    // it waits (delay), it doesn't hold any thread. 100_000 of them fit
    // easily — and they all run on the SINGLE thread of runBlocking.
    var coroutinesDone = 0 // safe: everything below runs on one thread
    runBlocking {
        repeat(COROUTINE_COUNT) {
            launch {
                delay(WAIT_MILLIS) // suspends: frees the thread for others
                coroutinesDone++
            }
        }
    } // runBlocking waits until all its coroutines are done (lesson 09.8)
    println("$COROUTINE_COUNT coroutines finished: $coroutinesDone")

    // ── 3. CONCEPT: suspension = pause without blocking ──────────────
    // Two tasks take turns on one thread. When A calls delay, it pauses
    // and the thread is free, so B can run. Then A continues, and so on.
    val threadsUsed = mutableSetOf<Thread>()
    runBlocking {
        launch {
            threadsUsed += Thread.currentThread()
            println("A: step 1")
            delay(WAIT_MILLIS) // A pauses at 0 ms, wakes up at 100 ms
            println("A: step 2")
        }
        launch {
            threadsUsed += Thread.currentThread()
            println("B: step 1")
            delay(WAIT_MILLIS + WAIT_MILLIS / 2) // B wakes up later, at 150 ms
            println("B: step 2")
        }
    }
    println("Both tasks shared ONE thread: ${threadsUsed.size == 1}")

    // ── 4. WHY this matters ──────────────────────────────────────────
    // Most programs spend their time WAITING: for the network, a file, a
    // database, a user. With threads, every waiting task wastes a thread.
    // With coroutines, waiting is free, so you can write simple
    // top-to-bottom code ("load user, then load orders") that still
    // handles thousands of tasks at once.

    // ── 5. PITFALL: coroutines are not magic speed ───────────────────
    // A coroutine doing heavy CPU work (no waiting) still needs a thread
    // the whole time. Coroutines shine for waiting, not for raw math.
    // Lesson 09.6 shows how to run CPU work on several threads.

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Change COROUTINE_COUNT to 1_000_000. Does it still work?
    // b) Replace delay(...) in section 3 with Thread.sleep(...). In which
    //    order are the lines printed now, and why? (lesson 09.2)
    // c) Give A and B the same delay. Is the order still A, B?
}
