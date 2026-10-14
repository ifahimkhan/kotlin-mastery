package l07_functional.lesson03_higher_order

/**
 * =====================================================================
 *  LESSON 07.3 — Higher-order functions
 * =====================================================================
 *  GOAL   : Write functions that TAKE functions and functions that
 *           RETURN functions, and combine small functions into bigger ones.
 *  RUN    : ./gradlew -q run -Plesson=l07_functional.lesson03_higher_order.HigherOrderFunctionsKt
 *  OUTPUT :
 *           round 1
 *           round 2
 *           round 3
 *           applyTwice(+3, 10) = 16
 *           applyTwice(double, 5) = 20
 *           myFilter evens = [2, 4, 6]
 *           myFilter long words = [kotlin, lambda]
 *           times3(7) = 21, times10(7) = 70
 *           addThenDouble(4) = 10
 *           doubleThenAdd(4) = 9
 *           timed result = 5050 (time varies, not printed)
 *  NEXT   : lesson04_references/FunctionReferences.kt
 * =====================================================================
 */

// ── A function that TAKES a function ──────────────────────────────────
// `action` is a parameter of function type (Int) -> Unit.
fun repeatAction(times: Int, action: (Int) -> Unit) {
    for (round in 1..times) {
        action(round)
    }
}

// The function parameter can be used more than once.
fun applyTwice(f: (Int) -> Int, x: Int): Int = f(f(x))

// Our own version of filter: the CALLER decides what "keep" means.
fun myFilter(items: List<Int>, keep: (Int) -> Boolean): List<Int> {
    val result = mutableListOf<Int>() // local and private: nobody else sees it
    for (item in items) {
        if (keep(item)) result.add(item)
    }
    return result
}

// Same idea for Strings. (One function for every type: generics, level 08.)
fun myFilterWords(items: List<String>, keep: (String) -> Boolean): List<String> {
    val result = mutableListOf<String>()
    for (item in items) {
        if (keep(item)) result.add(item)
    }
    return result
}

// ── A function that RETURNS a function ────────────────────────────────
// The return type is itself a function type: (Int) -> Int.
fun multiplier(factor: Int): (Int) -> Int = { n -> n * factor }

// ── Combining functions: compose ──────────────────────────────────────
// "first f, then g", packed into one new function.
fun andThen(f: (Int) -> Int, g: (Int) -> Int): (Int) -> Int = { x -> g(f(x)) }

// Takes ANY work, measures it, and still returns the work's result.
fun timed(work: () -> Int): Pair<Int, Long> {
    val start = System.nanoTime()
    val result = work()
    val elapsedNanos = System.nanoTime() - start
    return result to elapsedNanos
}

fun main() {
    // ── 1. CONCEPT: passing a lambda ────────────────────────────────
    // The lambda is the last argument, so it goes outside the ( ).
    repeatAction(3) { round -> println("round $round") }

    // ── 2. CONCEPT: the function decides WHEN, the caller decides WHAT ─
    println("applyTwice(+3, 10) = ${applyTwice({ it + 3 }, 10)}")
    val double = { n: Int -> n * 2 }
    println("applyTwice(double, 5) = ${applyTwice(double, 5)}")

    println("myFilter evens = ${myFilter(listOf(1, 2, 3, 4, 5, 6)) { it % 2 == 0 }}")
    println("myFilter long words = ${myFilterWords(listOf("fun", "kotlin", "lambda", "it")) { it.length > 3 }}")

    // ── 3. CONCEPT: returning functions ─────────────────────────────
    // multiplier BUILDS new functions. Each one remembers its own factor
    // (that memory is called a closure, lesson 07.5).
    val times3 = multiplier(3)
    val times10 = multiplier(10)
    println("times3(7) = ${times3(7)}, times10(7) = ${times10(7)}")

    // ── 4. CONCEPT: composition ─────────────────────────────────────
    // Order matters: (4 + 1) * 2 = 10, but 4 * 2 + 1 = 9.
    val addOne = { n: Int -> n + 1 }
    println("addThenDouble(4) = ${andThen(addOne, double)(4)}")
    println("doubleThenAdd(4) = ${andThen(double, addOne)(4)}")

    // ── 5. CONCEPT: wrapping behaviour ──────────────────────────────
    // timed() adds measuring around ANY block without changing it.
    // (Elapsed time differs on every run, so the lesson ignores it with _.)
    val (sum, _) = timed { (1..100).sum() }
    println("timed result = $sum (time varies, not printed)")

    // ── 6. WHY ───────────────────────────────────────────────────────
    // map, filter, sortedBy, repeat, buildString... are ALL higher-order
    // functions. Now you can write your own: the loop lives in ONE place,
    // and every caller only describes the part that changes.

    // ── 7. PITFALL: () -> Int is not Int ─────────────────────────────
    //   applyTwice(double(5), 10)
    //   double(5) is already a NUMBER; applyTwice wants a FUNCTION.
    //   Pass the function itself: applyTwice(double, 10)

    // ── 8. TRY IT ────────────────────────────────────────────────────
    // a) Write fun countWhere(items: List<Int>, test: (Int) -> Boolean): Int.
    // b) Write fun adder(amount: Int): (Int) -> Int and build add5 and add100.
    // c) Compose three steps with andThen twice. Does the grouping matter?
}
