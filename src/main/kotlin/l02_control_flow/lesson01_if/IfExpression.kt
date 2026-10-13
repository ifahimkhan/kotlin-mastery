package l02_control_flow.lesson01_if

/**
 * =====================================================================
 *  LESSON 02.1 — if as an expression
 * =====================================================================
 *  GOAL   : Make decisions with `if` / `else if` / `else`, and use `if`
 *           as an EXPRESSION that produces a value.
 *  RUN    : ./gradlew -q run -Plesson=l02_control_flow.lesson01_if.IfExpressionKt
 *  OUTPUT :
 *           18 is old enough to vote
 *           max of 7 and 12 is 12
 *           temperature 31 feels hot
 *           temperature 18 feels mild
 *           temperature -3 feels freezing
 *           ticket price for age 10 is 5
 *           Computing the discount...
 *           discount = 15%
 *  NEXT   : lesson02_when/When.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: classic if / else ────────────────────────────────
    // The condition inside ( ) must be a Boolean. Braces { } hold the
    // code to run. else is optional when used as a statement.
    val age = 18
    if (age >= 18) {
        println("$age is old enough to vote")
    } else {
        println("$age is too young to vote")
    }

    // ── 2. CONCEPT: if is an EXPRESSION ──────────────────────────────
    // In Kotlin, `if` returns a value. That's why Kotlin has no ternary
    // operator (Java's `a > b ? a : b`): you don't need one.
    val a = 7
    val b = 12
    val max = if (a > b) a else b
    println("max of $a and $b is $max")

    // ── 3. CONCEPT: else if chains ───────────────────────────────────
    // Conditions are checked top to bottom; the FIRST true one wins.
    println("temperature 31 feels ${describeTemperature(31)}")
    println("temperature 18 feels ${describeTemperature(18)}")
    println("temperature -3 feels ${describeTemperature(-3)}")

    // ── 4. CONCEPT: blocks as values ─────────────────────────────────
    // When a branch is a block { }, its LAST expression is the value.
    val childAge = 10
    val price =
        if (childAge < 12) {
            // you can run several statements here...
            5 // ...and the last line is the result
        } else {
            10
        }
    println("ticket price for age $childAge is $price")

    val discount =
        if (price < 8) {
            println("Computing the discount...")
            15
        } else {
            0
        }
    println("discount = $discount%")

    // ── 5. WHY expressions? ──────────────────────────────────────────
    // `val x = if (...) a else b` lets x be a `val` that is assigned
    // exactly once. With statements you'd need a `var` and assign it
    // inside each branch — more lines and room for mistakes.

    // ── 6. PITFALL: if as an expression needs else ───────────────────
    //   val label = if (age > 18) "adult"   // ❌ 'if' must have both main and 'else' branches when used as an expression.
    // And `=` is not `==`:
    //   if (age = 18) { }                   // ❌ Only expressions are allowed in this context.

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Write `val min = if (...) ... else ...` for two numbers.
    // b) Add a "warm" level (20..29) to describeTemperature.
    // c) Print "even" or "odd" for a number using if as an expression.
}

private fun describeTemperature(celsius: Int): String =
    if (celsius >= 30) {
        "hot"
    } else if (celsius >= 10) {
        "mild"
    } else if (celsius >= 0) {
        "cold"
    } else {
        "freezing"
    }
