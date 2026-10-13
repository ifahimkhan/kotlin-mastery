package l03_functions

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Shared checks for Level 03. Subclasses decide WHICH implementation is
 * tested: the learner's exercises or the reference solutions.
 */
@TestMethodOrder(MethodOrderer.MethodName::class) // ex1, ex2, ... in order
abstract class FunctionsTasksTest(
    private val tasks: FunctionsTasks,
) {
    @Test
    fun `ex1 power uses exponent 2 by default`() {
        assertEquals(25L, tasks.power(5), "Hint: power(5) should use the default exponent 2")
        assertEquals(1024L, tasks.power(2, 10))
        assertEquals(1L, tasks.power(7, 0), "Anything to the power 0 is 1")
        assertEquals(-8L, tasks.power(-2, 3))
        assertEquals(10_000_000_000L, tasks.power(10, 10), "Hint: work with Long, not Int")
    }

    @Test
    fun `ex2 formatName handles optional middle name and order`() {
        assertEquals("Ada Lovelace", tasks.formatName("Ada", "Lovelace"))
        assertEquals("John Fitzgerald Kennedy", tasks.formatName("John", "Kennedy", middle = "Fitzgerald"))
        assertEquals("Lovelace, Ada", tasks.formatName("Ada", "Lovelace", lastNameFirst = true))
        assertEquals(
            "Kennedy, John Fitzgerald",
            tasks.formatName("John", "Kennedy", middle = "Fitzgerald", lastNameFirst = true),
        )
    }

    @Test
    fun `ex3 sumAll accepts any number of arguments`() {
        assertEquals(0, tasks.sumAll(), "No numbers means a sum of 0")
        assertEquals(5, tasks.sumAll(5))
        assertEquals(6, tasks.sumAll(1, 2, 3))
        val numbers = intArrayOf(10, -20, 30)
        assertEquals(20, tasks.sumAll(*numbers))
    }

    @Test
    fun `ex4 maxOf3 finds the largest wherever it is`() {
        assertEquals(9, tasks.maxOf3(9, 3, 4))
        assertEquals(9, tasks.maxOf3(3, 9, 4))
        assertEquals(9, tasks.maxOf3(3, 4, 9))
        assertEquals(-1, tasks.maxOf3(-5, -1, -3))
        assertEquals(7, tasks.maxOf3(7, 7, 7))
    }

    @Test
    fun `ex5 isPalindrome ignores case and symbols`() {
        assertTrue(tasks.isPalindrome("racecar"))
        assertTrue(tasks.isPalindrome("Racecar"), "Hint: ignore case")
        assertTrue(tasks.isPalindrome("A man, a plan, a canal: Panama"), "Hint: ignore spaces and punctuation")
        assertTrue(tasks.isPalindrome(""), "An empty text is a palindrome")
        assertTrue(tasks.isPalindrome("12321"))
        assertFalse(tasks.isPalindrome("Kotlin"))
    }

    @Test
    fun `ex6 fibonacci is correct and fast`() {
        assertEquals(0L, tasks.fibonacci(0))
        assertEquals(1L, tasks.fibonacci(1))
        assertEquals(1L, tasks.fibonacci(2))
        assertEquals(55L, tasks.fibonacci(10))
        assertEquals(2_880_067_194_370_816_120L, tasks.fibonacci(90), "Hint: naive recursion is far too slow, use tailrec or a loop")
    }

    @Test
    fun `ex7 repeatWord uses a space by default`() {
        assertEquals("ha ha ha", tasks.repeatWord("ha", 3))
        assertEquals("go-go", tasks.repeatWord("go", 2, separator = "-"))
        assertEquals("solo", tasks.repeatWord("solo", 1), "No separator after a single word")
        assertEquals("", tasks.repeatWord("ha", 0))
        assertEquals("", tasks.repeatWord("ha", -2), "Negative times gives an empty string")
    }

    @Test
    fun `ex8 clamp keeps value inside the range`() {
        assertEquals(5, tasks.clamp(5, 0, 10))
        assertEquals(10, tasks.clamp(15, 0, 10))
        assertEquals(0, tasks.clamp(-3, 0, 10))
        assertEquals(0, tasks.clamp(0, 0, 10), "Edges are inside the range")
        assertEquals(10, tasks.clamp(10, 0, 10))
    }
}
