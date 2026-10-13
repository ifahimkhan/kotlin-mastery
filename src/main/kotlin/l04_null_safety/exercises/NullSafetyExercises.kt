package l04_null_safety.exercises

import l04_null_safety.NullSafetyTasks

/**
 * =====================================================================
 *  LEVEL 04 — EXERCISES (your turn!)
 * =====================================================================
 *  HOW    : Replace each TODO("...") with your answer.
 *           Keep everything else as it is. No `!!` allowed!
 *  CHECK  : ./gradlew exercises --tests "l04_*"
 *           or open src/test/kotlin/l04_null_safety/NullSafetyExercisesTest.kt
 *           and click ▶ next to the class name.
 *  STUCK? : Re-read the lesson in the hint. Still stuck? Peek at
 *           ../solutions/NullSafetySolutions.kt — but try first!
 * =====================================================================
 */
object NullSafetyExercises : NullSafetyTasks {
    // Hint: lesson02_safe_call (?. and ?:)
    override fun lengthOrZero(text: String?): Int = TODO("Ex 1: length or 0")

    // Hint: lesson02_safe_call (firstOrNull() returns null for an empty string)
    override fun firstCharOrDefault(text: String?, default: Char): Char = TODO("Ex 2: first char or default")

    // Hint: l01_basics/lesson06_input (toIntOrNull) + lesson04_let_smart_cast (check the range, then return)
    override fun parseAgeOrNull(text: String): Int? = TODO("Ex 3: valid age 0..150 or null")

    // Hint: lesson04_let_smart_cast (smart casts after null checks). Four cases!
    override fun displayName(firstName: String?, lastName: String?): String = TODO("Ex 4: display name")

    // Hint: lesson02_safe_call (?: with a default). Watch the PITFALL about ?: precedence.
    override fun sumOfNullable(a: Int?, b: Int?): Int = TODO("Ex 5: null counts as 0")

    // Hint: lesson01_nullable (an Int? result may be null): return null instead of
    // crashing with ArithmeticException
    override fun safeDivide(a: Int, b: Int): Int? = TODO("Ex 6: null when dividing by zero")

    // Hint: lesson04_let_smart_cast (after a check, the smart cast does the rest).
    // text.isNullOrBlank() is true for null, "" and "   ".
    override fun uppercaseOrNull(text: String?): String? = TODO("Ex 7: upper case or null")

    // Hint: lesson07_safe_casts (`values` behaves like a list here: try filterNotNull)
    override fun countNonNull(vararg values: Int?): Int = TODO("Ex 8: count non-null values")
}
