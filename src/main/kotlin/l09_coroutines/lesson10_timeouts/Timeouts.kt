package l09_coroutines.lesson10_timeouts

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

/**
 * =====================================================================
 *  LESSON 09.10 — Timeouts
 * =====================================================================
 *  GOAL   : Put a time limit on slow work with withTimeout and
 *           withTimeoutOrNull, and fall back to a default value.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.lesson10_timeouts.TimeoutsKt
 *  OUTPUT :
 *           slow job step 0
 *           slow job step 1
 *           slow job step 2
 *           withTimeoutOrNull -> null
 *           fast server -> price from fast-server
 *           slow server -> cached price
 *           withTimeout threw TimeoutCancellationException
 *  NEXT   : lesson11_exceptions/Exceptions.kt
 * =====================================================================
 */

private const val STEP_MS = 200L
private const val LIMIT_MS = 500L // steps land at 0, 200, 400: each is 100 ms clear of the limit
private const val FAST_SERVER_MS = 50L
private const val SLOW_SERVER_MS = 1_000L

/** A pretend network call that takes [latencyMs] to answer. */
suspend fun fetchPrice(server: String, latencyMs: Long): String {
    delay(latencyMs)
    return "price from $server"
}

fun main(): Unit = runBlocking { // `: Unit` because the last expression below is a try
    // ── 1. CONCEPT: withTimeoutOrNull ────────────────────────────────
    // Runs the block; if it takes longer than the limit, the block is
    // CANCELLED and you get null instead of a result.
    val result = withTimeoutOrNull(LIMIT_MS) {
        repeat(10) { i ->
            println("slow job step $i")
            delay(STEP_MS)
        }
        "finished" // never reached
    }
    println("withTimeoutOrNull -> $result")

    val fast = withTimeoutOrNull(LIMIT_MS) { fetchPrice("fast-server", FAST_SERVER_MS) }
    println("fast server -> $fast")

    // ── 2. PRACTICAL: a fallback with ?: ─────────────────────────────
    val price = withTimeoutOrNull(LIMIT_MS) { fetchPrice("slow-server", SLOW_SERVER_MS) }
        ?: "cached price"
    println("slow server -> $price")

    // ── 3. CONCEPT: withTimeout throws ───────────────────────────────
    // withTimeout throws TimeoutCancellationException instead of
    // returning null. (try/catch/finally are explained in lesson11_exceptions.)
    try {
        withTimeout(LIMIT_MS) { fetchPrice("slow-server", SLOW_SERVER_MS) }
    } catch (e: TimeoutCancellationException) {
        println("withTimeout threw ${e::class.simpleName}")
    }

    // ── 4. PITFALL: the timeout exception is a CancellationException ─
    // TimeoutCancellationException extends CancellationException. If it
    // escapes from a launch, the coroutine just counts as CANCELLED:
    // no crash, no log, nothing. Prefer withTimeoutOrNull, or catch it
    // right where you call withTimeout.

    // ── 5. PITFALL: leaking resources ────────────────────────────────
    // The timeout can strike right AFTER the block opened a file or a
    // connection but BEFORE it returned it to you. Open resources outside
    // the timeout block and close them in `finally`:
    //   val connection = openConnection()
    //   try { withTimeout(LIMIT_MS) { connection.read() } }
    //   finally { connection.close() }

    // ── 6. WHY timeouts? ─────────────────────────────────────────────
    // A network call without a limit can hang forever and keep its
    // coroutine (and memory) alive. Every remote call deserves a limit.

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Change LIMIT_MS to 1_200. What does the slow server return now?
    // b) Write a function that asks two servers in turn and returns the
    //    first answer that arrives within 200 ms, or "unavailable".
}
