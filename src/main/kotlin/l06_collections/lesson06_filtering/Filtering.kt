package l06_collections.lesson06_filtering

/**
 * =====================================================================
 *  LESSON 06.6 — Filtering and predicates
 * =====================================================================
 *  GOAL   : Pick the items you want, test conditions on a whole
 *           collection, and find single items safely.
 *  RUN    : ./gradlew -q run -Plesson=l06_collections.lesson06_filtering.FilteringKt
 *  OUTPUT :
 *           filter    -> [72, 95, 88]
 *           filterNot -> [45, 59]
 *           filterIsInstance<String> -> [a, b]
 *           partition -> passed=[72, 95, 88], failed=[45, 59]
 *           any >= 90: true, all >= 40: true, none > 100: true
 *           count >= 70: 3
 *           first >= 80: 95, firstOrNull > 100: null, find { it < 50 }: 45
 *           take(2)=[72, 45], drop(3)=[88, 59], takeWhile(> 50)=[72]
 *           empty list: any=false, all=true, none=true
 *           first on empty list -> crashed with NoSuchElementException
 *  NEXT   : lesson07_aggregation/Aggregation.kt
 * =====================================================================
 */
fun main() {
    val scores = listOf(72, 45, 95, 88, 59)

    // ── 1. CONCEPT: filter family ────────────────────────────────────
    // The lambda is a PREDICATE: it returns true (keep) or false (skip).
    println("filter    -> ${scores.filter { it >= 60 }}")
    println("filterNot -> ${scores.filterNot { it >= 60 }}")
    val mixed: List<Any> = listOf(1, "a", 2.5, "b")
    println("filterIsInstance<String> -> ${mixed.filterIsInstance<String>()}")

    // partition splits into a Pair: (matching, not matching).
    val (passed, failed) = scores.partition { it >= 60 }
    println("partition -> passed=$passed, failed=$failed")

    // ── 2. CONCEPT: questions about the whole collection ─────────────
    println(
        "any >= 90: ${scores.any { it >= 90 }}, " +
            "all >= 40: ${scores.all { it >= 40 }}, " +
            "none > 100: ${scores.none { it > 100 }}",
    )
    println("count >= 70: ${scores.count { it >= 70 }}")

    // ── 3. CONCEPT: finding one item ─────────────────────────────────
    // first { } throws if nothing matches; firstOrNull / find return null.
    println(
        "first >= 80: ${scores.first { it >= 80 }}, " +
            "firstOrNull > 100: ${scores.firstOrNull { it > 100 }}, " +
            "find { it < 50 }: ${scores.find { it < 50 }}",
    )

    // ── 4. CONCEPT: slicing ──────────────────────────────────────────
    // takeWhile stops at the FIRST item that fails (45), unlike filter.
    println("take(2)=${scores.take(2)}, drop(3)=${scores.drop(3)}, takeWhile(> 50)=${scores.takeWhile { it > 50 }}")

    // ── 5. PITFALL: empty collections ────────────────────────────────
    // all { } is TRUE for an empty list ("no item breaks the rule").
    val empty = emptyList<Int>()
    println("empty list: any=${empty.any { it > 0 }}, all=${empty.all { it > 0 }}, none=${empty.none { it > 0 }}")
    try { // (try/catch: later level) — catches the crash so the lesson continues
        empty.first()
    } catch (e: NoSuchElementException) {
        println("first on empty list -> crashed with ${e::class.simpleName}")
    }
    // Fix: firstOrNull() and decide what null means (level 04).

    // ── 6. WHY ───────────────────────────────────────────────────────
    // Each name says exactly what it does. `scores.any { it >= 90 }` is
    // easier to read than a loop with a Boolean flag and a break.

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) From listOf("kotlin", "", "java", " ") keep only non-blank strings.
    // b) Are all words in listOf("apple", "avocado") starting with 'a'?
    // c) Split 1..20 into evens and odds with partition.
}
