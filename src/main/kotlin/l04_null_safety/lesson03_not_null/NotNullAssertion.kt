package l04_null_safety.lesson03_not_null

/**
 * =====================================================================
 *  LESSON 04.3 — The not-null assertion (!!) and better alternatives
 * =====================================================================
 *  GOAL   : See what `!!` does, why it brings back the crash Kotlin
 *           tried to remove, and what to write instead.
 *  RUN    : ./gradlew -q run -Plesson=l04_null_safety.lesson03_not_null.NotNullAssertionKt
 *  OUTPUT :
 *           present!!.length = 6
 *           missing!!.length -> crashed with NullPointerException
 *           requireNotNull -> IllegalArgumentException: User id 42 not found
 *           checkNotNull -> IllegalStateException: Config was not loaded
 *           Elvis default -> 0
 *  NEXT   : lesson04_let_smart_cast/LetAndSmartCasts.kt
 * =====================================================================
 */
fun main() {
    val present: String? = loadSetting("theme")
    val missing: String? = loadSetting("font")

    // ── 1. CONCEPT: !! means "trust me, it's not null" ───────────────
    // It converts String? into String. If the value IS null, it throws
    // a NullPointerException right there.
    println("present!!.length = ${present!!.length}")

    // try/catch lets us SEE the crash without stopping the lesson.
    try {
        println(missing!!.length)
    } catch (e: NullPointerException) {
        println("missing!!.length -> crashed with ${e::class.simpleName}")
    }

    // ── 2. WHY avoid !! ──────────────────────────────────────────────
    // - It hides the "what if it's null?" decision instead of making it.
    // - The NPE message says nothing about WHAT was missing or WHY.
    // - Each !! is a small bet that the code around it never changes.
    // Many teams treat every `!!` as a code-review red flag.

    // ── 3. BETTER: requireNotNull / checkNotNull ─────────────────────
    // Same idea (stop if null), but with a clear message and the right
    // exception type:
    //   requireNotNull -> IllegalArgumentException ("bad INPUT")
    //   checkNotNull   -> IllegalStateException    ("bad STATE")
    try {
        findUserName(42)
    } catch (e: IllegalArgumentException) {
        println("requireNotNull -> ${e::class.simpleName}: ${e.message}")
    }

    try {
        readConfig(loaded = null)
    } catch (e: IllegalStateException) {
        println("checkNotNull -> ${e::class.simpleName}: ${e.message}")
    }

    // ── 4. BEST: don't crash at all ──────────────────────────────────
    // Usually missing data has a sensible default. Use ?: (lesson 04.2).
    println("Elvis default -> ${missing?.length ?: 0}")

    // ── 5. PITFALL: "it can't be null here" ──────────────────────────
    // That's what every developer thinks before the crash. Common
    // traps: data from Java code, from JSON, from a database, or a var
    // that another part of the program changes. Prefer ?. ?: and let.

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Replace `present!!.length` with a version that can't crash.
    // b) Call requireNotNull(missing) without a message. Read the message you get.
    // c) Find the difference: when would you use require vs check?
}

// ---- helpers: pretend data sources --------------------------------------

private fun loadSetting(key: String): String? = if (key == "theme") "dark-2" else null

private fun findUserName(id: Int): String {
    val name: String? = loadSetting("user-$id") // nothing stored for users
    return requireNotNull(name) { "User id $id not found" }
}

private fun readConfig(loaded: String?): String = checkNotNull(loaded) { "Config was not loaded" }
