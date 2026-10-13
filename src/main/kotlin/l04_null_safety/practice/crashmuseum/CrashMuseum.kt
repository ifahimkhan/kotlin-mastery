package l04_null_safety.practice.crashmuseum

/**
 * =====================================================================
 *  PRACTICE 04.B — The Crash Museum
 * =====================================================================
 *  GOAL   : Walk past five famous crashes. Each exhibit shows the
 *           crashing code (safely caught) and then the safe fix.
 *  RUN    : ./gradlew -q run -Plesson=l04_null_safety.practice.crashmuseum.CrashMuseumKt
 *  OUTPUT :
 *           Exhibit 1: the not-null assertion
 *             crash: NullPointerException
 *             fix  : Guest
 *           Exhibit 2: parsing text as a number
 *             crash: NumberFormatException
 *             fix  : 0
 *           Exhibit 3: lateinit read too early
 *             crash: UninitializedPropertyAccessException
 *             fix  : not loaded yet
 *           Exhibit 4: the hard cast
 *             crash: ClassCastException
 *             fix  : -1
 *           Exhibit 5: substring of an empty string
 *             crash: StringIndexOutOfBoundsException
 *             fix  : ""
 *  DESIGN : Every "fix" is a small pure function, tested in
 *           CrashMuseumTest. Crashes are caught with try/catch only to
 *           SHOW them; real code should use the fixes instead.
 *  NEW    : `crash: () -> Any?` is a parameter that holds a piece of
 *           code (a lambda). Lambdas are level 07; here they just let
 *           each exhibit run its crash inside a try/catch.
 *  EXTEND : Add exhibit 6: list[5] on a 3-element list
 *           (IndexOutOfBoundsException) and fix it with getOrNull(5).
 * =====================================================================
 */

const val GUEST_NAME = "Guest"
const val NOT_LOADED = "not loaded yet"

// ---- the fixes: small pure functions, tested in CrashMuseumTest ----------

fun nameOrGuest(name: String?): String = name ?: GUEST_NAME

fun parseOrZero(text: String): Int = text.toIntOrNull() ?: 0

fun lengthIfString(value: Any): Int = (value as? String)?.length ?: -1

/**
 * Removes the last character. dropLast never crashes: on "" it returns "".
 * (Don't return a marker like "(empty)": callers couldn't tell it from real text.)
 */
fun dropLastChar(text: String): String = text.dropLast(1)

class Settings {
    lateinit var theme: String

    fun themeOrMessage(): String = if (::theme.isInitialized) theme else NOT_LOADED
}

// ---- the exhibits ---------------------------------------------------------

/** Runs [crash], prints the exception's simple name, then prints [fix]. */
private fun exhibit(number: Int, title: String, crash: () -> Any?, fix: () -> Any?) {
    println("Exhibit $number: $title")
    try {
        crash()
        println("  crash: (none?!)")
    } catch (e: RuntimeException) {
        println("  crash: ${e::class.simpleName}")
    }
    println("  fix  : ${fix()}")
}

fun main() {
    val missingName: String? = System.getProperty("crash.museum.visitor") // not set -> null
    exhibit(1, "the not-null assertion", crash = { missingName!!.length }, fix = { nameOrGuest(missingName) })

    exhibit(2, "parsing text as a number", crash = { "twelve".toInt() }, fix = { parseOrZero("twelve") })

    val settings = Settings()
    exhibit(3, "lateinit read too early", crash = { settings.theme }, fix = { settings.themeOrMessage() })

    val box: Any = 42
    exhibit(4, "the hard cast", crash = { (box as String).length }, fix = { lengthIfString(box) })

    // A classic bug: "remove the last character" with substring(0, length - 1).
    val empty = ""
    exhibit(
        5,
        "substring of an empty string",
        crash = { empty.substring(0, empty.length - 1) },
        fix = { "\"${dropLastChar(empty)}\"" }, // quotes make the empty result visible
    )
}
