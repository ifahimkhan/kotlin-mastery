package l07_functional.lesson09_patterns

/**
 * =====================================================================
 *  LESSON 07.9 — Functional patterns
 * =====================================================================
 *  GOAL   : Use the everyday functional toolbox: pure functions,
 *           immutable updates, pipelines, partial application,
 *           memoization and Result / runCatching.
 *  RUN    : ./gradlew -q run -Plesson=l07_functional.lesson09_patterns.FunctionalPatternsKt
 *  OUTPUT :
 *           pure  : total = 60, total again = 60
 *           impure: 60 then 120 (same input, different answer!)
 *           cart before: [apple], after: [apple, pear]
 *           pipeline("  Hello World  ") = hello-world
 *           square(9) = 81, cube(2) = 8
 *           curried add(2)(3) = 5
 *           slowSquare(12) = 144 (computed)
 *           slowSquare(12) = 144 (from cache)
 *           real computations: 1
 *           parse("42") doubled = 84
 *           parse("4x2") doubled = -1
 *           failure type = NumberFormatException
 *  NEXT   : practice/validation/ValidationPipeline.kt
 * =====================================================================
 */

// ── Helpers for section 1: pure vs impure ─────────────────────────────
// PURE: same input -> same output, and nothing else changes.
fun total(prices: List<Int>): Int = prices.sum()

// IMPURE: depends on (and changes) state outside the function.
private var runningTotal = 0

fun addToRunningTotal(prices: List<Int>): Int {
    runningTotal += prices.sum()
    return runningTotal
}

// ── Helpers for section 4: fix some arguments, get a new function ─────
fun power(base: Int, exponent: Int): Int {
    var result = 1
    repeat(exponent) { result *= base }
    return result
}

fun fixExponent(exponent: Int): (Int) -> Int = { base -> power(base, exponent) }

private const val SQUARE = 2
private const val CUBE = 3

// ── Helper for section 5: remember answers to expensive calls ─────────
// The cache is captured by the returned lambda (a closure, 07.5).
// Kept simple on purpose: single-threaded and unbounded (it never forgets).
// A bounded cache with eviction is built in l08_generics/practice/cache.
fun memoize(f: (Int) -> Int): (Int) -> Int {
    val cache = mutableMapOf<Int, Int>()
    return { n -> cache.getOrPut(n) { f(n) } }
}

fun main() {
    // ── 1. Pure functions are predictable ────────────────────────────
    val prices = listOf(10, 20, 30)
    println("pure  : total = ${total(prices)}, total again = ${total(prices)}")
    val first = addToRunningTotal(prices)
    val second = addToRunningTotal(prices)
    println("impure: $first then $second (same input, different answer!)")
    // Pure functions are easy to test (every exercise test in this repo
    // checks pure functions) and safe to call from many coroutines at
    // once (level 09), because they share no changing state.

    // ── 2. Immutable updates: make a new value, keep the old one ─────
    val cart = listOf("apple")
    val newCart = cart + "pear" // a NEW list; cart is unchanged
    println("cart before: $cart, after: $newCart")

    // ── 3. Pipelines: a list of steps, applied with fold ─────────────
    val steps: List<(String) -> String> =
        listOf(
            { it.trim() },
            { it.lowercase() },
            { it.replace(" ", "-") },
        )
    val pipeline = { text: String -> steps.fold(text) { acc, step -> step(acc) } }
    println("pipeline(\"  Hello World  \") = ${pipeline("  Hello World  ")}")

    // ── 4. Partial application and currying ──────────────────────────
    val square = fixExponent(SQUARE)
    val cube = fixExponent(CUBE)
    println("square(9) = ${square(9)}, cube(2) = ${cube(2)}")

    // Currying: a function that takes ONE argument and returns a function
    // for the next one. Rare in everyday Kotlin, but good to recognise.
    val add = { a: Int -> { b: Int -> a + b } }
    println("curried add(2)(3) = ${add(2)(3)}")

    // ── 5. Memoization ───────────────────────────────────────────────
    var computations = 0
    val slowSquare =
        memoize { n ->
            computations++
            n * n
        }
    println("slowSquare(12) = ${slowSquare(12)} (computed)")
    println("slowSquare(12) = ${slowSquare(12)} (from cache)")
    println("real computations: $computations")
    // Only memoize PURE functions: a cached impure answer goes stale.
    // Note: memoize itself is NOT pure: its cache is shared, changing state.
    // Sharing slowSquare between threads would need a thread-safe map (level 09).

    // ── 6. Result and runCatching ────────────────────────────────────
    // runCatching { } turns "may throw" into a Result value: success or
    // failure. map transforms a success; getOrElse supplies a fallback.
    // (Under the hood it is try/catch: later level.)
    fun parseDoubled(text: String): Int =
        runCatching { text.toInt() }
            .map { it * 2 }
            .getOrElse { -1 }
    println("parse(\"42\") doubled = ${parseDoubled("42")}")
    println("parse(\"4x2\") doubled = ${parseDoubled("4x2")}")

    val failure = runCatching { "4x2".toInt() }
    println("failure type = ${failure.exceptionOrNull()?.let { it::class.simpleName }}")

    // PITFALL: runCatching catches EVERYTHING (any Throwable), including
    // real bugs, and in coroutines it can swallow cancellation (level 09).
    // Use it at boundaries (parsing input, calling other systems). Inside
    // your own code, prefer nullable returns ("4x2".toIntOrNull()) or a
    // sealed result type (level 05).

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Add a step { it.take(5) } to the pipeline. Does the order of steps matter?
    // b) Memoize a function that counts the vowels in a word.
    // c) Write fun safeDivide(a: Int, b: Int): Result<Int> with runCatching.
}
