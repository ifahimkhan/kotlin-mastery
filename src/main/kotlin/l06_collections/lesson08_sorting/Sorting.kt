package l06_collections.lesson08_sorting

/**
 * =====================================================================
 *  LESSON 06.8 — Sorting
 * =====================================================================
 *  GOAL   : Sort by natural order, by a property, and by several
 *           properties, without changing the original list.
 *  RUN    : ./gradlew -q run -Plesson=l06_collections.lesson08_sorting.SortingKt
 *  OUTPUT :
 *           sorted           -> [1, 2, 5, 8, 9]
 *           sortedDescending -> [9, 8, 5, 2, 1]
 *           reversed         -> [5, 1, 9, 2, 8]  (just backwards, NOT sorted)
 *           words sorted     -> [Banana, apple, cherry]
 *           ignoring case    -> [apple, Banana, cherry]
 *           by age           -> [Cid(25), Ann(31), Bea(31), Dan(40)]
 *           by age desc      -> [Dan(40), Ann(31), Bea(31), Cid(25)]
 *           by city, then age desc -> [Ann(31, Oslo), Dan(40, Rome), Bea(31, Rome), Cid(25, Rome)]
 *           original still: [8, 2, 9, 1, 5]
 *           sort() in place on a MutableList: [1, 2, 5, 8, 9]
 *  NEXT   : lesson09_sequences/Sequences.kt
 * =====================================================================
 */

// toString() is customised so the output stays short.
data class Person(val name: String, val age: Int, val city: String) {
    override fun toString(): String = "$name($age)"
}

private fun Person.withCity(): String = "$name($age, $city)"

fun main() {
    // ── 1. CONCEPT: natural order ────────────────────────────────────
    val numbers = listOf(8, 2, 9, 1, 5)
    println("sorted           -> ${numbers.sorted()}")
    println("sortedDescending -> ${numbers.sortedDescending()}")
    println("reversed         -> ${numbers.reversed()}  (just backwards, NOT sorted)")

    // PITFALL: Strings sort by character code, and 'B' (66) < 'a' (97).
    val words = listOf("cherry", "Banana", "apple")
    println("words sorted     -> ${words.sorted()}")
    println("ignoring case    -> ${words.sortedBy { it.lowercase() }}")

    // ── 2. CONCEPT: sort by a property ───────────────────────────────
    val people =
        listOf(
            Person("Ann", 31, "Oslo"),
            Person("Bea", 31, "Rome"),
            Person("Cid", 25, "Rome"),
            Person("Dan", 40, "Rome"),
        )
    // Sorting is STABLE: Ann and Bea (both 31) keep their original order.
    println("by age           -> ${people.sortedBy { it.age }}")
    println("by age desc      -> ${people.sortedByDescending { it.age }}")

    // ── 3. CONCEPT: several keys with compareBy / thenBy ─────────────
    val byCityThenAgeDesc = compareBy<Person> { it.city }.thenByDescending { it.age }
    println("by city, then age desc -> ${people.sortedWith(byCityThenAgeDesc).map { it.withCity() }}")

    // ── 4. WHY: sorted() returns a NEW list ──────────────────────────
    println("original still: $numbers")
    // MutableList also has sort(), which changes the list IN PLACE.
    val mutable = numbers.toMutableList()
    mutable.sort()
    println("sort() in place on a MutableList: $mutable")
    // Prefer sorted() unless you really own the mutable list.

    // ── 5. TRY IT ────────────────────────────────────────────────────
    // a) Sort listOf("kiwi", "fig", "banana") by length, then alphabetically.
    // b) Find the 2 oldest people with sortedByDescending + take(2).
    // c) Sort people by name descending with compareByDescending.
}
