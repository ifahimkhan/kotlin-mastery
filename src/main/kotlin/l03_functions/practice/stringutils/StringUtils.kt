package l03_functions.practice.stringutils

/**
 * =====================================================================
 *  PRACTICE 03.A — A tiny String utility library
 * =====================================================================
 *  GOAL   : Design small, reusable extension functions with sensible
 *           defaults, and make them safe for edge cases (empty text!).
 *  RUN    : ./gradlew -q run -Plesson=l03_functions.practice.stringutils.StringUtilsKt
 *  OUTPUT :
 *           "Racecar".isPalindrome() = true
 *           "Kotlin".isPalindrome() = false
 *           capitalizeWords -> Hello Kotlin World
 *           wordCount -> 4
 *           truncate(10) -> Kotlin ...
 *           truncate(10, "~") -> Kotlin is~
 *           safeRemoveLast("Fahim,") -> Fahim
 *           safeRemoveLast("") -> ""
 *  DESIGN : Every function is an extension on String, has no side
 *           effects, and returns a NEW string. All are tested in
 *           StringUtilsTest, including empty and blank input.
 *  NEW    : Previews of later levels, read them like English:
 *           lambdas `{ it.isLetterOrDigit() }` in filter, joinToString
 *           and replaceFirstChar (level 07), lists from split (level 06),
 *           and a Regex meaning "one or more whitespace characters".
 *  EXTEND : Add `String.toSnakeCase()` ("helloWorld" -> "hello_world").
 * =====================================================================
 */

private const val DEFAULT_ELLIPSIS = "..."

/** Ignores case and anything that is not a letter or digit. "" is a palindrome. */
fun String.isPalindrome(): Boolean {
    val cleaned = filter { it.isLetterOrDigit() }.lowercase()
    return cleaned == cleaned.reversed()
}

/** "hello   kotlin" -> "Hello Kotlin". Collapses extra spaces. */
fun String.capitalizeWords(): String =
    trim()
        .split(WHITESPACE)
        .filter { it.isNotEmpty() }
        .joinToString(" ") { word -> word.lowercase().replaceFirstChar { it.uppercaseChar() } }

/** Number of words separated by any whitespace. "" and "   " have 0 words. */
fun String.wordCount(): Int = if (isBlank()) 0 else trim().split(WHITESPACE).size

/**
 * Shortens the text to at most [maxLength] characters, INCLUDING the
 * ellipsis. Text that already fits is returned unchanged.
 */
fun String.truncate(maxLength: Int, ellipsis: String = DEFAULT_ELLIPSIS): String {
    if (length <= maxLength) return this
    // Only when we really cut does the ellipsis have to fit.
    require(maxLength >= ellipsis.length) { "maxLength ($maxLength) must fit the ellipsis \"$ellipsis\"" }
    return take(maxLength - ellipsis.length) + ellipsis
}

/** Removes the last character only if it equals [char]. Safe on "". */
fun String.safeRemoveLast(char: Char = ','): String = if (endsWith(char)) dropLast(1) else this

// A regular expression: "one or more whitespace characters".
private val WHITESPACE = Regex("\\s+")

fun main() {
    println("\"Racecar\".isPalindrome() = ${"Racecar".isPalindrome()}")
    println("\"Kotlin\".isPalindrome() = ${"Kotlin".isPalindrome()}")
    println("capitalizeWords -> ${"hello   KOTLIN world".capitalizeWords()}")
    println("wordCount -> ${"  Kotlin is really fun ".wordCount()}")
    println("truncate(10) -> ${"Kotlin is fun".truncate(10)}")
    println("truncate(10, \"~\") -> ${"Kotlin is fun".truncate(10, "~")}")
    println("safeRemoveLast(\"Fahim,\") -> ${"Fahim,".safeRemoveLast()}")
    println("safeRemoveLast(\"\") -> \"${"".safeRemoveLast()}\"")
}
