package l04_null_safety.lesson01_nullable

/**
 * =====================================================================
 *  LESSON 04.1 — Nullable types: String vs String?
 * =====================================================================
 *  GOAL   : Understand what `null` is, why Kotlin splits every type into
 *           a non-null and a nullable version, and what the compiler
 *           forbids you to do with a nullable value.
 *  RUN    : ./gradlew -q run -Plesson=l04_null_safety.lesson01_nullable.NullableTypesKt
 *  OUTPUT :
 *           city = Paris (length 5)
 *           nickname = null
 *           nickname after assignment = Bob
 *           middleName is null? true
 *           middleName length (checked first): unknown
 *           Is null a String?  false
 *           Is null a String?? true
 *           maybe = hi
 *           loaded = hi
 *  NEXT   : lesson02_safe_call/SafeCallAndElvis.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: null means "no value" ────────────────────────────
    // A plain type like `String` can NEVER be null. The compiler checks it.
    val city: String = "Paris"
    // val broken: String = null // ❌ Null cannot be a value of a non-null type 'String'.
    println("city = $city (length ${city.length})") // always safe

    // Add `?` to the type to allow null: `String?` = "a String OR null".
    var nickname: String? = null
    println("nickname = $nickname")
    nickname = "Bob"
    println("nickname after assignment = $nickname")

    // ── 2. CONCEPT: the compiler protects you ────────────────────────
    // Values usually come from somewhere else (a form, a file, a database),
    // so the compiler can't know whether they're null.
    val middleName: String? = lookUpMiddleName("Ada")
    // println(middleName.length)
    //   ❌ Only safe (?.) or non-null asserted (!!.) calls are allowed
    //      on a nullable receiver of type 'String?'.
    // Kotlin refuses to compile code that MIGHT crash. You must decide
    // what happens when the value is missing.
    println("middleName is null? ${middleName == null}")

    // The simplest decision: check for null first (more ways in 04.2).
    val lengthText = if (middleName != null) middleName.length.toString() else "unknown"
    println("middleName length (checked first): $lengthText")

    // ── 3. WHY ───────────────────────────────────────────────────────
    // Tony Hoare, who invented null references in 1965, called them his
    // "billion-dollar mistake": in Java, ANY object can be null, so
    // NullPointerException (NPE) is one of the most common crashes.
    // Kotlin moves the problem from runtime (crash) to compile time
    // (red underline). Non-null is the default; null is opt-in.
    // Ask the type system directly: `is` checks whether a value fits a type.
    // `noValue` holds null (typed Any? so the compiler can't answer early).
    val noValue: Any? = lookUpMiddleName("Ada")
    println("Is null a String?  ${noValue is String}")
    println("Is null a String?? ${noValue is String?}")

    // ── 4. PITFALL: a non-null value fits into a nullable, not back ──
    val sure: String = "hi"
    val maybe: String? = sure // ✅ every String is also a valid String?
    println("maybe = $maybe")
    val loaded: String? = loadGreeting()
    // val back: String = loaded
    //   ❌ Initializer type mismatch: expected 'String', actual 'String?'.
    // Even though loadGreeting() happens to return "hi" today, its TYPE
    // says it may return null, and the compiler trusts the type.
    println("loaded = $loaded")

    // ── 5. TRY IT ────────────────────────────────────────────────────
    // a) Declare `var age: Int? = null`, print it, assign 30, print again.
    // b) Uncomment `println(middleName.length)` and read the error.
    // c) Why does `val back: String = loaded` fail even though it holds "hi"?
}

// Pretend lookups. Their return type `String?` says "might be missing".
private fun lookUpMiddleName(firstName: String): String? = if (firstName == "Ada") null else "Marie"

private fun loadGreeting(): String? = "hi"
