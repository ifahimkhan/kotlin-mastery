package l03_functions.solutions

import l03_functions.FunctionsTasks

/**
 * Reference answers for Level 03. Try the exercises first!
 * Each answer explains WHY it is written this way.
 */
object FunctionsSolutions : FunctionsTasks {
    // A tailrec helper with an accumulator: no loop variable, no stack growth.
    override fun power(base: Long, exponent: Int): Long {
        require(exponent >= 0) { "Exponent must not be negative, was $exponent" }

        tailrec fun go(remaining: Int, acc: Long): Long = if (remaining == 0) acc else go(remaining - 1, acc * base)
        return go(exponent, 1L)
    }

    // Build the "given names" part once, then place it according to the flag.
    override fun formatName(first: String, last: String, middle: String, lastNameFirst: Boolean): String {
        val given = if (middle.isEmpty()) first else "$first $middle"
        return if (lastNameFirst) "$last, $given" else "$given $last"
    }

    // A vararg is an array: loop over it like any other array.
    override fun sumAll(vararg numbers: Int): Int {
        var total = 0
        for (n in numbers) total += n
        return total
    }

    // Reuse a two-value function instead of writing nested ifs.
    override fun maxOf3(a: Int, b: Int, c: Int): Int = max2(max2(a, b), c)

    // Keep only letters/digits in lower case, then compare with the reverse.
    // `filter { ... }` takes a lambda: a function written inline (level 07).
    override fun isPalindrome(text: String): Boolean {
        val cleaned = text.filter { it.isLetterOrDigit() }.lowercase()
        return cleaned == cleaned.reversed()
    }

    // Naive recursion fib(n-1) + fib(n-2) needs about 5.7e18 calls for n = 90 (quintillions!).
    // Carrying the last two numbers makes it n steps.
    override fun fibonacci(n: Int): Long {
        require(n >= 0) { "n must not be negative, was $n" }

        tailrec fun go(steps: Int, current: Long, next: Long): Long =
            if (steps == 0) current else go(steps - 1, next, current + next)
        return go(n, 0L, 1L)
    }

    // Validate first (early return), then build with a StringBuilder-backed buildString.
    override fun repeatWord(word: String, times: Int, separator: String): String {
        if (times <= 0) return ""
        return buildString {
            for (i in 1..times) {
                append(word)
                if (i < times) append(separator)
            }
        }
    }

    // An `if` chain as an expression. (Built-in alternative: value.coerceIn(min, max).)
    override fun clamp(value: Int, min: Int, max: Int): Int =
        if (value < min) {
            min
        } else if (value > max) {
            max
        } else {
            value
        }

    private fun max2(a: Int, b: Int): Int = if (a > b) a else b
}
