package l07_functional.lesson01_lambdas

/**
 * =====================================================================
 *  LESSON 07.1 — Lambdas in depth
 * =====================================================================
 *  GOAL   : Read and write any lambda: explicit types, several lines,
 *           `it`, unused parameters `_` and destructuring.
 *  RUN    : ./gradlew -q run -Plesson=l07_functional.lesson01_lambdas.LambdasInDepthKt
 *  OUTPUT :
 *           square(7) = 49
 *           greet() = Hello!
 *           describe(-3) = -3 is negative
 *           describe(8) = 8 is even
 *           lengths = [3, 5, 5]
 *           indexes only = [0, 1, 2]
 *           Ada -> 36
 *           Alan -> 41
 *           pairs summed = [3, 7]
 *  NEXT   : lesson02_function_types/FunctionTypes.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: the full form ────────────────────────────────────
    // { parameter: Type, ... -> body }
    // The value of the LAST expression is what the lambda returns.
    // A bare `return` is not allowed in a plain lambda; return@label is (more in 07.7).
    val square = { n: Int -> n * n }
    println("square(7) = ${square(7)}")

    // A lambda with no parameters has no arrow.
    val greet = { "Hello!" }
    println("greet() = ${greet()}")

    // ── 2. CONCEPT: several lines ────────────────────────────────────
    // All lines run in order; the last line is the result.
    val describe = { n: Int ->
        val parity = if (n % 2 == 0) "even" else "odd"
        if (n < 0) "$n is negative" else "$n is $parity"
    }
    println("describe(-3) = ${describe(-3)}")
    println("describe(8) = ${describe(8)}")

    // ── 3. CONCEPT: `it` ─────────────────────────────────────────────
    // When the lambda has ONE parameter and its type is known from the
    // context, you may skip the name and use `it`.
    val names = listOf("Ada", "Grace", "Linus")
    println("lengths = ${names.map { it.length }}")
    // Use a real name when the lambda is longer than one line, or when
    // lambdas are nested: `it` inside `it` is confusing.

    // ── 4. CONCEPT: `_` for parameters you don't use ─────────────────
    // mapIndexed passes (index, value). We only want the index.
    println("indexes only = ${names.mapIndexed { index, _ -> index }}")

    // ── 5. CONCEPT: destructuring parameters ─────────────────────────
    // A Pair or a Map entry can be split right in the parameter list.
    val ages = mapOf("Ada" to 36, "Alan" to 41)
    ages.forEach { (name, age) -> println("$name -> $age") }

    val pairs = listOf(1 to 2, 3 to 4)
    println("pairs summed = ${pairs.map { (a, b) -> a + b }}")
    // Careful: { a, b -> } means TWO parameters;
    //          { (a, b) -> } means ONE parameter split into two parts.

    // ── 6. PITFALL: the type must be known somewhere ─────────────────
    //   val twice = { x -> x * 2 }
    //   // ❌ An explicit type is required on a value parameter.
    // Either type the parameter      { x: Int -> x * 2 }
    // or type the variable  val twice: (Int) -> Int = { x -> x * 2 }   (07.2)

    // ── 7. PITFALL: the last line is the result, even by accident ────
    //   val check: (Int) -> String = { it * 2 }
    //   // ❌ Return type mismatch: expected 'String', actual 'Int'.

    // ── 8. TRY IT ────────────────────────────────────────────────────
    // a) Write a lambda { a: Int, b: Int, c: Int -> ... } that returns the largest.
    // b) Use mapIndexed { i, name -> "$i:$name" } on the names list.
    // c) Loop over ages with forEach and print only names older than 40.
}
