package l04_null_safety

/**
 * The list of Level 04 exercises.
 *
 * Both [l04_null_safety.exercises.NullSafetyExercises] (your answers) and
 * [l04_null_safety.solutions.NullSafetySolutions] (reference answers)
 * implement this interface, so the SAME tests can check both.
 *
 * Rule for this level: no `!!` allowed. Use ?. ?: let and smart casts.
 */
interface NullSafetyTasks {
    /** Ex 1. Length of [text], or 0 when it is null.  lengthOrZero(null) == 0 */
    fun lengthOrZero(text: String?): Int

    /**
     * Ex 2. First character of [text], or [default] when text is null OR empty.
     * firstCharOrDefault("kotlin", '?') == 'k',  firstCharOrDefault("", '?') == '?'
     */
    fun firstCharOrDefault(text: String?, default: Char): Char

    /**
     * Ex 3. Parse an age. Returns null when [text] is not a whole number or
     * is outside 0..150. Surrounding spaces are allowed.
     * parseAgeOrNull(" 42 ") == 42,  parseAgeOrNull("abc") == null,  parseAgeOrNull("200") == null
     */
    fun parseAgeOrNull(text: String): Int?

    /**
     * Ex 4. Build a display name from optional parts:
     * ("Ada", "Lovelace") -> "Ada Lovelace", ("Ada", null) -> "Ada",
     * (null, "Lovelace") -> "Lovelace", (null, null) -> "Anonymous".
     */
    fun displayName(firstName: String?, lastName: String?): String

    /** Ex 5. Sum of two optional numbers; a null counts as 0.  sumOfNullable(null, 5) == 5 */
    fun sumOfNullable(a: Int?, b: Int?): Int

    /** Ex 6. Whole-number division, or null when [b] is 0.  safeDivide(7, 2) == 3 */
    fun safeDivide(a: Int, b: Int): Int?

    /**
     * Ex 7. Upper-case version of [text], or null when text is null or blank
     * (empty or only spaces).  uppercaseOrNull("  ") == null
     */
    fun uppercaseOrNull(text: String?): String?

    /** Ex 8. How many of the [values] are not null.  countNonNull(1, null, 3) == 2 */
    fun countNonNull(vararg values: Int?): Int
}
