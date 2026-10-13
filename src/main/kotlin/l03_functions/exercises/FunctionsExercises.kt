package l03_functions.exercises

import l03_functions.FunctionsTasks

/**
 * =====================================================================
 *  LEVEL 03 — EXERCISES (your turn!)
 * =====================================================================
 *  HOW    : Replace each TODO("...") with your answer.
 *           Keep everything else as it is. You may switch an expression
 *           body (`= ...`) to a block body (`{ ... return ... }`).
 *  CHECK  : ./gradlew exercises --tests "l03_*"
 *           or open src/test/kotlin/l03_functions/FunctionsExercisesTest.kt
 *           and click ▶ next to the class name.
 *  STUCK? : Re-read the lesson in the hint. Still stuck? Peek at
 *           ../solutions/FunctionsSolutions.kt — but try first!
 *
 *  Note: an `override` can't repeat default values; they come from
 *  FunctionsTasks.kt. Read the task descriptions there.
 * =====================================================================
 */
object FunctionsExercises : FunctionsTasks {
    // Hint: lesson02_default_named (the default exponent is 2) + a loop or tailrec helper
    override fun power(base: Long, exponent: Int): Long = TODO("Ex 1: base to the power of exponent")

    // Hint: lesson02_default_named (optional parts, Boolean flag)
    override fun formatName(first: String, last: String, middle: String, lastNameFirst: Boolean): String =
        TODO("Ex 2: format the full name")

    // Hint: lesson03_vararg (numbers is an IntArray)
    override fun sumAll(vararg numbers: Int): Int = TODO("Ex 3: add up all numbers")

    // Hint: lesson01_basics (FunctionBasics, reuse a smaller max function)
    override fun maxOf3(a: Int, b: Int, c: Int): Int = TODO("Ex 4: largest of three")

    // Hint: a for loop over the chars + buildString (l01_basics/lesson04_strings),
    // keep only Char.isLetterOrDigit(), then compare lowercase() with reversed().
    // (A shorter way, filter { ... }, uses a lambda: level 07.)
    override fun isPalindrome(text: String): Boolean = TODO("Ex 5: palindrome ignoring case and symbols")

    // Hint: lesson06_infix_tailrec (tailrec with two accumulators)
    override fun fibonacci(n: Int): Long = TODO("Ex 6: n-th Fibonacci number, fast")

    // Hint: lesson02_default_named; check times <= 0 first
    override fun repeatWord(word: String, times: Int, separator: String): String =
        TODO("Ex 7: repeat the word with a separator")

    // Hint: lesson01_basics (FunctionBasics, if as an expression)
    override fun clamp(value: Int, min: Int, max: Int): Int = TODO("Ex 8: keep value inside min..max")
}
