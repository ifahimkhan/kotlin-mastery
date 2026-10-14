package l09_coroutines.lesson11_exceptions

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

/**
 * =====================================================================
 *  LESSON 09.11 — Exceptions: try/catch/finally and coroutines
 * =====================================================================
 *  GOAL   : Handle errors with try/catch/finally, then learn where an
 *           exception goes in a coroutine: launch vs async, parent vs
 *           child, coroutineScope vs supervisorScope, and handlers.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.lesson11_exceptions.ExceptionsKt
 *  OUTPUT :
 *           caught NumberFormatException
 *           finally always runs
 *           port = 8080
 *           download: cancelled, cleaning up and rethrowing
 *           download.isCancelled = true
 *           sibling was cancelled
 *           coroutineScope rethrew: payment service is down
 *           await() rethrew: division by zero
 *           root handler got: failure in a child
 *           root handler got: thumbnail A failed
 *           thumbnail B finished anyway
 *           crash matrix:
 *             launch child fails  -> parent and siblings cancelled, scope rethrows
 *             async child fails   -> await() rethrows (and a normal scope is cancelled)
 *             root launch fails   -> CoroutineExceptionHandler, else printed to stderr
 *             supervisor child    -> only that child fails, siblings continue
 *  NEXT   : lesson12_context/CoroutineContextBasics.kt
 * =====================================================================
 */

private const val SHORT_MS = 50L
private const val LONG_MS = 100L
private const val NEVER_MS = 10_000L

fun parsePort(text: String): Int = text.toInt()

fun main() = runBlocking {
    // ── 1. CONCEPT: try / catch / finally ────────────────────────────
    // try    : run code that may throw.
    // catch  : runs only if a matching exception was thrown.
    // finally: ALWAYS runs, error or not. Use it for cleanup.
    try {
        println("port = ${parsePort("80x")}")
    } catch (e: NumberFormatException) {
        println("caught ${e::class.simpleName}")
    } finally {
        println("finally always runs")
    }

    // `try` is an expression, like `if` and `when`:
    val port = try { parsePort("8080") } catch (e: NumberFormatException) { -1 }
    println("port = $port")
    // Catch the most specific type you can. `catch (e: Exception)` also
    // catches bugs you never meant to hide (and CancellationException,
    // see lesson09_cancellation).

    // ── 2. RULE: never swallow CancellationException ─────────────────
    // Cancellation travels as a CancellationException. If you catch it,
    // do your cleanup and THROW IT AGAIN, or the coroutine keeps running
    // as if nothing happened (see lesson09_cancellation, section 6).
    val download = launch {
        try {
            delay(NEVER_MS)
        } catch (e: CancellationException) {
            println("download: cancelled, cleaning up and rethrowing")
            throw e // keep the cancellation going
        }
    }
    delay(SHORT_MS)
    download.cancelAndJoin()
    println("download.isCancelled = ${download.isCancelled}")

    // ── 3. CONCEPT: launch -> the failure goes UP to the parent ──────
    // A failing child cancels its parent, which cancels every sibling.
    // coroutineScope then rethrows the failure to you.
    try {
        coroutineScope {
            launch {
                delay(SHORT_MS)
                throw IllegalStateException("payment service is down")
            }
            launch {
                try {
                    delay(NEVER_MS)
                    println("sibling finished") // never printed
                } finally {
                    println("sibling was cancelled")
                }
            }
        }
    } catch (e: IllegalStateException) {
        println("coroutineScope rethrew: ${e.message}")
    }

    // ── 4. CONCEPT: async -> the failure waits inside the Deferred ───
    // await() rethrows it. Here supervisorScope keeps the failure from
    // cancelling the scope (see section 6).
    supervisorScope {
        val total = async {
            delay(SHORT_MS)
            throw ArithmeticException("division by zero")
        }
        try {
            total.await()
        } catch (e: ArithmeticException) {
            println("await() rethrew: ${e.message}")
        }
    }
    // PITFALL: inside coroutineScope, a failing async ALSO cancels the
    // whole scope (like launch), even if you wrap await() in try/catch.

    // ── 5. CONCEPT: CoroutineExceptionHandler, for ROOT coroutines ───
    // An uncaught failure in a root launch goes to the handler in its
    // context (instead of crashing to the console). A handler on a CHILD
    // is ignored: the child hands the failure to its parent.
    val rootHandler = CoroutineExceptionHandler { _, e -> println("root handler got: ${e.message}") }
    val ignoredHandler = CoroutineExceptionHandler { _, e -> println("child handler got: ${e.message}") }
    val appScope = CoroutineScope(SupervisorJob() + rootHandler)
    val root = appScope.launch {
        launch(ignoredHandler) { // child: this handler is never used
            throw IllegalStateException("failure in a child")
        }
    }
    root.join()
    appScope.cancel() // always cancel scopes you create yourself
    // PITFALL: the handler does NOTHING for a root `async`. Its failure
    // stays inside the Deferred until someone calls await(); if nobody
    // does, the error is silently lost. Handle async errors at await().

    // ── 6. CONCEPT: supervisorScope - siblings survive ───────────────
    // Children of a supervisor are independent: one failing does not
    // cancel the others. Each child then needs its own handling.
    supervisorScope {
        launch(rootHandler) { // a supervisor's child acts like a root
            delay(SHORT_MS)
            throw IllegalStateException("thumbnail A failed")
        }
        launch {
            delay(LONG_MS)
            println("thumbnail B finished anyway")
        }
    }

    // PITFALL: launch(SupervisorJob()) { } or withContext(SupervisorJob())
    // does NOT give you supervisor behaviour. The new Job REPLACES the
    // parent's Job, so the coroutine has no parent at all: nobody waits
    // for it, nobody cancels it, structured concurrency is broken. Use
    // supervisorScope { } (above), or CoroutineScope(SupervisorJob()) for
    // a long-lived scope that you cancel yourself (section 5).

    // ── 7. SUMMARY: the crash matrix ─────────────────────────────────
    println("crash matrix:")
    println("  launch child fails  -> parent and siblings cancelled, scope rethrows")
    println("  async child fails   -> await() rethrows (and a normal scope is cancelled)")
    println("  root launch fails   -> CoroutineExceptionHandler, else printed to stderr")
    println("  supervisor child    -> only that child fails, siblings continue")

    // ── 8. WHY this design? ──────────────────────────────────────────
    // Structured concurrency (lesson08) promises: no failure is lost, and
    // no work keeps running after its result became useless. Propagating
    // failures upward keeps both promises. Use a supervisor only when
    // the children are truly independent (e.g. separate downloads).

    // ── 9. TRY IT ────────────────────────────────────────────────────
    // a) In section 6, replace supervisorScope with coroutineScope.
    //    What happens to the program?
    // b) In section 5, remove `+ rootHandler` from appScope. Where does
    //    the error message go now? (Look at the error output.)
}
