package l01_basics.solutions

import l01_basics.BasicsTasks
import kotlin.math.abs

/**
 * Reference answers for Level 01. Try the exercises first!
 * Each answer explains WHY it is written this way.
 */
object BasicsSolutions : BasicsTasks {
    // A template beats "Hello, " + name + "!": easier to read, no missed spaces.
    override fun greet(name: String): String = "Hello, $name!"

    // Convert to Long FIRST. `days * 86_400` would be computed as an Int
    // and overflow before being widened: 30_000 * 86_400 > Int.MAX_VALUE.
    override fun secondsInDays(days: Int): Long = days.toLong() * SECONDS_PER_DAY

    // Dividing by 3.0 (a Double) makes the whole division a Double division.
    override fun average(a: Int, b: Int, c: Int): Double = (a + b + c) / 3.0

    // n % 2 is 0 for even numbers, including negatives (-4 % 2 == 0).
    // Comparing with 1 would fail for negatives: -3 % 2 == -1.
    override fun isEven(n: Int): Boolean = n % 2 == 0

    // first() gives a Char; uppercaseChar() keeps it a Char (uppercase()
    // would return a String). The dots are plain text in the template.
    override fun initials(firstName: String, lastName: String): String =
        "${firstName.first().uppercaseChar()}.${lastName.first().uppercaseChar()}."

    // -147 % 10 == -7, so take the absolute value.
    override fun lastDigit(n: Int): Int = abs(n % 10)

    // A named constant explains the magic number 2.54.
    override fun inchesToCentimeters(inches: Double): Double = inches * CM_PER_INCH

    // ${age + 1} computes inside the template. Without braces, "$age + 1"
    // would print "41 + 1": only `age` is inserted.
    override fun describe(name: String, age: Int): String =
        "$name is $age years old and will be ${age + 1} next year."

    private const val SECONDS_PER_DAY = 24 * 60 * 60
    private const val CM_PER_INCH = 2.54
}
