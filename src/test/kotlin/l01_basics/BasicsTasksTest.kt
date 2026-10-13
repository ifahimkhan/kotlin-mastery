package l01_basics

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Shared checks for Level 01. Subclasses decide WHICH implementation is
 * tested: the learner's exercises or the reference solutions.
 */
@TestMethodOrder(MethodOrderer.MethodName::class) // ex1, ex2, ... in order
abstract class BasicsTasksTest(
    private val tasks: BasicsTasks,
) {
    @Test
    fun `ex1 greet builds a greeting`() {
        assertEquals("Hello, Kotlin!", tasks.greet("Kotlin"))
        assertEquals("Hello, !", tasks.greet(""), "An empty name still gets the punctuation")
    }

    @Test
    fun `ex2 secondsInDays does not overflow`() {
        assertEquals(86_400L, tasks.secondsInDays(1))
        assertEquals(0L, tasks.secondsInDays(0))
        assertEquals(2_592_000_000L, tasks.secondsInDays(30_000), "Hint: convert to Long BEFORE multiplying")
    }

    @Test
    fun `ex3 average keeps decimals`() {
        assertEquals(2.0, tasks.average(1, 2, 3), DELTA)
        assertEquals(5.0 / 3.0, tasks.average(1, 2, 2), DELTA, "Hint: 5 / 3 is 1 for Ints")
        assertEquals(-1.0, tasks.average(-1, -1, -1), DELTA)
    }

    @Test
    fun `ex4 isEven handles negatives and zero`() {
        assertTrue(tasks.isEven(0))
        assertTrue(tasks.isEven(10))
        assertTrue(tasks.isEven(-4))
        assertFalse(tasks.isEven(7))
        assertFalse(tasks.isEven(-3), "Hint: -3 % 2 == -1, not 1")
    }

    @Test
    fun `ex5 initials are upper case with dots`() {
        assertEquals("A.L.", tasks.initials("ada", "lovelace"))
        assertEquals("G.H.", tasks.initials("Grace", "Hopper"))
    }

    @Test
    fun `ex6 lastDigit is never negative`() {
        assertEquals(3, tasks.lastDigit(123))
        assertEquals(0, tasks.lastDigit(0))
        assertEquals(7, tasks.lastDigit(-147), "Hint: -147 % 10 == -7")
    }

    @Test
    fun `ex7 inchesToCentimeters converts`() {
        assertEquals(2.54, tasks.inchesToCentimeters(1.0), DELTA)
        assertEquals(30.48, tasks.inchesToCentimeters(12.0), DELTA)
        assertEquals(0.0, tasks.inchesToCentimeters(0.0), DELTA)
    }

    @Test
    fun `ex8 describe uses an expression in the template`() {
        assertEquals("Sam is 41 years old and will be 42 next year.", tasks.describe("Sam", 41))
        assertEquals("Mia is 0 years old and will be 1 next year.", tasks.describe("Mia", 0))
    }

    private companion object {
        const val DELTA = 1e-9
    }
}
