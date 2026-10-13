package l02_control_flow

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Shared checks for Level 02. Subclasses decide WHICH implementation is
 * tested: the learner's exercises or the reference solutions.
 */
@TestMethodOrder(MethodOrderer.MethodName::class) // ex1, ex2, ... in order
abstract class ControlFlowTasksTest(
    private val tasks: ControlFlowTasks,
) {
    @Test
    fun `ex1 sign classifies numbers`() {
        assertEquals("positive", tasks.sign(5))
        assertEquals("negative", tasks.sign(-5))
        assertEquals("zero", tasks.sign(0), "Hint: zero is neither positive nor negative")
        assertEquals("negative", tasks.sign(Int.MIN_VALUE))
    }

    @Test
    fun `ex2 fizzBuzz checks the most specific rule first`() {
        assertEquals("1", tasks.fizzBuzz(1))
        assertEquals("Fizz", tasks.fizzBuzz(9))
        assertEquals("Buzz", tasks.fizzBuzz(10))
        assertEquals("FizzBuzz", tasks.fizzBuzz(15), "Hint: test 'divisible by 15' BEFORE 'divisible by 3'")
        assertEquals("FizzBuzz", tasks.fizzBuzz(0), "0 is divisible by everything")
    }

    @Test
    fun `ex3 sumOfRange includes both ends`() {
        assertEquals(10L, tasks.sumOfRange(1, 4))
        assertEquals(7L, tasks.sumOfRange(7, 7))
        assertEquals(0L, tasks.sumOfRange(5, 1), "Hint: 5..1 is an empty range")
        assertEquals(0L, tasks.sumOfRange(-3, 3))
        assertEquals(5_000_050_000L, tasks.sumOfRange(1, 100_000), "Hint: use a Long so the sum doesn't overflow")
    }

    @Test
    fun `ex4 countVowels ignores case`() {
        assertEquals(4, tasks.countVowels("Kotlin IDE"))
        assertEquals(0, tasks.countVowels(""))
        assertEquals(0, tasks.countVowels("rhythm"))
        assertEquals(5, tasks.countVowels("AEIOU"), "Hint: upper case vowels count too")
    }

    @Test
    fun `ex5 factorial uses Long and rejects negatives`() {
        assertEquals(1L, tasks.factorial(0), "Hint: 0! is 1 by definition")
        assertEquals(1L, tasks.factorial(1))
        assertEquals(120L, tasks.factorial(5))
        assertEquals(2_432_902_008_176_640_000L, tasks.factorial(20), "Hint: an Int overflows after 12!")
        assertFailsWith<IllegalArgumentException> { tasks.factorial(-1) }
    }

    @Test
    fun `ex6 isPrime handles small and large numbers`() {
        assertFalse(tasks.isPrime(-7))
        assertFalse(tasks.isPrime(0))
        assertFalse(tasks.isPrime(1), "Hint: 1 is not prime")
        assertTrue(tasks.isPrime(2))
        assertTrue(tasks.isPrime(97))
        assertFalse(tasks.isPrime(91), "91 = 7 * 13")
        assertTrue(tasks.isPrime(7_919), "7919 is the 1000th prime")
        assertFalse(tasks.isPrime(1_000_001), "1_000_001 = 101 * 9901")
    }

    @Test
    fun `ex7 gradeFor maps ranges and validates`() {
        assertEquals('A', tasks.gradeFor(100))
        assertEquals('A', tasks.gradeFor(90))
        assertEquals('B', tasks.gradeFor(89))
        assertEquals('C', tasks.gradeFor(70))
        assertEquals('D', tasks.gradeFor(65))
        assertEquals('F', tasks.gradeFor(0))
        assertFailsWith<IllegalArgumentException> { tasks.gradeFor(101) }
        assertFailsWith<IllegalArgumentException> { tasks.gradeFor(-1) }
    }

    @Test
    fun `ex8 reverseDigits keeps the sign and drops trailing zeros`() {
        assertEquals(321, tasks.reverseDigits(123))
        assertEquals(321, tasks.reverseDigits(1230))
        assertEquals(-54, tasks.reverseDigits(-45))
        assertEquals(0, tasks.reverseDigits(0))
        assertEquals(7, tasks.reverseDigits(7))
    }
}
