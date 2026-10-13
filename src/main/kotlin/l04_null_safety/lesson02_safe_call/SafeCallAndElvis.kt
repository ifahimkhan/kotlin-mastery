package l04_null_safety.lesson02_safe_call

/**
 * =====================================================================
 *  LESSON 04.2 — Safe calls (?.) and the Elvis operator (?:)
 * =====================================================================
 *  GOAL   : Work with nullable values in one short expression: call
 *           members safely, chain calls, and supply defaults.
 *  RUN    : ./gradlew -q run -Plesson=l04_null_safety.lesson02_safe_call.SafeCallAndElvisKt
 *  OUTPUT :
 *           known?.length   = 5
 *           missing?.length = null
 *           known?.uppercase()?.reversed() = ECILA
 *           missing?.uppercase()?.reversed() = null
 *           city of Alice = LISBON
 *           city of Bob   = null
 *           missing ?: "Guest" = Guest
 *           known ?: "Guest"   = Alice
 *           name length or 0 = 0
 *           city of Bob or default = UNKNOWN CITY
 *           greet("Alice") -> Hello, Alice!
 *           greet(null) -> (nobody to greet)
 *           requireId: IllegalArgumentException -> id is missing
 *  NEXT   : lesson03_not_null/NotNullAssertion.kt
 * =====================================================================
 */
fun main() {
    val known: String? = findName(1)
    val missing: String? = findName(99)

    // ── 1. CONCEPT: safe call ?. ─────────────────────────────────────
    // a?.b means: "if a is not null, call b; otherwise the result is null".
    // The result type is always nullable: known?.length is Int?, not Int.
    println("known?.length   = ${known?.length}")
    println("missing?.length = ${missing?.length}")

    // ── 2. CONCEPT: chains ───────────────────────────────────────────
    // The first null stops the chain; nothing after it runs.
    println("known?.uppercase()?.reversed() = ${known?.uppercase()?.reversed()}")
    println("missing?.uppercase()?.reversed() = ${missing?.uppercase()?.reversed()}")

    // Very useful with nested data that may be missing at any level.
    println("city of Alice = ${findCity(1)?.uppercase()}")
    println("city of Bob   = ${findCity(2)?.uppercase()}")

    // ── 3. CONCEPT: Elvis ?: (default value) ─────────────────────────
    // a ?: b means: "use a if it's not null, otherwise use b".
    // (Turn your head left: ?: looks like Elvis Presley's hair.)
    println("missing ?: \"Guest\" = ${missing ?: "Guest"}")
    println("known ?: \"Guest\"   = ${known ?: "Guest"}")

    // ?. and ?: together = the most common null-handling pattern.
    // The result is a plain Int again: null is gone.
    val nameLength: Int = missing?.length ?: 0
    println("name length or 0 = $nameLength")
    println("city of Bob or default = ${findCity(2)?.uppercase() ?: "UNKNOWN CITY"}")

    // ── 4. CONCEPT: ?: return and ?: throw ───────────────────────────
    // `return` and `throw` are expressions in Kotlin, so they can sit on
    // the right of ?:. This gives very readable "guard" lines.
    println("greet(\"Alice\") -> ${greet("Alice")}")
    println("greet(null) -> ${greet(null)}")

    try { // try/catch catches a crash so the program can continue (later level)
        requireId(null)
    } catch (e: IllegalArgumentException) {
        println("requireId: ${e::class.simpleName} -> ${e.message}")
    }

    // ── 5. WHY ───────────────────────────────────────────────────────
    // Java needs nested `if (a != null && a.b != null && ...)` checks.
    // Kotlin says the same in one line, and the compiler makes sure you
    // never forget the "what if it's null?" case.

    // ── 6. PITFALL: ?: binds loosely ─────────────────────────────────
    // val total = price ?: 0 + tax   means   price ?: (0 + tax)
    // If you want (price ?: 0) + tax, write the parentheses yourself.

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Print the first letter of `missing`, or '?' if there is none.
    // b) Write `fun shout(text: String?): String` that returns the text
    //    upper-cased, or "SILENCE" for null.
    // c) Change findCity so Bob has a city. Which lines change output?
}

// ---- helpers: pretend data sources --------------------------------------

private fun findName(id: Int): String? = if (id == 1) "Alice" else null

private fun findCity(id: Int): String? = if (id == 1) "Lisbon" else null

// `?: return ...` leaves the function early when name is null.
private fun greet(name: String?): String {
    val safeName = name ?: return "(nobody to greet)"
    return "Hello, $safeName!" // here safeName is a plain String
}

private fun requireId(id: Int?): Int = id ?: throw IllegalArgumentException("id is missing")
