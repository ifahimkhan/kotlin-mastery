package l11_advanced.lesson05_operator_overloading

/**
 * =====================================================================
 *  LESSON 11.5 — Operator overloading
 * =====================================================================
 *  GOAL   : Give your own types +, -, *, unary minus, comparisons,
 *           indexing, `in`, `..`, `()` and destructuring, by writing
 *           functions marked `operator`. Know the += pitfall.
 *  RUN    : ./gradlew -q run -Plesson=l11_advanced.lesson05_operator_overloading.OperatorOverloadingKt
 *  OUTPUT :
 *           a + b = (4, 6), a - b = (-2, -2), a * 3 = (3, 6), -a = (-1, -2)
 *           price + tax = 10.80, price * 3 = 30.00
 *           cheap < price: true, max = 10.00
 *           grid[1, 2] = 0, after set: 9
 *           (2, 2) in board: true, (5, 5) in board: false
 *           money range contains 5.00: true
 *           discount(10.00) = 9.00
 *           destructured: x=1, y=2
 *           for-in over a Countdown: 3 2 1
 *           cart after += : [tea, milk]
 *           equal values: true, same hash: true
 *  NEXT   : lesson06_dsl_builders/DslBuilders.kt
 * =====================================================================
 */

// ── 1. CONCEPT: arithmetic operators ────────────────────────────────
// `a + b` is just a call to `a.plus(b)`. The `operator` keyword allows it.
data class Vector2(
    val x: Int,
    val y: Int,
) {
    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)

    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)

    operator fun times(factor: Int) = Vector2(x * factor, y * factor)

    operator fun unaryMinus() = Vector2(-x, -y)

    override fun toString() = "($x, $y)"
    // data class gives component1()/component2() for destructuring.
}

// Money in Long cents (never Double for money: l01_basics/lesson03_types).
@JvmInline
value class Money(
    val cents: Long,
) : Comparable<Money> {
    operator fun plus(other: Money) = Money(cents + other.cents)

    operator fun times(count: Int) = Money(cents * count)

    // compareTo powers <, >, <=, >= and maxOf/sorting. (`operator` is
    // inherited from Comparable, so the override doesn't repeat it.)
    // Implementing Comparable is ALSO all you need for `a..b` and `in`:
    // the stdlib has `operator fun <T : Comparable<T>> T.rangeTo(that: T)`.
    // Write your own rangeTo only if you need a different range type.
    override fun compareTo(other: Money): Int = cents.compareTo(other.cents)

    override fun toString(): String {
        val sign = if (cents < 0) "-" else ""
        val abs = kotlin.math.abs(cents)
        val fraction = (abs % CENTS_PER_UNIT).toString().padStart(2, '0')
        return "$sign${abs / CENTS_PER_UNIT}.$fraction"
    }

    private companion object {
        const val CENTS_PER_UNIT = 100
    }
}

// ── 2. CONCEPT: indexing and `in` ───────────────────────────────────
class Grid(
    private val width: Int,
    private val height: Int,
) {
    private val cells = IntArray(width * height)

    operator fun get(
        row: Int,
        col: Int,
    ): Int = cells[indexOf(row, col)]

    operator fun set(
        row: Int,
        col: Int,
        value: Int,
    ) {
        cells[indexOf(row, col)] = value
    }

    // Without these checks grid[0, 5] on a 3-wide grid would silently read
    // a cell of the NEXT row: the flat array can't tell.
    private fun indexOf(
        row: Int,
        col: Int,
    ): Int {
        require(row in 0 until height) { "row must be in 0 until $height, was $row" }
        require(col in 0 until width) { "col must be in 0 until $width, was $col" }
        return row * width + col
    }
}

class Board(
    private val size: Int,
) {
    operator fun contains(point: Vector2): Boolean = point.x in 0 until size && point.y in 0 until size
}

// ── 3. CONCEPT: invoke makes an object callable like a function ─────
class Discount(
    private val percent: Int,
) {
    operator fun invoke(price: Money): Money = Money(price.cents * (PERCENT - percent) / PERCENT)

    private companion object {
        const val PERCENT = 100
    }
}

// ── 4. CONCEPT: iterator() makes `for (x in thing)` work ────────────
class Countdown(
    private val from: Int,
) {
    operator fun iterator(): Iterator<Int> = (from downTo 1).iterator()
}

// ── 5. PITFALL: += with a var and both plus and plusAssign ──────────
class Cart {
    private val items = mutableListOf<String>()

    // plusAssign changes THIS cart in place (returns Unit).
    operator fun plusAssign(item: String) {
        items += item
    }

    override fun toString() = items.toString()
}

private const val TAX_CENTS = 80L
private const val PRICE_CENTS = 1000L
private const val CHEAP_CENTS = 250L
private const val DISCOUNT_PERCENT = 10
private const val GRID_VALUE = 9
private const val BOARD_SIZE = 4
private const val COUNTDOWN_FROM = 3

fun main() {
    val a = Vector2(1, 2)
    val b = Vector2(3, 4)
    println("a + b = ${a + b}, a - b = ${a - b}, a * 3 = ${a * 3}, -a = ${-a}")

    val price = Money(PRICE_CENTS)
    val tax = Money(TAX_CENTS)
    println("price + tax = ${price + tax}, price * 3 = ${price * 3}")
    val cheap = Money(CHEAP_CENTS)
    println("cheap < price: ${cheap < price}, max = ${maxOf(cheap, price)}")
    // Without `operator`, `+` doesn't compile:
    //   ❌ 'operator' modifier is required on 'fun plus(other: Money): Money' defined in
    //      'l11_advanced.lesson05_operator_overloading.Money'.

    val grid = Grid(width = 3, height = 3)
    print("grid[1, 2] = ${grid[1, 2]}")
    grid[1, 2] = GRID_VALUE
    println(", after set: ${grid[1, 2]}")

    val board = Board(BOARD_SIZE)
    println("(2, 2) in board: ${Vector2(2, 2) in board}, (5, 5) in board: ${Vector2(5, 5) in board}")
    println("money range contains 5.00: ${Money(500) in cheap..price}")

    val discount = Discount(DISCOUNT_PERCENT)
    println("discount($price) = ${discount(price)}")

    val (x, y) = a
    println("destructured: x=$x, y=$y")

    print("for-in over a Countdown:")
    for (n in Countdown(COUNTDOWN_FROM)) print(" $n")
    println()

    val cart = Cart() // a val: += can only mean plusAssign
    cart += "tea"
    cart += "milk"
    println("cart after += : $cart")
    // If a class has BOTH plus and plusAssign and you use a `var`, `+=`
    // could mean either "replace with plus" or "change in place".
    // For `class MutBag` with both operators, `var bag = MutBag(); bag += "x"` gives:
    //   ❌ Ambiguity between assign operator candidates:
    //      fun plus(item: String): MutBag
    //      fun plusAssign(item: String): Unit
    // Rule: an immutable type offers plus; a mutable type offers plusAssign.

    // ── 6. WHY: equals and hashCode go together ──────────────────────
    // `==` calls equals(). If you write equals yourself, write hashCode
    // too, or sets and maps break. data and value classes do both for you.
    println("equal values: ${Vector2(1, 2) == a}, same hash: ${Vector2(1, 2).hashCode() == a.hashCode()}")

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Add `operator fun div(n: Int)` to Money. What about remainders?
    // b) Add a `plus` to Cart as well, then use `var cart`. Read the error.
    // c) Use an operator only where its meaning is obvious. Would
    //    `user1 + user2` be clear to a reader?
}
