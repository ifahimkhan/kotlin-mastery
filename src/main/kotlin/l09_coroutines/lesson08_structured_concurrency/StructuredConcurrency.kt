package l09_coroutines.lesson08_structured_concurrency

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

/**
 * =====================================================================
 *  LESSON 09.8 — Structured concurrency
 * =====================================================================
 *  GOAL   : Understand the parent/child rules that make coroutines safe:
 *           parents wait for children, a failing child cancels its
 *           siblings, and supervisorScope stops that from spreading.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.lesson08_structured_concurrency.StructuredConcurrencyKt
 *  OUTPUT :
 *           parent body finished
 *           child 2 done
 *           child 1 done
 *           parent completed (after its children)
 *           sibling cancelled, cleaning up
 *           coroutineScope failed -> IllegalStateException: upload failed
 *           handler caught: upload failed
 *           sibling finished normally
 *           supervisorScope completed
 *  NEXT   : lesson09_cancellation/Cancellation.kt
 * =====================================================================
 */

private const val CHILD_1_MILLIS = 100L
private const val CHILD_2_MILLIS = 50L
private const val FAIL_AFTER_MILLIS = 50L
private const val SIBLING_MILLIS = 100L

fun main() = runBlocking {
    // ── 1. RULE 1: a parent waits for all its children ───────────────
    // The parent's own code ends first, but the parent isn't "completed"
    // until every child is done. No coroutine gets lost or forgotten.
    val parent =
        launch {
            launch {
                delay(CHILD_1_MILLIS)
                println("child 1 done")
            }
            launch {
                delay(CHILD_2_MILLIS)
                println("child 2 done")
            }
            println("parent body finished")
        }
    parent.join()
    println("parent completed (after its children)")

    // ── 2. RULE 2: a failing child cancels the parent and its siblings ─
    // If one part of a task fails, the rest of the task is pointless:
    // coroutineScope cancels everything and rethrows the error.
    try { // (try/catch: later level)
        coroutineScope {
            launch {
                delay(FAIL_AFTER_MILLIS)
                throw IllegalStateException("upload failed")
            }
            launch {
                try {
                    delay(SIBLING_MILLIS)
                    println("never printed: the sibling was cancelled")
                } finally {
                    // `finally` runs even when cancelled: the place to clean up.
                    println("sibling cancelled, cleaning up")
                }
            }
        }
    } catch (e: IllegalStateException) {
        println("coroutineScope failed -> ${e::class.simpleName}: ${e.message}")
    }

    // ── 3. PREVIEW: supervisorScope keeps siblings alive ─────────────
    // In a supervisorScope, a failing child does NOT cancel the others.
    // Its error goes to a CoroutineExceptionHandler instead. (Details in
    // lesson11_exceptions.)
    val handler = CoroutineExceptionHandler { _, error -> println("handler caught: ${error.message}") }
    supervisorScope {
        launch(handler) {
            delay(FAIL_AFTER_MILLIS)
            throw IllegalStateException("upload failed")
        }
        launch {
            delay(SIBLING_MILLIS)
            println("sibling finished normally")
        }
    }
    println("supervisorScope completed")

    // ── 4. WHY "structured"? ─────────────────────────────────────────
    // Like code blocks { }, coroutines live inside a scope. When the scope
    // ends, all its coroutines are finished or cancelled. So:
    //  - no coroutine keeps running forgotten in the background ("leak"),
    //  - errors are never silently lost,
    //  - cancelling a screen/request cancels all of its work at once.

    // ── 5. PITFALL: GlobalScope breaks the structure ─────────────────
    //   GlobalScope.launch { uploadPhotos() }
    //   ⚠️ This is a delicate API and its use requires care. ...
    // A GlobalScope coroutine has NO parent: nobody waits for it, nobody
    // cancels it, and if the app moves on, it keeps running. Use a scope
    // you own instead (runBlocking, coroutineScope, or a framework scope
    // like Android's viewModelScope).

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Remove parent.join(). Does "parent completed" still come last? Why?
    // b) In section 2, swap coroutineScope for supervisorScope. What changes?
    // c) Make child 1 fail in section 1 (throw an exception). What happens
    //    to child 2? Expect the whole program to fail: the failure cancels
    //    the parent too. Read the stack trace to see which coroutine failed.
}
