package l04_null_safety.solutions

import l04_null_safety.NullSafetyTasks

/**
 * Reference answers for Level 04. Try the exercises first!
 * Each answer explains WHY it is written this way.
 */
object NullSafetySolutions : NullSafetyTasks {
    // ?. gives Int? (null if text is null); ?: replaces null with 0.
    override fun lengthOrZero(text: String?): Int = text?.length ?: 0

    // firstOrNull() handles the empty string; ?. handles the null string.
    // text?.first() would CRASH on "" (NoSuchElementException).
    override fun firstCharOrDefault(text: String?, default: Char): Char = text?.firstOrNull() ?: default

    // Parse first (null on bad text), then validate the range.
    // `?: return null` leaves early, so `age` is a plain Int afterwards.
    override fun parseAgeOrNull(text: String): Int? {
        val age = text.trim().toIntOrNull() ?: return null
        return if (age in MIN_AGE..MAX_AGE) age else null
    }

    // After the null checks, the compiler smart-casts both values to String.
    // (`when` without a subject is covered in level 02.)
    override fun displayName(firstName: String?, lastName: String?): String =
        when {
            firstName != null && lastName != null -> "$firstName $lastName"
            firstName != null -> firstName
            lastName != null -> lastName
            else -> ANONYMOUS
        }

    // Parentheses matter: a ?: 0 + b ?: 0 would mean a ?: (0 + b) ?: 0.
    override fun sumOfNullable(a: Int?, b: Int?): Int = (a ?: 0) + (b ?: 0)

    // Represent "no answer" as null instead of throwing ArithmeticException.
    override fun safeDivide(a: Int, b: Int): Int? = if (b == 0) null else a / b

    // isNullOrBlank() is an extension on String?; after it returns false,
    // `text` is smart-cast to String, so uppercase() needs no ?.
    override fun uppercaseOrNull(text: String?): String? = if (text.isNullOrBlank()) null else text.uppercase()

    // A vararg parameter is an Array<out Int?>; filterNotNull drops the nulls.
    override fun countNonNull(vararg values: Int?): Int = values.filterNotNull().size

    private const val MIN_AGE = 0
    private const val MAX_AGE = 150
    private const val ANONYMOUS = "Anonymous"
}
