package l06_collections.lesson07_aggregation

/**
 * =====================================================================
 *  LESSON 06.7 — Aggregation and grouping
 * =====================================================================
 *  GOAL   : Reduce a collection to one value (sum, max, fold) and split
 *           it into groups, chunks and windows.
 *  RUN    : ./gradlew -q run -Plesson=l06_collections.lesson07_aggregation.AggregationKt
 *  OUTPUT :
 *           sum=1590, average=318.0, min=99, max=899
 *           sumOf price*qty = 3541
 *           maxBy price -> Item(name=headphones, price=899, qty=1)
 *           minByOrNull on empty -> null
 *           reduce -> 120 (1*2*3*4*5)
 *           fold   -> 120 (starts from 1)
 *           fold on empty list  -> 0
 *           reduce on empty list -> crashed with UnsupportedOperationException
 *           running total (runningFold) -> [0, 1, 3, 6, 10]
 *           running total (runningReduce) -> [1, 3, 6, 10]
 *           groupBy length -> {3=[ant, cow], 4=[bear, lion], 5=[horse]}
 *           eachCount      -> {a=3, b=1, c=2}
 *           chunked(2)  -> [[1, 2], [3, 4], [5]]
 *           windowed(3) -> [[1, 2, 3], [2, 3, 4], [3, 4, 5]]
 *           3-day averages -> [11.0, 12.0, 14.0]
 *  NEXT   : lesson08_sorting/Sorting.kt
 * =====================================================================
 */

// A data class gets a readable toString() for free (level 05).
data class Item(val name: String, val price: Int, val qty: Int)

fun main() {
    // ── 1. CONCEPT: built-in aggregates ──────────────────────────────
    val prices = listOf(250, 99, 899, 120, 222)
    println("sum=${prices.sum()}, average=${prices.average()}, min=${prices.min()}, max=${prices.max()}")

    // ...By / ...Of versions take a lambda to choose WHAT to measure.
    val cart =
        listOf(
            Item("pen", 120, 3),
            Item("headphones", 899, 1),
            Item("notebook", 250, 2),
            Item("stickers", 99, 18),
        )
    println("sumOf price*qty = ${cart.sumOf { it.price * it.qty }}")
    println("maxBy price -> ${cart.maxBy { it.price }}")
    println("minByOrNull on empty -> ${emptyList<Item>().minByOrNull { it.price }}")

    // ── 2. CONCEPT: reduce vs fold ───────────────────────────────────
    // reduce: starts with the FIRST item as the accumulator.
    // fold  : starts with a value YOU give. Safer and more flexible.
    val numbers = listOf(1, 2, 3, 4, 5)
    println("reduce -> ${numbers.reduce { acc, n -> acc * n }} (1*2*3*4*5)")
    println("fold   -> ${numbers.fold(1) { acc, n -> acc * n }} (starts from 1)")

    // PITFALL: reduce has nothing to start from on an empty list.
    val none = emptyList<Int>()
    println("fold on empty list  -> ${none.fold(0) { acc, n -> acc + n }}")
    try { // (try/catch: later level) — catches the crash so the lesson continues
        none.reduce { acc, n -> acc + n }
    } catch (e: UnsupportedOperationException) {
        println("reduce on empty list -> crashed with ${e::class.simpleName}")
    }
    // runningFold keeps every intermediate value.
    println("running total (runningFold) -> ${listOf(1, 2, 3, 4).runningFold(0) { acc, n -> acc + n }}")
    // runningReduce is the same without the extra start value (like reduce vs fold).
    println("running total (runningReduce) -> ${listOf(1, 2, 3, 4).runningReduce { acc, n -> acc + n }}")

    // ── 3. CONCEPT: grouping ─────────────────────────────────────────
    // groupBy: key -> list of ALL items with that key.
    val animals = listOf("ant", "bear", "cow", "horse", "lion")
    println("groupBy length -> ${animals.groupBy { it.length }}")
    // groupingBy().eachCount(): key -> how many items have that key.
    println("eachCount      -> ${"abacca".groupingBy { it }.eachCount()}")

    // ── 4. CONCEPT: chunked and windowed ─────────────────────────────
    // chunked : cut into pieces that DON'T overlap (pages, batches).
    // windowed: a sliding window that DOES overlap (moving averages).
    println("chunked(2)  -> ${numbers.chunked(2)}")
    println("windowed(3) -> ${numbers.windowed(3)}")
    val temperatures = listOf(10, 11, 12, 13, 17)
    println("3-day averages -> ${temperatures.windowed(3) { it.average() }}")

    // ── 5. WHY ───────────────────────────────────────────────────────
    // Reports, statistics and dashboards are mostly "group, then
    // aggregate". These functions turn 15-line loops into one line.

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Find the longest word in listOf("a", "abc", "ab") with maxBy.
    // b) Count how many times each word appears in "a b a c b a".
    // c) Use fold to build the string "1-2-3" from listOf(1, 2, 3).
}
