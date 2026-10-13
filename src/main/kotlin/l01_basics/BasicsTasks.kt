package l01_basics

/**
 * The list of Level 01 exercises.
 *
 * Both [l01_basics.exercises.BasicsExercises] (your answers) and
 * [l01_basics.solutions.BasicsSolutions] (reference answers) implement
 * this interface, so the SAME tests can check both.
 *
 * You don't need to understand `interface` yet (level 05). Just read the
 * description of each function, then fill in its body in BasicsExercises.kt.
 */
interface BasicsTasks {
    /** Ex 1. Return "Hello, <name>!".  greet("Kotlin") == "Hello, Kotlin!" */
    fun greet(name: String): String

    /** Ex 2. Number of seconds in [days] days. Must not overflow for 30_000 days. */
    fun secondsInDays(days: Int): Long

    /** Ex 3. Average of three whole numbers, WITH decimals.  average(1, 2, 2) == 1.666... */
    fun average(a: Int, b: Int, c: Int): Double

    /** Ex 4. True if [n] is even. Works for negatives and zero too. */
    fun isEven(n: Int): Boolean

    /** Ex 5. Initials in upper case with dots.  initials("ada", "lovelace") == "A.L." */
    fun initials(firstName: String, lastName: String): String

    /** Ex 6. Last digit of [n], always 0..9.  lastDigit(-147) == 7 */
    fun lastDigit(n: Int): Int

    /** Ex 7. Convert inches to centimeters (1 inch = 2.54 cm). */
    fun inchesToCentimeters(inches: Double): Double

    /**
     * Ex 8. Describe a person and their age next year:
     * describe("Sam", 41) == "Sam is 41 years old and will be 42 next year."
     */
    fun describe(name: String, age: Int): String
}
