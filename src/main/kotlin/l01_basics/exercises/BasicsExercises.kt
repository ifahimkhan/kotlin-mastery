package l01_basics.exercises

import l01_basics.BasicsTasks

/**
 * =====================================================================
 *  LEVEL 01 — EXERCISES (your turn!)
 * =====================================================================
 *  HOW    : Replace each TODO("...") with your answer.
 *           Keep everything else as it is.
 *  CHECK  : ./gradlew exercises --tests "l01_*"
 *           or open src/test/kotlin/l01_basics/BasicsExercisesTest.kt
 *           and click ▶ next to the class name.
 *  STUCK? : Re-read the lesson in the hint. Still stuck? Peek at
 *           ../solutions/BasicsSolutions.kt — but try first!
 *
 *  Note: `object` and `override` are explained in level 05. For now,
 *  only change what's after the `=` sign.
 * =====================================================================
 */
object BasicsExercises : BasicsTasks {
    // Hint: lesson04_strings (string templates)
    override fun greet(name: String): String = TODO("Ex 1: return \"Hello, <name>!\"")

    // Hint: lesson03_types (overflow, toLong()). 1 day = 24 * 60 * 60 seconds.
    override fun secondsInDays(days: Int): Long = TODO("Ex 2: avoid Int overflow")

    // Hint: lesson03_types (Int division drops decimals)
    override fun average(a: Int, b: Int, c: Int): Double = TODO("Ex 3: average with decimals")

    // Hint: lesson05_operators (% remainder)
    override fun isEven(n: Int): Boolean = TODO("Ex 4: is n even?")

    // Hint: lesson04_strings (first(), uppercaseChar())
    override fun initials(firstName: String, lastName: String): String = TODO("Ex 5: \"A.L.\" style initials")

    // Hint: lesson05_operators (% keeps the sign of the left side!) + kotlin.math.abs()
    override fun lastDigit(n: Int): Int = TODO("Ex 6: last digit, never negative")

    // Hint: lesson03_types (Double arithmetic)
    override fun inchesToCentimeters(inches: Double): Double = TODO("Ex 7: inches -> cm")

    // Hint: lesson04_strings (${expression} inside a template)
    override fun describe(name: String, age: Int): String = TODO("Ex 8: describe a person")
}
