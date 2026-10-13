package l03_functions

/**
 * The list of Level 03 exercises.
 *
 * Both [l03_functions.exercises.FunctionsExercises] (your answers) and
 * [l03_functions.solutions.FunctionsSolutions] (reference answers)
 * implement this interface, so the SAME tests can check both.
 *
 * Default values declared here are used by every implementation, so you
 * can call `power(5)` and get `power(5, 2)`.
 */
interface FunctionsTasks {
    /** Ex 1. [base] raised to [exponent] (exponent >= 0).  power(5) == 25, power(2, 10) == 1024 */
    fun power(base: Long, exponent: Int = 2): Long

    /**
     * Ex 2. Format a full name. Middle name is optional (empty = none).
     * formatName("Ada", "Lovelace") == "Ada Lovelace"
     * formatName("John", "Kennedy", middle = "Fitzgerald") == "John Fitzgerald Kennedy"
     * formatName("Ada", "Lovelace", lastNameFirst = true) == "Lovelace, Ada"
     * formatName("John", "Kennedy", middle = "Fitzgerald", lastNameFirst = true) == "Kennedy, John Fitzgerald"
     */
    fun formatName(first: String, last: String, middle: String = "", lastNameFirst: Boolean = false): String

    /** Ex 3. Sum of all given numbers, 0 when none.  sumAll(1, 2, 3) == 6 */
    fun sumAll(vararg numbers: Int): Int

    /** Ex 4. Largest of three numbers.  maxOf3(3, 9, 4) == 9 */
    fun maxOf3(a: Int, b: Int, c: Int): Int

    /**
     * Ex 5. Palindrome check that ignores case and everything that is not
     * a letter or digit.  isPalindrome("A man, a plan, a canal: Panama") == true
     * An empty text is a palindrome.
     */
    fun isPalindrome(text: String): Boolean

    /** Ex 6. The n-th Fibonacci number: fib(0)=0, fib(1)=1, fib(n)=fib(n-1)+fib(n-2). fibonacci(90) must work fast. */
    fun fibonacci(n: Int): Long

    /** Ex 7. Repeat [word] [times] times joined by [separator]. repeatWord("ha", 3) == "ha ha ha"; times <= 0 gives "". */
    fun repeatWord(word: String, times: Int, separator: String = " "): String

    /** Ex 8. Limit [value] to the range [min]..[max].  clamp(15, 0, 10) == 10, clamp(-3, 0, 10) == 0 */
    fun clamp(value: Int, min: Int, max: Int): Int
}
