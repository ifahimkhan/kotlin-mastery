import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Keeps the study material honest: runs every lesson's and practice project's `main` and checks
 * that what it prints matches the "OUTPUT :" block in its header comment.
 *
 * A file opts out when the OUTPUT line itself starts with "(", e.g.
 * `OUTPUT : (example session: ...)`, which marks output that depends on
 * user input, timing or thread scheduling. A missing OUTPUT label fails.
 */
class LessonOutputTest {
    @TestFactory
    fun `every lesson and practice project prints what its header promises`(): List<DynamicTest> {
        val lessons =
            SOURCE_ROOT
                .walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .filter { file -> file.parentFile.name.startsWith("lesson") || file.parentFile.parentFile.name == "practice" }
                .sortedBy { it.path }
                .toList()

        assertTrue(lessons.isNotEmpty(), "No lessons found under $SOURCE_ROOT")

        return lessons.map { file ->
            val className = className(file)
            DynamicTest.dynamicTest(className) {
                val expected = parseExpectedOutput(file) ?: return@dynamicTest
                val actual = runMain(className)
                assertEquals(expected, actual, "Output of $className differs from its OUTPUT block")
            }
        }
    }

    private fun className(file: File): String {
        val pkg =
            file.useLines { lines ->
                lines.firstOrNull { it.startsWith("package ") }?.removePrefix("package ")?.trim()
            }
        val simpleName = file.nameWithoutExtension + "Kt"
        return if (pkg.isNullOrEmpty()) simpleName else "$pkg.$simpleName"
    }

    /** Returns expected lines, or null when the file opts out with "OUTPUT : (...)". */
    private fun parseExpectedOutput(file: File): List<String>? {
        val lines = file.readLines(Charsets.UTF_8)
        val start = lines.indexOfFirst { OUTPUT_LABEL.matches(it) }
        check(start >= 0) { "${file.path} has no ' *  OUTPUT :' header line (see docs/CONVENTIONS.md)" }
        val sameLineText = lines[start].substringAfter(":").trim()
        if (sameLineText.startsWith("(")) return null
        check(sameLineText.isEmpty()) { "${file.path}: OUTPUT opt-out text must start with '(', was: $sameLineText" }

        val body =
            lines
                .drop(start + 1)
                .takeWhile { !ANY_LABEL.matches(it) && !it.trimStart().startsWith("* ===") }
                .map { it.substringAfter(" *").trimEnd() }
        val indent = body.filter { it.isNotBlank() }.minOfOrNull { it.length - it.trimStart().length } ?: 0
        return body.map { it.drop(indent) }.dropLastWhile { it.isBlank() }
    }

    private fun runMain(className: String): List<String> {
        val buffer = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(buffer, true, Charsets.UTF_8))
        try {
            Class
                .forName(className)
                .getMethod("main", Array<String>::class.java)
                .invoke(null, emptyArray<String>())
        } finally {
            System.setOut(originalOut)
        }
        return buffer
            .toString(Charsets.UTF_8)
            .lines()
            .map { it.trimEnd() }
            .dropLastWhile { it.isBlank() }
    }

    private companion object {
        // Overridable so a single level can be checked in isolation.
        val SOURCE_ROOT = File(System.getProperty("lessons.root") ?: "src/main/kotlin")
        val OUTPUT_LABEL = Regex("""^\s*\*\s+OUTPUT\s*:.*$""")
        // Header labels from docs/CONVENTIONS.md; the OUTPUT block ends at the next one.
        val ANY_LABEL = Regex("""^\s*\*\s+(GOAL|RUN|OUTPUT|NEXT|NEW|DESIGN|EXTEND|NOTE)\s*:.*$""")
    }
}
