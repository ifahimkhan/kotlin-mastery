package l02_control_flow

/**
 * The list of Level 02 exercises.
 *
 * Both [l02_control_flow.exercises.ControlFlowExercises] (your answers) and
 * [l02_control_flow.solutions.ControlFlowSolutions] (reference answers)
 * implement this interface, so the SAME tests can check both.
 *
 * Read the description of each function, then fill in its body in
 * ControlFlowExercises.kt.
 */
interface ControlFlowTasks {
    /** Ex 1. "positive", "negative" or "zero".  sign(-5) == "negative" */
    fun sign(n: Int): String

    /**
     * Ex 2. FizzBuzz for ONE number: "FizzBuzz" if divisible by 3 and 5,
     * "Fizz" if by 3, "Buzz" if by 5, otherwise the number as text.
     * fizzBuzz(10) == "Buzz", fizzBuzz(7) == "7"
     */
    fun fizzBuzz(n: Int): String

    /**
     * Ex 3. Sum of all whole numbers from [from] to [to], both included.
     * If [from] > [to] the range is empty and the sum is 0.
     * sumOfRange(1, 4) == 10
     */
    fun sumOfRange(from: Int, to: Int): Long

    /** Ex 4. Number of vowels (a, e, i, o, u), upper or lower case.  countVowels("Kotlin IDE") == 4 */
    fun countVowels(text: String): Int

    /**
     * Ex 5. n! = 1 * 2 * ... * n, and 0! == 1.
     * Throw IllegalArgumentException for negative n (hint: require).
     * factorial(5) == 120, factorial(20) == 2432902008176640000
     */
    fun factorial(n: Int): Long

    /** Ex 6. True if [n] is a prime number (2, 3, 5, 7, 11, ...). Numbers below 2 are not prime. */
    fun isPrime(n: Int): Boolean

    /**
     * Ex 7. Letter grade: 90..100 -> 'A', 80..89 -> 'B', 70..79 -> 'C',
     * 60..69 -> 'D', 0..59 -> 'F'. Throw IllegalArgumentException outside 0..100.
     */
    fun gradeFor(score: Int): Char

    /**
     * Ex 8. Digits in reverse order. Trailing zeros disappear and the sign stays.
     * reverseDigits(1230) == 321, reverseDigits(-45) == -54, reverseDigits(0) == 0
     */
    fun reverseDigits(n: Int): Int
}
