package l03_functions.practice

import l03_functions.practice.stringutils.capitalizeWords
import l03_functions.practice.stringutils.isPalindrome
import l03_functions.practice.stringutils.safeRemoveLast
import l03_functions.practice.stringutils.truncate
import l03_functions.practice.stringutils.wordCount
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringUtilsTest {
    @Test
    fun `isPalindrome ignores case and punctuation`() {
        assertTrue("Racecar".isPalindrome())
        assertTrue("Was it a car or a cat I saw?".isPalindrome())
        assertTrue("".isPalindrome())
        assertTrue("!!!".isPalindrome(), "Only symbols means nothing left to compare")
        assertFalse("Kotlin".isPalindrome())
    }

    @Test
    fun `capitalizeWords fixes case and spacing`() {
        assertEquals("Hello Kotlin World", "hello   KOTLIN world".capitalizeWords())
        assertEquals("Single", "single".capitalizeWords())
        assertEquals("", "".capitalizeWords())
        assertEquals("", "    ".capitalizeWords())
    }

    @Test
    fun `wordCount counts words separated by any whitespace`() {
        assertEquals(4, "  Kotlin is really fun ".wordCount())
        assertEquals(3, "tabs\tand\nnewlines".wordCount())
        assertEquals(1, "one".wordCount())
        assertEquals(0, "".wordCount())
        assertEquals(0, "   ".wordCount())
    }

    @Test
    fun `truncate respects max length including ellipsis`() {
        assertEquals("Kotlin ...", "Kotlin is fun".truncate(10))
        assertEquals(10, "Kotlin is fun".truncate(10).length)
        assertEquals("Kotlin is~", "Kotlin is fun".truncate(10, "~"))
        assertEquals("short", "short".truncate(10), "Text that fits stays unchanged")
        assertEquals("exact", "exact".truncate(5))
        assertEquals("", "".truncate(3))
        assertEquals("Kotl", "Kotlin".truncate(4, ellipsis = ""))
    }

    @Test
    fun `truncate rejects a max length smaller than the ellipsis`() {
        assertFailsWith<IllegalArgumentException> { "Kotlin".truncate(2) }
    }

    @Test
    fun `truncate does not complain when no cut is needed`() {
        assertEquals("Hi", "Hi".truncate(2), "Fits already, so the ellipsis size doesn't matter")
        assertEquals("", "".truncate(0))
    }

    @Test
    fun `safeRemoveLast only removes the given char`() {
        assertEquals("Fahim", "Fahim,".safeRemoveLast())
        assertEquals("Fahim", "Fahim".safeRemoveLast(), "No comma: nothing removed")
        assertEquals("", "".safeRemoveLast(), "Empty string must not crash")
        assertEquals("", ",".safeRemoveLast())
        assertEquals("Hi", "Hi!".safeRemoveLast('!'))
    }
}
