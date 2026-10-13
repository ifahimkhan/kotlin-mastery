package l03_functions.lesson04_local_unit_nothing

/**
 * =====================================================================
 *  LESSON 03.4 — Local functions, Unit and Nothing
 * =====================================================================
 *  GOAL   : Hide helper logic inside a function, understand what a
 *           function returns when it "returns nothing" (Unit), and meet
 *           the type of functions that NEVER return (Nothing).
 *  RUN    : ./gradlew -q run -Plesson=l03_functions.lesson04_local_unit_nothing.LocalFunctionsUnitNothingKt
 *  OUTPUT :
 *           Registered: alice (alice@example.com)
 *           Rejected: Username must not be blank
 *           Rejected: Email must contain @
 *           sayHi returned: kotlin.Unit
 *           Parsed port: 8080
 *           Caught: Invalid port: abc
 *           Caught: NotImplementedError
 *  NEXT   : lesson05_extensions/ExtensionFunctions.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: local functions ──────────────────────────────────
    // A function declared INSIDE another function. Only its parent can
    // call it, and it can read the parent's parameters.
    println(register("alice", "alice@example.com"))
    println(register("  ", "bob@example.com"))
    println(register("carol", "carol.example.com"))

    // ── 2. CONCEPT: Unit ─────────────────────────────────────────────
    // A function without a useful result returns `Unit` (like Java's
    // void, but Unit is a real object you can print).
    val result = sayHi()
    println("sayHi returned: $result")
    // `fun sayHi() { ... }` is short for `fun sayHi(): Unit { ... }`.

    // ── 3. CONCEPT: Nothing ──────────────────────────────────────────
    // `Nothing` is the return type of a function that never finishes
    // normally: it always throws (or loops forever).
    println("Parsed port: ${parsePort("8080")}")
    // try/catch catches a thrown exception (covered in depth in a later level).
    try {
        parsePort("abc")
    } catch (e: IllegalArgumentException) {
        println("Caught: ${e.message}")
    }

    // TODO() is a built-in function returning Nothing. That's why the
    // exercises compile: Nothing fits where ANY type is expected.
    try {
        notWrittenYet()
    } catch (e: NotImplementedError) {
        println("Caught: ${e::class.simpleName}")
    }

    // ── 4. WHY Nothing matters ───────────────────────────────────────
    // The compiler knows code after a Nothing call can't run, so this
    // compiles without a warning about a missing value:
    //   val port: Int = text.toIntOrNull() ?: fail("bad port")

    // ── 5. PITFALL: inferred return types ────────────────────────────
    // fun total(a: Int, b: Int) { a + b }   // returns Unit, not Int!
    // println(total(1, 2))                   // prints kotlin.Unit 😱
    // Block body without `: Int` means Unit. Use `= a + b` or add `: Int`.

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Add a local function `isTooShort()` to register (min 3 chars).
    // b) Write `fun fail(message: String): Nothing = throw IllegalStateException(message)`.
    // c) Check the pitfall above yourself: write total() and print it.
}

fun register(username: String, email: String): String {
    // Local function: reads `username` and `email` from its parent.
    fun validationError(): String? {
        if (username.isBlank()) return "Username must not be blank"
        if (!email.contains("@")) return "Email must contain @"
        return null
    }

    val error = validationError()
    return if (error != null) "Rejected: $error" else "Registered: $username ($email)"
}

fun sayHi() {
    // Prints nothing; returns Unit.
}

fun parsePort(text: String): Int = text.toIntOrNull() ?: fail("Invalid port: $text")

// Always throws, so its return type is Nothing.
fun fail(message: String): Nothing = throw IllegalArgumentException(message)

fun notWrittenYet(): Int = TODO("Write me later")
