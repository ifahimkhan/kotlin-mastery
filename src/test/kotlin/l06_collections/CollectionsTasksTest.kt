package l06_collections

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Shared checks for Level 06. Subclasses decide WHICH implementation is
 * tested: the learner's exercises or the reference solutions.
 */
@TestMethodOrder(MethodOrderer.MethodName::class) // ex01, ex02, ... in order
abstract class CollectionsTasksTest(
    private val tasks: CollectionsTasks,
) {
    @Test
    fun `ex01 evensSquared keeps order`() {
        assertEquals(listOf(4, 16), tasks.evensSquared(listOf(1, 2, 3, 4)))
        assertEquals(listOf(36, 0, 4), tasks.evensSquared(listOf(6, 0, -2, 7)), "Hint: 0 and negatives can be even")
        assertEquals(emptyList(), tasks.evensSquared(emptyList()))
    }

    @Test
    fun `ex02 wordLengths maps words to lengths`() {
        assertEquals(mapOf("hi" to 2, "kotlin" to 6), tasks.wordLengths(listOf("hi", "kotlin")))
        assertEquals(mapOf("a" to 1), tasks.wordLengths(listOf("a", "a")), "Duplicate words become one key")
        assertEquals(emptyMap(), tasks.wordLengths(emptyList()))
    }

    @Test
    fun `ex03 mostFrequentChar ignores whitespace and breaks ties by first occurrence`() {
        assertEquals('l', tasks.mostFrequentChar("hello world"))
        assertEquals('a', tasks.mostFrequentChar("abab"), "Hint: on a tie, the first one seen wins")
        assertEquals('x', tasks.mostFrequentChar("  x  "), "Hint: spaces don't count")
        assertNull(tasks.mostFrequentChar("   "))
        assertNull(tasks.mostFrequentChar(""))
    }

    @Test
    fun `ex04 groupByFirstLetter uses lower case keys and skips empty words`() {
        assertEquals(
            mapOf('a' to listOf("Apple", "avocado"), 'b' to listOf("bob")),
            tasks.groupByFirstLetter(listOf("Apple", "bob", "avocado")),
        )
        assertEquals(
            mapOf('k' to listOf("kiwi")),
            tasks.groupByFirstLetter(listOf("", "kiwi")),
            "Hint: first() crashes on \"\"",
        )
        assertEquals(emptyMap(), tasks.groupByFirstLetter(emptyList()))
    }

    @Test
    fun `ex05 secondLargestDistinct ignores duplicates`() {
        assertEquals(4, tasks.secondLargestDistinct(listOf(4, 9, 9, 2)), "Hint: the second 9 is not 'second largest'")
        assertEquals(-3, tasks.secondLargestDistinct(listOf(-1, -3, -5)))
        assertNull(tasks.secondLargestDistinct(listOf(7, 7)))
        assertNull(tasks.secondLargestDistinct(emptyList()))
    }

    @Test
    fun `ex06 runningTotals has the same size as the input`() {
        assertEquals(listOf(1, 3, 6, 10), tasks.runningTotals(listOf(1, 2, 3, 4)))
        assertEquals(listOf(5), tasks.runningTotals(listOf(5)))
        assertEquals(emptyList(), tasks.runningTotals(emptyList()), "Hint: runningFold(0) adds an extra 0")
    }

    @Test
    fun `ex07 mergeInventories adds shared counts and keeps order`() {
        val merged = tasks.mergeInventories(mapOf("pen" to 2, "ink" to 1), mapOf("ink" to 4, "cap" to 3))
        assertEquals(mapOf("pen" to 2, "ink" to 5, "cap" to 3), merged)
        assertEquals(listOf("pen", "ink", "cap"), merged.keys.toList(), "Keys of the first map come first")
        assertEquals(mapOf("x" to 1), tasks.mergeInventories(emptyMap(), mapOf("x" to 1)))
    }

    @Test
    fun `ex08 topNWords sorts by count then alphabetically`() {
        assertEquals(listOf("b", "a"), tasks.topNWords("b a b c a b", 2))
        assertEquals(
            listOf("cat", "dog"),
            tasks.topNWords("Dog, cat! dog? CAT.", 5),
            "Hint: ties are alphabetical; ignore case and punctuation",
        )
        assertEquals(emptyList(), tasks.topNWords("a b", 0))
        assertEquals(emptyList(), tasks.topNWords("", 3))
    }

    @Test
    fun `ex09 isAnagram ignores case and spaces`() {
        assertTrue(tasks.isAnagram("Dormitory", "dirty room"))
        assertTrue(tasks.isAnagram("listen", "silent"))
        assertFalse(tasks.isAnagram("aab", "abb"), "Hint: letter COUNTS must match, not just the letters")
    }

    @Test
    fun `ex10 flattenAndDedupe keeps first occurrence order`() {
        assertEquals(listOf(3, 1, 2), tasks.flattenAndDedupe(listOf(listOf(3, 1), listOf(1, 2), listOf(3))))
        assertEquals(emptyList(), tasks.flattenAndDedupe(listOf(emptyList(), emptyList())))
    }
}
