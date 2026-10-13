package l06_collections.practice

import l06_collections.practice.words.SAMPLE_RESOURCE
import l06_collections.practice.words.countWords
import l06_collections.practice.words.formatRow
import l06_collections.practice.words.normalizeWords
import l06_collections.practice.words.readResource
import l06_collections.practice.words.topWords
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WordFrequencyTest {
    @Test
    fun `normalize lowercases and drops punctuation and digits`() {
        assertEquals(listOf("hello", "world", "it", "s"), normalizeWords("Hello, WORLD! 42 it's"))
        assertEquals(emptyList(), normalizeWords(" ... "))
    }

    @Test
    fun `top words break ties alphabetically`() {
        val counts = countWords(listOf("b", "a", "c", "a", "b"))
        assertEquals(listOf("a" to 2, "b" to 2, "c" to 1), topWords(counts, 3))
        assertEquals(listOf("a" to 2), topWords(counts, 1))
        assertEquals(emptyList(), topWords(emptyMap(), 10))
    }

    @Test
    fun `rows are aligned`() {
        assertEquals(" 1. the   6 ######", formatRow(1, "the", 6, 5))
        assertEquals("10. count 1 #", formatRow(10, "count", 1, 5))
        assertEquals(" 2. fox    9 #########", formatRow(2, "fox", 9, 5, countWidth = 2), "Counts align when padded")
    }

    @Test
    fun `sample resource exists and missing resources return null`() {
        assertNotNull(readResource(SAMPLE_RESOURCE))
        assertNull(readResource("no/such/file.txt"))
    }
}
