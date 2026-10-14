package l08_generics.lesson03_constraints

/**
 * =====================================================================
 *  LESSON 08.3 — Constraints: upper bounds, where, T : Any
 * =====================================================================
 *  GOAL   : Limit which types T may be, so you can USE what those types
 *           offer: compare with `T : Comparable<T>`, combine bounds
 *           with `where`, and forbid null with `T : Any`.
 *  RUN    : ./gradlew -q run -Plesson=l08_generics.lesson03_constraints.ConstraintsKt
 *  OUTPUT :
 *           biggest of [3, 9, 4] = 9
 *           biggest of [pear, apple, zebra] = zebra
 *           biggest of [] = null
 *           sumAsDouble([1, 2, 3]) = 6.0, sumAsDouble([1.5, 2.5]) = 4.0
 *           bestName(players) = Kim
 *           key Int: 42
 *           key String: id-7
 *  NEXT   : lesson04_variance_out/VarianceOut.kt
 * =====================================================================
 */

// `T : Comparable<T>` = "T must be comparable with itself".
// That is what makes `item > best` allowed inside the function.
fun <T : Comparable<T>> biggest(items: List<T>): T? {
    var best: T? = null
    for (item in items) {
        if (best == null || item > best) best = item
    }
    return best
}

// An upper bound can also be a class: every Number has toDouble().
fun <T : Number> sumAsDouble(items: List<T>): Double {
    var total = 0.0
    for (item in items) total += item.toDouble()
    return total
}

interface Named {
    val name: String
}

// (classes and interfaces: level 05)
data class Player(
    override val name: String,
    val score: Int,
) : Named,
    Comparable<Player> {
    override fun compareTo(other: Player): Int = score.compareTo(other.score)
}

// `where` lists SEVERAL bounds: T must be Named AND Comparable<T>.
fun <T> bestName(items: List<T>): String? where T : Named, T : Comparable<T> = biggest(items)?.name

// T : Any = "T can't be a nullable type", so `key::class` is allowed.
fun <T : Any> describeKey(key: T): String = "key ${key::class.simpleName}: $key"

fun main() {
    // ── 1. CONCEPT: an upper bound ───────────────────────────────────
    println("biggest of [3, 9, 4] = ${biggest(listOf(3, 9, 4))}")
    println("biggest of [pear, apple, zebra] = ${biggest(listOf("pear", "apple", "zebra"))}")
    println("biggest of [] = ${biggest(emptyList<Int>())}")

    // Any is not Comparable, so it doesn't satisfy the bound:
    //   biggest<Any>(listOf(1))
    //   ❌ Type argument is not within its bounds: type parameter 'T (of fun <T : Comparable<T>> biggest)'
    //      must be subtype of 'Comparable<Any>', but actual: 'Any'.

    // ── 2. CONCEPT: a class as the bound ─────────────────────────────
    val wholeSum = sumAsDouble(listOf(1, 2, 3))
    val decimalSum = sumAsDouble(listOf(1.5, 2.5))
    println("sumAsDouble([1, 2, 3]) = $wholeSum, sumAsDouble([1.5, 2.5]) = $decimalSum")
    //   sumAsDouble(listOf("1")) // does not compile: a String is not a Number

    // ── 3. CONCEPT: several bounds with `where` ──────────────────────
    val players = listOf(Player("Ada", 12), Player("Kim", 30), Player("Lee", 7))
    println("bestName(players) = ${bestName(players)}")

    // ── 4. CONCEPT: T : Any (no nulls) ───────────────────────────────
    // Without a bound, T may be nullable: its default bound is Any?.
    //   fun <T> keyOf(x: T): Any = x
    //   ❌ Return type mismatch: expected 'Any', actual 'T (of fun <T> keyOf)'.
    println(describeKey(42))
    println(describeKey("id-7"))
    //   describeKey(null) // ❌ Null cannot be a value of a non-null type ...

    // ── 5. WHY constraints? ──────────────────────────────────────────
    // A bound is a promise in both directions: callers may only pass
    // types that fit, and inside the function you may use everything
    // the bound offers (compareTo, toDouble, name...).

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Write `fun <T : Comparable<T>> smallest(items: List<T>): T?`.
    // b) Call biggest on a list of Player. Why does it work?
    // c) Write `fun <T : CharSequence> totalLength(items: List<T>): Int`
    //    and call it with Strings and with StringBuilders.
}
