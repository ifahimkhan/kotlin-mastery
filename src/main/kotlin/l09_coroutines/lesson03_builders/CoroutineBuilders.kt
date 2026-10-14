package l09_coroutines.lesson03_builders

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * =====================================================================
 *  LESSON 09.3 — Coroutine builders: runBlocking, launch, async
 * =====================================================================
 *  GOAL   : Start coroutines with the three main builders, know what
 *           each returns, and when NOT to use runBlocking.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.lesson03_builders.CoroutineBuildersKt
 *  OUTPUT :
 *           1. runBlocking started
 *           2. launch returned a Job immediately
 *           3. launch body runs once main suspends
 *           4. async returned a Deferred immediately
 *           5. await -> 42
 *           6. coroutineScope result -> 30
 *           7. runBlocking is about to return
 *  NEXT   : lesson04_job_lifecycle/JobLifecycle.kt
 * =====================================================================
 */

private const val SHORT_DELAY = 50L

// coroutineScope { } is a builder for INSIDE suspend functions: it creates
// a child scope, so you can launch/async there, and it waits for all of
// them before returning. (Structured concurrency, lesson 09.8.)
suspend fun addInParallel(a: Int, b: Int): Int =
    coroutineScope {
        val first = async { delay(SHORT_DELAY); a }
        val second = async { delay(SHORT_DELAY); b }
        first.await() + second.await()
    }

// runBlocking turns `main` into a coroutine. Its block runs on the
// calling thread, and runBlocking returns only when everything inside
// (including launched children) has finished.
fun main() = runBlocking {
    println("1. runBlocking started")

    // ── 1. CONCEPT: launch = "start this and don't give me a result" ──
    // launch returns a Job (a handle to wait for or cancel the work).
    // The body does NOT run yet: it's queued until this thread is free.
    val job: Job = launch { println("3. launch body runs once main suspends") }
    println("2. launch returned a Job immediately")
    job.join() // suspends main until the job is finished -> the body runs now

    // ── 2. CONCEPT: async = "start this and give me a result later" ──
    // async returns a Deferred<T>: a Job that will hold a value.
    val answer: Deferred<Int> =
        async {
            delay(SHORT_DELAY)
            21 * 2
        }
    println("4. async returned a Deferred immediately")
    println("5. await -> ${answer.await()}") // suspends until the value is ready

    // ── 3. CONCEPT: coroutineScope inside suspend functions ──────────
    println("6. coroutineScope result -> ${addInParallel(10, 20)}")

    // ── 4. WHEN to use which ─────────────────────────────────────────
    // | Builder        | Returns     | Use it for                          |
    // |----------------|-------------|-------------------------------------|
    // | runBlocking    | the result  | main() and small scripts ONLY       |
    // | launch         | Job         | work with no result (save, log)     |
    // | async          | Deferred<T> | work that produces a value          |
    // | coroutineScope | the result  | starting children in a suspend fun  |

    // ── 5. PITFALL: runBlocking everywhere ───────────────────────────
    // runBlocking BLOCKS the thread that calls it until all work is done.
    // - Calling it inside a coroutine wastes a thread and can deadlock.
    // - Calling it on an Android/desktop UI thread freezes the app.
    // Use it only as the bridge from normal code (main, small scripts)
    // into coroutines. Tests use runTest instead (lesson16_testing), and
    // real apps get scopes from their framework.

    // ── 6. PITFALL: launch and async need a scope ────────────────────
    //   suspend fun noScope() { launch { println("x") } }
    //   ❌ ... 'launch' can not be called without the corresponding coroutine scope. Consider wrapping
    //      'launch' in 'coroutineScope { }', using 'runBlocking { }', or using some other 'CoroutineScope'.

    println("7. runBlocking is about to return")

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Remove job.join(). Where does line 3 appear now? Why?
    // b) Make addInParallel take three numbers.
    // c) Use async without ever calling await. Does the work still run?
}
