package l04_null_safety.practice

import l04_null_safety.practice.safeinput.parseIntInRange
import l04_null_safety.practice.safeinput.parseNonBlank
import l04_null_safety.practice.safeinput.parseYesNo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SafeInputParserTest {
    @Test
    fun `parseIntInRange accepts numbers inside the range`() {
        assertEquals(1, parseIntInRange("1", 1..10))
        assertEquals(10, parseIntInRange(" 10 ", 1..10))
    }

    @Test
    fun `parseIntInRange rejects text, null and out-of-range numbers`() {
        assertNull(parseIntInRange("abc", 1..10))
        assertNull(parseIntInRange("", 1..10))
        assertNull(parseIntInRange(null, 1..10))
        assertNull(parseIntInRange("0", 1..10))
        assertNull(parseIntInRange("11", 1..10))
    }

    @Test
    fun `parseYesNo understands common answers in any case`() {
        assertEquals(true, parseYesNo("Y"))
        assertEquals(true, parseYesNo(" yes "))
        assertEquals(false, parseYesNo("No"))
        assertEquals(false, parseYesNo("0"))
    }

    @Test
    fun `parseYesNo returns null for anything else`() {
        assertNull(parseYesNo("maybe"))
        assertNull(parseYesNo(""))
        assertNull(parseYesNo(null))
    }

    @Test
    fun `parseNonBlank trims and rejects blank text`() {
        assertEquals("Ada", parseNonBlank("  Ada "))
        assertNull(parseNonBlank(""))
        assertNull(parseNonBlank("   "))
        assertNull(parseNonBlank(null))
    }
}
