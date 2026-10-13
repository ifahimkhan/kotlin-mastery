package l06_collections.lesson02_lists

/**
 * =====================================================================
 *  LESSON 06.2 — Lists
 * =====================================================================
 *  GOAL   : Create, read and change lists. Know the difference between
 *           a read-only List, a MutableList and an Array.
 *  RUN    : ./gradlew -q run -Plesson=l06_collections.lesson02_lists.ListsKt
 *  OUTPUT :
 *           fruits = [apple, banana, cherry], size = 3
 *           first = apple, last = cherry, fruits[1] = banana
 *           getOrNull(10) = null, getOrElse(10) = none
 *           indexOf("cherry") = 2, contains "kiwi" = false
 *           cart after add/remove: [milk, eggs, butter]
 *           cart after add(0, ...) and set: [bread, milk, cheese, butter]
 *           subList(1, 3) = [milk, cheese]
 *           built: [item 1, item 2, item 3, done]
 *           array: [10, 20, 30], ints: [1, 2, 3], as list: [10, 20, 30]
 *           array printed directly looks like: [Ljava.lang.Integer;@...
 *  NEXT   : lesson03_sets/Sets.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: read-only List ───────────────────────────────────
    // listOf gives a List: you can READ it, but not add or remove.
    val fruits = listOf("apple", "banana", "cherry")
    println("fruits = $fruits, size = ${fruits.size}")
    println("first = ${fruits.first()}, last = ${fruits.last()}, fruits[1] = ${fruits[1]}")

    // Safe reading: no IndexOutOfBoundsException for a bad index.
    println("getOrNull(10) = ${fruits.getOrNull(10)}, getOrElse(10) = ${fruits.getOrElse(10) { "none" }}")
    println("indexOf(\"cherry\") = ${fruits.indexOf("cherry")}, contains \"kiwi\" = ${"kiwi" in fruits}")

    //   fruits.add("kiwi")  // ❌ Unresolved reference 'add' on receiver of type 'List<String>'.
    //   fruits[0] = "kiwi"  // ❌ No 'set' operator method providing array access.

    // ── 2. CONCEPT: MutableList ──────────────────────────────────────
    // `val` means the VARIABLE can't point to another list. The list
    // itself can still change, because it is mutable.
    val cart = mutableListOf("milk", "eggs")
    cart.add("butter")
    cart.add("sugar")
    cart.remove("sugar")
    println("cart after add/remove: $cart")
    cart.add(0, "bread") // insert at index 0, the rest shifts right
    cart[2] = "cheese" // replace the item at index 2 ("eggs")
    println("cart after add(0, ...) and set: $cart")

    // subList(from, to): `to` is EXCLUDED, like substring.
    println("subList(1, 3) = ${cart.subList(1, 3)}")

    // ── 3. CONCEPT: buildList ────────────────────────────────────────
    // Build with a mutable list inside, get a read-only List out.
    val built =
        buildList {
            for (i in 1..3) add("item $i")
            add("done")
        }
    println("built: $built")

    // ── 4. CONCEPT: Array vs List ────────────────────────────────────
    // Array: fixed size, always mutable, Java-compatible.
    // IntArray: an array of primitive ints (fast, no boxing).
    // Prefer List in everyday Kotlin; use arrays for performance or Java APIs.
    val array = arrayOf(10, 20, 30)
    val ints = intArrayOf(1, 2, 3)
    println("array: ${array.contentToString()}, ints: ${ints.contentToString()}, as list: ${array.toList()}")

    // PITFALL: printing an array directly shows a memory address, not items.
    println("array printed directly looks like: ${array.toString().substringBefore("@")}@...")
    //   val l: List<Int> = array // ❌ Initializer type mismatch: expected 'List<Int>', actual 'Array<Int>'.

    // ── 5. WHY read-only by default? ─────────────────────────────────
    // If a function receives a List, it can't change your data THROUGH
    // THAT REFERENCE. Read-only is not the same as immutable, though: the
    // owner of a MutableList can still change it (see lesson10_pitfalls).
    // Hand out List; keep MutableList private (level 05 ideas).

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Make a mutableListOf<Int>(), add 5 numbers, then remove the first one.
    // b) Print fruits.reversed() and fruits.joinToString(" | ").
    // c) What does fruits[3] do? Try it, then fix it with getOrNull.
}
