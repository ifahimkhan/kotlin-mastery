package l03_functions.lesson05_extensions

/**
 * =====================================================================
 *  LESSON 03.5 — Extension functions and properties
 * =====================================================================
 *  GOAL   : Add new functions to existing types (even String and Int)
 *           without changing them, and avoid the classic pitfalls.
 *  RUN    : ./gradlew -q run -Plesson=l03_functions.lesson05_extensions.ExtensionFunctionsKt
 *  OUTPUT :
 *           "hello".shout() = HELLO!
 *           7.isOdd() = true, 10.isOdd() = false
 *           "Fahim,".removeLastComma() = Fahim
 *           "Fahim".removeLastComma() = Fahim
 *           "".removeLastComma() = ""
 *           Unsafe version on "" crashed: StringIndexOutOfBoundsException
 *           7.squared = 49
 *           Static type decides: I am a Shape
 *           "a".compareTo("b") = -1 (the member won, not our extension's 42)
 *  NEXT   : lesson06_infix_tailrec/InfixAndTailrec.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: extension function ───────────────────────────────
    // fun <Type>.<name>(...) — inside, `this` is the receiver object.
    println("\"hello\".shout() = ${"hello".shout()}")
    println("7.isOdd() = ${7.isOdd()}, 10.isOdd() = ${10.isOdd()}")

    // ── 2. PITFALL → FIX: removeLastComma ────────────────────────────
    // A common beginner version is:
    //   fun String.removeLastComma(): String = this.substring(0, this.length - 1)
    // It removes the last character WHATEVER it is, and crashes on "".
    println("\"Fahim,\".removeLastComma() = ${"Fahim,".removeLastComma()}")
    println("\"Fahim\".removeLastComma() = ${"Fahim".removeLastComma()}")
    println("\"\".removeLastComma() = \"${"".removeLastComma()}\"")
    try {
        "".unsafeRemoveLastChar()
    } catch (e: StringIndexOutOfBoundsException) {
        println("Unsafe version on \"\" crashed: ${e::class.simpleName}")
    }

    // ── 3. CONCEPT: extension property ───────────────────────────────
    // Extension properties have no storage: they must use a getter.
    println("7.squared = ${7.squared}")

    // ── 4. PITFALL: extensions are resolved STATICALLY ───────────────
    // The DECLARED type picks the extension, not the real object type.
    // (Classes and `open` are level 05; just watch the output.)
    val shape: Shape = Circle()
    println("Static type decides: ${shape.describe()}")

    // ── 5. PITFALL: a member always wins over an extension ───────────
    // String already has a MEMBER function compareTo. Our extension with the
    // same signature (bottom of this file) is silently ignored. The compiler
    // warns: "This extension is shadowed by a member: ..."
    println("\"a\".compareTo(\"b\") = ${"a".compareTo("b")} (the member won, not our extension's 42)")

    // ── 6. WHY extensions? ───────────────────────────────────────────
    // You can't edit String's source code, but you can make
    // `email.isValidEmail()` read like it was always there.
    // Under the hood it's a plain static function: isOdd(7).
    // It can't see private members of the class.

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Write `fun Int.isPositive(): Boolean`.
    // b) Write `val String.firstWord: String` (hint: substringBefore(" ")).
    // c) Write `fun String.removeSuffixIfPresent(suffix: String)` and
    //    compare it with the built-in removeSuffix().
}

fun String.shout(): String = uppercase() + "!"

fun Int.isOdd(): Boolean = this % 2 != 0

// SAFE: only removes a comma that is really there. Works on "".
fun String.removeLastComma(): String = if (endsWith(",")) dropLast(1) else this

// UNSAFE: kept only to show the crash.
fun String.unsafeRemoveLastChar(): String = substring(0, length - 1)

val Int.squared: Int
    get() = this * this

open class Shape

class Circle : Shape()

fun Shape.describe(): String = "I am a Shape"

fun Circle.describe(): String = "I am a Circle"

// Never called: String's own member compareTo always takes priority.
// The @Suppress hides the compiler warning only because this is a demo.
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun String.compareTo(other: String): Int = SHADOWED_RESULT

private const val SHADOWED_RESULT = 42
