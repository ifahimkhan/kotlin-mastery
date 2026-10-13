package l04_null_safety

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Shared checks for Level 04. Subclasses decide WHICH implementation is
 * tested: the learner's exercises or the reference solutions.
 */
@TestMethodOrder(MethodOrderer.MethodName::class) // ex1, ex2, ... in order
abstract class NullSafetyTasksTest(
    private val tasks: NullSafetyTasks,
) {
    @Test
    fun `ex1 lengthOrZero handles null`() {
        assertEquals(6, tasks.lengthOrZero("Kotlin"))
        assertEquals(0, tasks.lengthOrZero(""))
        assertEquals(0, tasks.lengthOrZero(null), "Hint: text?.length ?: 0")
    }

    @Test
    fun `ex2 firstCharOrDefault handles null and empty`() {
        assertEquals('k', tasks.firstCharOrDefault("kotlin", '?'))
        assertEquals('?', tasks.firstCharOrDefault(null, '?'))
        assertEquals('?', tasks.firstCharOrDefault("", '?'), "Hint: first() crashes on \"\", firstOrNull() doesn't")
    }

    @Test
    fun `ex3 parseAgeOrNull validates text and range`() {
        assertEquals(42, tasks.parseAgeOrNull("42"))
        assertEquals(42, tasks.parseAgeOrNull(" 42 "), "Hint: trim() first")
        assertEquals(0, tasks.parseAgeOrNull("0"))
        assertEquals(150, tasks.parseAgeOrNull("150"))
        assertNull(tasks.parseAgeOrNull("abc"))
        assertNull(tasks.parseAgeOrNull(""))
        assertNull(tasks.parseAgeOrNull("4.5"))
        assertNull(tasks.parseAgeOrNull("-1"), "Negative ages are invalid")
        assertNull(tasks.parseAgeOrNull("151"), "Ages above 150 are invalid")
    }

    @Test
    fun `ex4 displayName covers all four cases`() {
        assertEquals("Ada Lovelace", tasks.displayName("Ada", "Lovelace"))
        assertEquals("Ada", tasks.displayName("Ada", null))
        assertEquals("Lovelace", tasks.displayName(null, "Lovelace"))
        assertEquals("Anonymous", tasks.displayName(null, null))
    }

    @Test
    fun `ex5 sumOfNullable treats null as zero`() {
        assertEquals(7, tasks.sumOfNullable(3, 4))
        assertEquals(5, tasks.sumOfNullable(null, 5))
        assertEquals(5, tasks.sumOfNullable(5, null), "Hint: (a ?: 0) + (b ?: 0) - mind the parentheses")
        assertEquals(0, tasks.sumOfNullable(null, null))
    }

    @Test
    fun `ex6 safeDivide returns null for zero`() {
        assertEquals(3, tasks.safeDivide(7, 2))
        assertEquals(-2, tasks.safeDivide(-4, 2))
        assertEquals(0, tasks.safeDivide(0, 5))
        assertNull(tasks.safeDivide(1, 0), "Dividing by zero must give null, not crash")
    }

    @Test
    fun `ex7 uppercaseOrNull rejects null and blank`() {
        assertEquals("KOTLIN", tasks.uppercaseOrNull("kotlin"))
        assertEquals(" HI ", tasks.uppercaseOrNull(" hi "), "Only blank text becomes null; don't trim the rest")
        assertNull(tasks.uppercaseOrNull(null))
        assertNull(tasks.uppercaseOrNull(""))
        assertNull(tasks.uppercaseOrNull("   "), "Hint: isNullOrBlank()")
    }

    @Test
    fun `ex8 countNonNull ignores nulls`() {
        assertEquals(2, tasks.countNonNull(1, null, 3))
        assertEquals(0, tasks.countNonNull())
        assertEquals(0, tasks.countNonNull(null, null))
        assertEquals(3, tasks.countNonNull(0, 0, 0), "Zero is a value, not null")
    }
}
