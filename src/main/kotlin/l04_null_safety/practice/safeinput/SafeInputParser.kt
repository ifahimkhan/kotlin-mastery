package l04_null_safety.practice.safeinput

/**
 * =====================================================================
 *  PRACTICE 04.A — Safe input parser (CLI)
 * =====================================================================
 *  GOAL   : Turn messy user text into clean values without a single
 *           crash, using nullable return types to mean "invalid".
 *  RUN    : ./gradlew -q run -Plesson=l04_null_safety.practice.safeinput.SafeInputParserKt
 *  OUTPUT : (example session: you type "abc", "5", "maybe", "Y")
 *           How many tickets (1-10)? abc
 *           Please enter a whole number from 1 to 10.
 *           How many tickets (1-10)? 5
 *           Add insurance (yes/no)? maybe
 *           Please answer yes or no.
 *           Add insurance (yes/no)? Y
 *           Order: 5 ticket(s), insurance: yes
 *  DESIGN : The parse functions are pure (no input/output) and return
 *           null for invalid text. `main` only asks again until it gets
 *           a non-null value. Tests live in SafeInputParserTest.
 *  EXTEND : Add parseEmailOrNull(text) that requires one '@' and a '.'
 *           after it, and ask for an email address.
 * =====================================================================
 */

private const val MIN_TICKETS = 1
private const val MAX_TICKETS = 10

private val YES_WORDS = setOf("y", "yes", "true", "1")
private val NO_WORDS = setOf("n", "no", "false", "0")

/** Whole number inside [range], or null. Spaces around the number are ignored. */
fun parseIntInRange(text: String?, range: IntRange): Int? {
    val number = text?.trim()?.toIntOrNull() ?: return null
    return if (number in range) number else null
}

/** true for yes-like answers, false for no-like answers, null for anything else. */
fun parseYesNo(text: String?): Boolean? {
    val answer = text?.trim()?.lowercase() ?: return null
    return when (answer) {
        in YES_WORDS -> true
        in NO_WORDS -> false
        else -> null
    }
}

/** Text with surrounding spaces removed, or null when nothing is left. */
fun parseNonBlank(text: String?): String? = text?.trim()?.takeIf { it.isNotEmpty() }

/**
 * Keeps asking until [parse] returns a non-null value.
 * Returns null only when the input ends (readlnOrNull() gives null).
 * (Passing a function as a parameter is explained in level 07, and the
 * generic type parameter <T : Any> = "works for any non-null type" in level 08.)
 */
private fun <T : Any> askUntilValid(question: String, errorMessage: String, parse: (String) -> T?): T? {
    while (true) {
        print(question)
        val line = readlnOrNull() ?: return null
        val value = parse(line)
        if (value != null) return value
        println(errorMessage)
    }
}

fun main() {
    val tickets =
        askUntilValid(
            "How many tickets ($MIN_TICKETS-$MAX_TICKETS)? ",
            "Please enter a whole number from $MIN_TICKETS to $MAX_TICKETS.",
        ) { parseIntInRange(it, MIN_TICKETS..MAX_TICKETS) } ?: return

    val insurance =
        askUntilValid("Add insurance (yes/no)? ", "Please answer yes or no.") { parseYesNo(it) } ?: return

    println("Order: $tickets ticket(s), insurance: ${if (insurance) "yes" else "no"}")
}
