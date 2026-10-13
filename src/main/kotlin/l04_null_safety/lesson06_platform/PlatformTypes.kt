package l04_null_safety.lesson06_platform

/**
 * =====================================================================
 *  LESSON 04.6 — Platform types: null safety at the Java border
 * =====================================================================
 *  GOAL   : Understand why values coming from Java code are NOT checked
 *           for null, and how to protect yourself at that border.
 *  RUN    : ./gradlew -q run -Plesson=l04_null_safety.lesson06_platform.PlatformTypesKt
 *  OUTPUT :
 *           user 1 email (unchecked)  = linus@example.com
 *           user 2 email (unchecked)  = null
 *           Using it as String -> NullPointerException
 *           user 1 email length (safe) = 17
 *           user 2 email length (safe) = 0
 *           System.getProperty("no.such.key") = null
 *  NEXT   : lesson07_safe_casts/SafeCastsAndCollections.kt
 * =====================================================================
 */
fun main() {
    // The Java class lives in src/main/java/l04_null_safety/lesson06_platform/.
    val directory = LegacyUserDirectory()

    // ── 1. CONCEPT: platform types (String!) ─────────────────────────
    // Java's `String findEmail(int id)` doesn't say whether null is
    // possible. Kotlin shows the type as `String!` ("nullability
    // unknown") and lets you treat it as String OR String?.
    // Hover over `unchecked` in IntelliJ to see: String!
    val unchecked = directory.findEmail(1)
    println("user 1 email (unchecked)  = $unchecked")
    println("user 2 email (unchecked)  = ${directory.findEmail(2)}")

    // ── 2. PITFALL: Kotlin trusts you, and you were wrong ────────────
    // Declaring the result as a plain String compiles fine, and the
    // crash moves to the place where the value enters Kotlin code.
    try {
        val email: String = directory.findEmail(2) // Java returned null!
        println(email.length)
    } catch (e: NullPointerException) {
        println("Using it as String -> ${e::class.simpleName}")
    }

    // ── 3. FIX: declare the nullable type yourself ───────────────────
    // Write String? at the border. Now the compiler is back on your side
    // and forces you to handle null everywhere after this line.
    println("user 1 email length (safe) = ${emailLength(directory, 1)}")
    println("user 2 email length (safe) = ${emailLength(directory, 2)}")

    // The Java standard library has the same issue. getProperty returns
    // null for unknown keys, so treat it as String?.
    val missingProperty: String? = System.getProperty("no.such.key")
    println("System.getProperty(\"no.such.key\") = $missingProperty")

    // ── 4. WHY does Kotlin allow this? ───────────────────────────────
    // If every Java value were String?, calling Java would need ?. on
    // every line, even for methods that never return null. Kotlin
    // chose convenience, so the responsibility is YOURS at the border.
    // Java code annotated with @Nullable / @NotNull (JetBrains, JSpecify,
    // AndroidX ...) is understood by Kotlin, and then the type is exact.
    // Plain old Java without annotations is where the danger lives.

    // ── 5. TRY IT ────────────────────────────────────────────────────
    // a) Hover over directory.findEmail(1) in IntelliJ. What type is shown?
    // b) Call System.getenv("HOME_DIR_THAT_DOES_NOT_EXIST") and handle null.
    // c) Rule of thumb: the first time a Java value enters Kotlin, give it
    //    an explicit type. Apply it to section 1.
}

/** Wraps the Java call and decides the nullability once, at the border. */
private fun emailLength(directory: LegacyUserDirectory, id: Int): Int {
    val email: String? = directory.findEmail(id)
    return email?.length ?: 0
}
