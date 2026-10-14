package l08_generics.lesson02_functions

/**
 * =====================================================================
 *  LESSON 08.2 — Generic functions
 * =====================================================================
 *  GOAL   : Write functions that work for ANY type with `fun <T>`,
 *           use several type parameters, write generic extension
 *           functions, and know when to give the type explicitly.
 *  RUN    : ./gradlew -q run -Plesson=l08_generics.lesson02_functions.GenericFunctionsKt
 *  OUTPUT :
 *           firstOrNullOf(["x", "y"]) = x
 *           firstOrNullOf(empty) = null
 *           explicit <Int>: 1
 *           makePair("Ada", 36) = (Ada, 36)
 *           second of [1, 2, 3] = 2, second of [a] = null
 *           repeatItem("ha", 3) = [ha, ha, ha]
 *           describe: value=42 / value=hi / value=[1, 2]
 *  NEXT   : lesson03_constraints/Constraints.kt
 * =====================================================================
 */

// `<T>` goes BEFORE the function name. T can then be used in the
// parameters and in the return type.
fun <T> firstOrNullOf(items: List<T>): T? = if (items.isEmpty()) null else items[0]

// Several type parameters are separated by commas.
fun <A, B> makePair(first: A, second: B): Pair<A, B> = Pair(first, second)

// A generic EXTENSION function (level 03): works on every List<T>.
fun <T> List<T>.secondOrNull(): T? = if (size >= 2) this[1] else null

fun <T> repeatItem(item: T, times: Int): List<T> = List(times) { item }

// Without constraints you only know that T is "some type", so you can
// only use what every value has: toString(), equals() and hashCode().
fun <T> describe(value: T): String = "value=$value"

fun main() {
    // ── 1. CONCEPT: calling a generic function ───────────────────────
    // Kotlin INFERS T from the arguments: here T = String.
    println("firstOrNullOf([\"x\", \"y\"]) = ${firstOrNullOf(listOf("x", "y"))}")
    println("firstOrNullOf(empty) = ${firstOrNullOf(emptyList<Int>())}")

    // You may also write T explicitly between < >. Rarely needed.
    println("explicit <Int>: ${firstOrNullOf<Int>(listOf(1, 2))}")

    // ── 2. CONCEPT: when inference has nothing to go on ──────────────
    //   val empty = emptyList() // ❌ Cannot infer type for type parameter 'T'. Specify it explicitly.
    // Fix either way:
    //   val empty = emptyList<String>()
    //   val empty: List<String> = emptyList()

    // ── 3. CONCEPT: several type parameters ──────────────────────────
    val pair = makePair("Ada", 36) // A = String, B = Int
    println("makePair(\"Ada\", 36) = $pair")

    // ── 4. CONCEPT: generic extension functions ──────────────────────
    println("second of [1, 2, 3] = ${listOf(1, 2, 3).secondOrNull()}, second of [a] = ${listOf("a").secondOrNull()}")
    println("repeatItem(\"ha\", 3) = ${repeatItem("ha", 3)}")

    // ── 5. PITFALL: you don't know what T can do ─────────────────────
    //   fun <T> lengthOf(value: T) = value.length // ❌ Unresolved reference 'length'.
    // T might be an Int, which has no length. Only Any? members work:
    println("describe: ${describe(42)} / ${describe("hi")} / ${describe(listOf(1, 2))}")
    // To use more than that, CONSTRAIN T (next lesson).

    // ── 6. WHY generic functions? ────────────────────────────────────
    // Without generics you'd need firstOrNullOfStrings, firstOrNullOfInts...
    // or a version taking Any that loses the type. The standard library
    // is full of generic functions: listOf, first, map, filter, maxOf...

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Write `fun <T> lastOrNullOf(items: List<T>): T?`.
    // b) Write a generic extension `fun <T> T.inList(): List<T> = listOf(this)`
    //    and call it on 5 and on "x".
    // c) Write `fun <A, B> swap(pair: Pair<A, B>): Pair<B, A>`.
}
