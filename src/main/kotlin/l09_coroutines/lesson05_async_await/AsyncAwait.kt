package l09_coroutines.lesson05_async_await

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.measureTimedValue

/**
 * =====================================================================
 *  LESSON 09.5 — async and await: doing things at the same time
 * =====================================================================
 *  GOAL   : Run independent suspend calls concurrently with async,
 *           collect many results with awaitAll, and avoid the
 *           "lazy async" trap that makes code sequential again.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.lesson05_async_await.AsyncAwaitKt
 *  OUTPUT :
 *           sequential: Ada has 3 orders
 *           sequential took at least 500 ms: true
 *           concurrent: Ada has 3 orders
 *           concurrent took less than 500 ms: true
 *           all prices: [10, 20, 30], total = 60
 *           awaitAll took less than 300 ms: true
 *           lazy + await one by one took at least 500 ms: true
 *  NEXT   : lesson06_dispatchers/Dispatchers.kt
 * =====================================================================
 */

private const val USER_MILLIS = 300L
private const val ORDERS_MILLIS = 200L
private const val PRICE_MILLIS = 100L
private const val SEQUENTIAL_MILLIS = USER_MILLIS + ORDERS_MILLIS // 500 ms
private const val ALL_PRICES_SEQUENTIAL_MILLIS = 3 * PRICE_MILLIS // 300 ms

suspend fun loadUser(): String {
    delay(USER_MILLIS)
    return "Ada"
}

suspend fun loadOrderCount(): Int {
    delay(ORDERS_MILLIS)
    return 3
}

suspend fun loadPrice(id: Int): Int {
    delay(PRICE_MILLIS)
    return id * 10
}

fun main() = runBlocking {
    // Timings vary a little from machine to machine, so this lesson
    // prints comparisons ("at least", "less than") instead of exact times.

    // ── 1. SEQUENTIAL: one after the other ───────────────────────────
    // The two calls don't depend on each other, but we wait for the
    // first before starting the second: 300 + 200 = 500 ms.
    val (sequentialText, sequentialTime) =
        measureTimedValue {
            val user = loadUser()
            val orders = loadOrderCount()
            "$user has $orders orders"
        }
    println("sequential: $sequentialText")
    println("sequential took at least 500 ms: ${sequentialTime.inWholeMilliseconds >= SEQUENTIAL_MILLIS}")

    // ── 2. CONCURRENT: start both, then wait for both ────────────────
    // async starts each call right away. Both wait at the same time, so
    // the total is the LONGEST call (about 300 ms), not the sum.
    val (concurrentText, concurrentTime) =
        measureTimedValue {
            val user = async { loadUser() }
            val orders = async { loadOrderCount() }
            "${user.await()} has ${orders.await()} orders"
        }
    println("concurrent: $concurrentText")
    println("concurrent took less than 500 ms: ${concurrentTime.inWholeMilliseconds < SEQUENTIAL_MILLIS}")

    // ── 3. CONCEPT: many results with awaitAll ───────────────────────
    // map to a list of Deferreds, then awaitAll() waits for all of them
    // and returns the results IN THE SAME ORDER as the list.
    val (prices, pricesTime) =
        measureTimedValue {
            listOf(1, 2, 3)
                .map { id -> async { loadPrice(id) } }
                .awaitAll()
        }
    println("all prices: $prices, total = ${prices.sum()}")
    println("awaitAll took less than 300 ms: ${pricesTime.inWholeMilliseconds < ALL_PRICES_SEQUENTIAL_MILLIS}")

    // ── 4. PITFALL: lazy async awaited one by one ────────────────────
    // With CoroutineStart.LAZY, the work starts only at await(). Awaiting
    // them in a row starts them in a row: sequential again!
    val (_, lazyTime) =
        measureTimedValue {
            val user = async(start = CoroutineStart.LAZY) { loadUser() }
            val orders = async(start = CoroutineStart.LAZY) { loadOrderCount() }
            "${user.await()} has ${orders.await()} orders"
        }
    println("lazy + await one by one took at least 500 ms: ${lazyTime.inWholeMilliseconds >= SEQUENTIAL_MILLIS}")
    // Fix: call start() on both first, or simply don't use LAZY.

    // ── 5. WHY not always async? ─────────────────────────────────────
    // Use async only for INDEPENDENT work. If step 2 needs the result of
    // step 1 (load user, then load THAT user's orders), it must be
    // sequential — and plain sequential code says that most clearly.

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Add a third independent call (loadAddress) to section 2.
    // b) In section 3, load 10 prices. How long does it take now?
    // c) Fix section 4 with start() and check the time.
}
