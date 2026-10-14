package l12_capstones.library

import l12_capstones.library.reference.LibraryReference
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** The CLI layer and the save file, driven with the reference rules. */
class LibraryAppTest {
    private var today: LocalDate = LocalDate.of(2026, 3, 1)

    private fun app(onChange: (LibraryState) -> Unit = {}) =
        LibraryApp(LibraryReference, today = { today }, onChange = onChange)

    private fun LibraryApp.run(line: String): List<String> = handle(LibraryReference.parseCommand(line))

    @Test
    fun `a whole session from typed lines`() {
        val app = app()
        assertEquals(listOf("added Kotlin in Action"), app.run("add-book 111 | Kotlin in Action | Jemerov | 2017 | 1"))
        assertEquals(listOf("welcome Ada"), app.run("add-member m1 | Ada"))
        assertEquals(listOf("borrowed 111, due 2026-03-15"), app.run("borrow 111 m1"))
        assertEquals(listOf("111  Kotlin in Action by Jemerov (2017)  0/1 available"), app.run("list"))
        assertEquals(listOf("all copies are on loan"), app.run("borrow 111 m1"))

        today = today.plusDays(LOAN_DAYS + 4)
        assertEquals(listOf("111 by m1: 4 days late, fine $1.00"), app.run("overdue"))
        assertEquals(listOf("returned 111, fine $1.00"), app.run("return 111 m1"))
        assertEquals(listOf("nothing is overdue"), app.run("overdue"))
    }

    @Test
    fun `friendly messages for mistakes`() {
        val app = app()
        app.run("add-member m1 | Ada")
        assertEquals(listOf("member id m1 is already used"), app.run("add-member m1 | Someone"))
        assertEquals(listOf("? unknown command 'dance', type 'help'"), app.run("dance"))
        assertEquals(listOf("the library is empty"), app.run("list"))
    }

    @Test
    fun `onChange fires only when the state really changes`() {
        val saved = mutableListOf<LibraryState>()
        val app = app { saved += it }
        app.run("add-member m1 | Ada")
        app.run("borrow 999 m1") // refused: nothing to save
        app.run("list")
        assertEquals(1, saved.size)
    }

    @Test
    fun `the save file round-trips and a missing file is an empty library`(@TempDir dir: Path) {
        val file = LibraryFile(dir.resolve("lib.json"), LibraryReference)
        assertEquals(LoadResult.Loaded(LibraryState()), file.load())

        val app = app(onChange = file::save)
        app.run("add-book 111 | Kotlin in Action | Jemerov | 2017 | 1")
        app.run("add-member m1 | Ada")
        app.run("borrow 111 m1")

        assertEquals(LoadResult.Loaded(app.state), file.load())
    }

    @Test
    fun `a corrupt save file is reported, not overwritten`(@TempDir dir: Path) {
        val path = dir.resolve("lib.json")
        Files.writeString(path, "{ broken")
        assertIs<LoadResult.Corrupt>(LibraryFile(path, LibraryReference).load())
        assertEquals("{ broken", Files.readString(path))
    }

    @Test
    fun `a failed save is reported and the state does not change`() {
        val app = app(onChange = { throw IOException("disk full") })
        assertEquals(listOf("could not save: disk full"), app.run("add-member m1 | Ada"))
        assertEquals(LibraryState(), app.state, "Memory must not get ahead of the disk")
    }

    @Test
    fun `saving onto a folder fails cleanly and leaves no temp file`(@TempDir dir: Path) {
        val target = dir.resolve("lib.json")
        Files.createDirectory(target)
        Files.writeString(target.resolve("keep.txt"), "a folder that is not empty")
        val file = LibraryFile(target, LibraryReference)

        val app = app(onChange = file::save)
        val answer = app.run("add-member m1 | Ada")
        assertTrue(answer.single().startsWith("could not save:"), "was: $answer")
        assertEquals(LibraryState(), app.state)
        val leftovers = Files.list(dir).use { files -> files.filter { it.toString().endsWith(".tmp") }.count() }
        assertEquals(0L, leftovers, "The temporary file must be cleaned up")
    }

    @Test
    fun `money is formatted from cents`() {
        assertEquals("$0.05", formatCents(5))
        assertEquals("$12.34", formatCents(1234))
        assertEquals("-$0.05", formatCents(-5))
    }
}
