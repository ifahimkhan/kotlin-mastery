package l01_basics.lesson02_variables

/**
 * =====================================================================
 *  LESSON 01.2 — Variables: val, var and const
 * =====================================================================
 *  GOAL   : Store values in variables. Know when to use `val`, `var`
 *           and `const val`, and how Kotlin infers types.
 *  RUN    : ./gradlew -q run -Plesson=l01_basics.lesson02_variables.VariablesKt
 *  OUTPUT :
 *           name = Ada, age = 36
 *           Next year: age = 37
 *           city is String, population is Int, temperature is Double
 *           Max login attempts: 3
 *           score before = 10, score after = 25
 *  NEXT   : lesson03_types/BasicTypes.kt
 * =====================================================================
 */

// `const val` = a compile-time constant. Allowed only at top level or in
// an object, and only for primitives and String. Name it in UPPER_SNAKE_CASE.
const val MAX_LOGIN_ATTEMPTS = 3

fun main() {
    // ── 1. CONCEPT: val vs var ───────────────────────────────────────
    // val = "value"    → read-only. Assigned once, never changes.
    // var = "variable" → can be reassigned later.
    val name: String = "Ada"
    var age: Int = 36
    println("name = $name, age = $age")

    age = age + 1 // ✅ allowed: `age` is a var
    // name = "Grace" // ❌ 'val' cannot be reassigned.
    println("Next year: age = $age")

    // ── 2. CONCEPT: type inference ───────────────────────────────────
    // You rarely need to write the type: Kotlin infers it from the value.
    val city = "Lisbon" // inferred as String
    val population = 545_000 // inferred as Int (underscores are just for readability)
    val temperature = 21.5 // inferred as Double
    println(
        "city is ${city::class.simpleName}, " +
            "population is ${population::class.simpleName}, " +
            "temperature is ${temperature::class.simpleName}",
    )

    // ── 3. CONCEPT: constants ────────────────────────────────────────
    println("Max login attempts: $MAX_LOGIN_ATTEMPTS")

    // ── 4. CONCEPT: declare now, assign later ────────────────────────
    // A val may be assigned later, but exactly once, before it's read.
    val score: Int
    score = 10
    var updatedScore = score
    updatedScore += 15 // same as: updatedScore = updatedScore + 15
    println("score before = $score, score after = $updatedScore")

    // ── 5. WHY prefer val? ───────────────────────────────────────────
    // A val can't change behind your back, so code is easier to reason
    // about and safer when many threads read it (see level 09).
    // Rule of thumb: start with `val`; switch to `var` only when the
    // compiler tells you that you really need to reassign.

    // ── 6. PITFALL: types are fixed forever ──────────────────────────
    // var count = 5
    // count = "five" // ❌ Assignment type mismatch: actual type is 'String', but 'Int' was expected.

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Declare `val favoriteLanguage` and print it in a sentence.
    // b) Make a `var counter = 0`, add 1 three times, print it.
    // c) Uncomment the `name = "Grace"` line and read the compiler error.
}
