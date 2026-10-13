package l04_null_safety.practice

import l04_null_safety.practice.crashmuseum.GUEST_NAME
import l04_null_safety.practice.crashmuseum.NOT_LOADED
import l04_null_safety.practice.crashmuseum.Settings
import l04_null_safety.practice.crashmuseum.dropLastChar
import l04_null_safety.practice.crashmuseum.lengthIfString
import l04_null_safety.practice.crashmuseum.nameOrGuest
import l04_null_safety.practice.crashmuseum.parseOrZero
import kotlin.test.Test
import kotlin.test.assertEquals

class CrashMuseumTest {
    @Test
    fun `nameOrGuest falls back to Guest`() {
        assertEquals("Ada", nameOrGuest("Ada"))
        assertEquals(GUEST_NAME, nameOrGuest(null))
    }

    @Test
    fun `parseOrZero never crashes`() {
        assertEquals(12, parseOrZero("12"))
        assertEquals(0, parseOrZero("twelve"))
        assertEquals(0, parseOrZero(""))
    }

    @Test
    fun `lengthIfString uses a safe cast`() {
        assertEquals(5, lengthIfString("hello"))
        assertEquals(-1, lengthIfString(42))
    }

    @Test
    fun `dropLastChar never crashes on empty strings`() {
        assertEquals("Hello", dropLastChar("Hello,"))
        assertEquals("", dropLastChar("x"))
        assertEquals("", dropLastChar(""), "Empty in, empty out: no crash, no fake marker")
    }

    @Test
    fun `settings report when the lateinit theme is not loaded`() {
        val settings = Settings()
        assertEquals(NOT_LOADED, settings.themeOrMessage())
        settings.theme = "dark"
        assertEquals("dark", settings.themeOrMessage())
    }
}
