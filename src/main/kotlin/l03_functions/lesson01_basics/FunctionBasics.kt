package l03_functions.lesson01_basics

/**
 * =====================================================================
 *  LESSON 03.1 — Function basics
 * =====================================================================
 *  GOAL   : Declare functions with parameters and return types, call
 *           them, and choose between block body and expression body.
 *  RUN    : ./gradlew -q run -Plesson=l03_functions.lesson01_basics.FunctionBasicsKt
 *  OUTPUT :
 *           Welcome to level 03!
 *           add(2, 3) = 5
 *           square(7) = 49
 *           area of 3 x 4 rectangle = 12
 *           isAdult(17) = false, isAdult(18) = true
 *           max(10, 25) = 25
 *           Total with tax: 107.0
 *  NEXT   : lesson02_default_named/DefaultAndNamedArgs.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: declare, then call ───────────────────────────────
    // fun <name>(<param>: <Type>, ...): <ReturnType> { <body> }
    printWelcome()
    println("add(2, 3) = ${add(2, 3)}")

    // ── 2. CONCEPT: expression body ──────────────────────────────────
    // If the body is ONE expression, write `= expression`.
    // The return type can then be inferred.
    println("square(7) = ${square(7)}")
    println("area of 3 x 4 rectangle = ${rectangleArea(3, 4)}")
    println("isAdult(17) = ${isAdult(17)}, isAdult(18) = ${isAdult(18)}")

    // `if` is an expression too (level 02), so it fits an expression body.
    println("max(10, 25) = ${max(10, 25)}")

    // ── 3. CONCEPT: functions call functions ─────────────────────────
    // Small functions with clear names combine into readable programs.
    println("Total with tax: ${totalWithTax(100.0)}")

    // ── 4. WHY functions? ────────────────────────────────────────────
    // - Give a NAME to an idea: `isAdult(age)` reads better than `age >= 18`.
    // - Write once, reuse everywhere. Fix a bug in one place.
    // - Small functions are easy to test (see FunctionsTasksTest in src/test).

    // ── 5. PITFALL: parameters are read-only ─────────────────────────
    // fun increment(n: Int) { n = n + 1 } // ❌ 'val' cannot be reassigned.
    // Parameters behave like `val`. Return a new value instead:
    // fun increment(n: Int): Int = n + 1   // ✅

    // ── 6. PITFALL: block body needs `return` ────────────────────────
    // fun double(n: Int): Int { n * 2 }  // ❌ Missing return statement.
    // fun double(n: Int): Int { return n * 2 } // ✅ block body
    // fun double(n: Int) = n * 2               // ✅ expression body

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Write `fun cube(n: Int) = ...` and print cube(3).
    // b) Write `fun circleArea(radius: Double): Double` (use kotlin.math.PI).
    // c) Rewrite `add` as an expression body.
}

// A function with no parameters and no useful result (returns Unit, lesson 04).
fun printWelcome() {
    println("Welcome to level 03!")
}

// Block body: curly braces + explicit `return`.
fun add(a: Int, b: Int): Int {
    val sum = a + b
    return sum
}

// Expression body: `=` instead of braces. Return type Int is inferred.
fun square(n: Int) = n * n

// Writing the return type is still allowed, and good for public functions.
fun rectangleArea(width: Int, height: Int): Int = width * height

fun isAdult(age: Int): Boolean = age >= ADULT_AGE

fun max(a: Int, b: Int): Int = if (a > b) a else b

fun totalWithTax(price: Double): Double = price + tax(price)

private fun tax(price: Double): Double = price * TAX_RATE

private const val ADULT_AGE = 18
private const val TAX_RATE = 0.07
