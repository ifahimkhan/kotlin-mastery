package l02_control_flow.solutions

import l02_control_flow.ControlFlowTasks

/**
 * Reference answers for Level 02. Try the exercises first!
 * Each answer explains WHY it is written this way.
 */
object ControlFlowSolutions : ControlFlowTasks {
    // `when` without a subject reads like a table of rules.
    override fun sign(n: Int): String =
        when {
            n > 0 -> "positive"
            n < 0 -> "negative"
            else -> "zero"
        }

    // Most specific rule FIRST: 15 is divisible by 3 too.
    override fun fizzBuzz(n: Int): String =
        when {
            n % FIZZ_BUZZ == 0 -> "FizzBuzz"
            n % FIZZ == 0 -> "Fizz"
            n % BUZZ == 0 -> "Buzz"
            else -> n.toString()
        }

    // When from > to, `from..to` is empty: the loop runs zero times and
    // the sum stays 0 — no special case needed. Long avoids overflow.
    override fun sumOfRange(from: Int, to: Int): Long {
        var sum = 0L
        for (n in from..to) {
            sum += n
        }
        return sum
    }

    // Lower-case each char once, then a `when` with several values.
    override fun countVowels(text: String): Int {
        var count = 0
        for (char in text) {
            when (char.lowercaseChar()) {
                'a', 'e', 'i', 'o', 'u' -> count++
            }
        }
        return count
    }

    // Start at 1 (the value of 0!). An Int would overflow after 12!.
    override fun factorial(n: Int): Long {
        require(n >= 0) { "factorial is undefined for negative numbers, was $n" }
        var result = 1L
        for (i in 2..n) {
            result *= i
        }
        return result
    }

    // Only divisors up to sqrt(n) need checking: if n = a * b, one of
    // a or b is <= sqrt(n). `d * d <= n` avoids floating point.
    override fun isPrime(n: Int): Boolean {
        if (n < 2) return false
        var divisor = 2
        while (divisor.toLong() * divisor <= n) {
            if (n % divisor == 0) return false
            divisor++
        }
        return true
    }

    // Validate FIRST, then classify. Ranges in `when` mirror the spec.
    override fun gradeFor(score: Int): Char {
        require(score in MIN_SCORE..MAX_SCORE) { "score must be in $MIN_SCORE..$MAX_SCORE, was $score" }
        return when (score) {
            in A_MIN..MAX_SCORE -> 'A'
            in B_MIN..<A_MIN -> 'B'
            in C_MIN..<B_MIN -> 'C'
            in D_MIN..<C_MIN -> 'D'
            else -> 'F'
        }
    }

    // Peel the last digit off `remaining` and push it onto `reversed`.
    // Works for negatives because % and / keep the sign: -45 -> -5, -4.
    // Out of scope: reversing huge values (1_999_999_999 -> 9_999_999_991)
    // overflows Int silently, see l01_basics/lesson03_types.
    override fun reverseDigits(n: Int): Int {
        var remaining = n
        var reversed = 0
        while (remaining != 0) {
            reversed = reversed * BASE + remaining % BASE
            remaining /= BASE
        }
        return reversed
    }

    private const val FIZZ = 3
    private const val BUZZ = 5
    private const val FIZZ_BUZZ = FIZZ * BUZZ
    private const val MIN_SCORE = 0
    private const val MAX_SCORE = 100
    private const val A_MIN = 90
    private const val B_MIN = 80
    private const val C_MIN = 70
    private const val D_MIN = 60
    private const val BASE = 10 // decimal digits
}
