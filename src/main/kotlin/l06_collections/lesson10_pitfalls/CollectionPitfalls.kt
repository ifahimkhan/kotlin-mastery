package l06_collections.lesson10_pitfalls

/**
 * =====================================================================
 *  LESSON 06.10 — Pitfalls and immutability
 * =====================================================================
 *  GOAL   : Avoid the classic collection bugs: read-only is not
 *           immutable, changing a list while looping over it, and
 *           sharing mutable lists.
 *  RUN    : ./gradlew -q run -Plesson=l06_collections.lesson10_pitfalls.CollectionPitfallsKt
 *  OUTPUT :
 *           readOnlyView before: [a, b]
 *           readOnlyView after someone casts it back: [a, b, sneaky]
 *           safeCopy stays: [a, b]
 *           removing inside for loop -> crashed with ConcurrentModificationException
 *           removeAll (in place) -> [1, 3, 5]
 *           filter (new list)    -> [1, 3, 5]
 *           base = [1, 2, 3]
 *           base + 4 = [1, 2, 3, 4], base - 2 = [1, 3], base still [1, 2, 3]
 *           team A after B's change: [Ann, Bob, Eve]
 *           team A with a copy:      [Ann, Bob]
 *  NEXT   : practice/grades/StudentGradeAnalytics.kt
 * =====================================================================
 */
fun main() {
    // ── 1. PITFALL: read-only is NOT immutable ───────────────────────
    // A List type only promises that YOU can't change it through this
    // variable. The object underneath may still be a MutableList.
    val backing = mutableListOf("a", "b")
    val readOnlyView: List<String> = backing
    val safeCopy: List<String> = backing.toList() // a real, separate copy
    println("readOnlyView before: $readOnlyView")
    // Someone could do this (never do it yourself!):
    (readOnlyView as MutableList<String>).add("sneaky")
    println("readOnlyView after someone casts it back: $readOnlyView")
    println("safeCopy stays: $safeCopy")
    // Fix: hand out copies (toList()) of data you must protect.

    // ── 2. PITFALL: changing a list while looping over it ────────────
    val numbers = mutableListOf(1, 2, 3, 4, 5)
    // This MAY crash, and with other data it silently SKIPS items instead
    // (e.g. removing the second-to-last element). Never rely on either.
    try { // (try/catch: later level) — catches the crash so the lesson continues
        for (n in numbers) {
            if (n % 2 == 0) numbers.remove(n)
        }
    } catch (e: ConcurrentModificationException) {
        println("removing inside for loop -> crashed with ${e::class.simpleName}")
    }
    // Fix 1: removeAll { } changes the list safely in place.
    val inPlace = mutableListOf(1, 2, 3, 4, 5)
    inPlace.removeAll { it % 2 == 0 }
    println("removeAll (in place) -> $inPlace")
    // Fix 2 (preferred): build a NEW list and leave the old one alone.
    val original = listOf(1, 2, 3, 4, 5)
    println("filter (new list)    -> ${original.filter { it % 2 != 0 }}")

    // ── 3. CONCEPT: copy-on-write with + and - ───────────────────────
    // On a read-only List, + and - return NEW lists. Nothing is mutated.
    val base = listOf(1, 2, 3)
    println("base = $base")
    println("base + 4 = ${base + 4}, base - 2 = ${base - 2}, base still $base")

    // ── 4. PITFALL: sharing one mutable list ─────────────────────────
    val teamA = mutableListOf("Ann", "Bob")
    val teamB = teamA // NOT a copy: both names point to the SAME list
    teamB.add("Eve")
    println("team A after B's change: $teamA")
    val teamA2 = mutableListOf("Ann", "Bob")
    val teamB2 = teamA2.toMutableList() // a real copy
    teamB2.add("Eve")
    println("team A with a copy:      $teamA2")

    // ── 5. WHY ───────────────────────────────────────────────────────
    // Most collection bugs come from SHARED, MUTABLE data. The safest
    // habit: use List/Set/Map, transform with map/filter/+, and only
    // keep a MutableList where one small piece of code owns it.

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Remove all names shorter than 4 letters from a MutableList using removeAll { }.
    // b) Write fun addItem(list: List<String>, item: String): List<String> = list + item.
    // c) Explain why section 1's cast is dangerous in your own words.
}
