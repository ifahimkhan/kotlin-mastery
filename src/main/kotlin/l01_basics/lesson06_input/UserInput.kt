package l01_basics.lesson06_input

/**
 * =====================================================================
 *  LESSON 01.6 — Reading user input
 * =====================================================================
 *  GOAL   : Read text from the keyboard with `readln()` and turn it
 *           into numbers without crashing on bad input.
 *  RUN    : Click ▶ in IntelliJ and type into the Run window, or
 *           ./gradlew -q run -Plesson=l01_basics.lesson06_input.UserInputKt
 *  OUTPUT : (example session: you type "Sam" and "abc")
 *           What is your name? Sam
 *           Nice to meet you, Sam!
 *           How old are you? abc
 *           "abc" is not a number, assuming 0.
 *           In 10 years you will be 10.
 *  NEXT   : practice/temperature/TemperatureConverter.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: readln() ─────────────────────────────────────────
    // readln() waits for the user to press Enter and returns the line
    // as a String. It is ALWAYS a String, even if the user typed digits.
    print("What is your name? ")
    val name = readln().trim()
    println("Nice to meet you, $name!")

    // ── 2. CONCEPT: text -> number, safely ───────────────────────────
    print("How old are you? ")
    val ageText = readln().trim()

    // toInt() would crash on "abc". toIntOrNull() returns null instead.
    val parsedAge: Int? = ageText.toIntOrNull() // Int? = "an Int or null" (level 04)
    if (parsedAge == null) { // `if` is covered in level 02
        println("\"$ageText\" is not a number, assuming 0.")
    }
    // `?: 0` means "if it's null, use 0 instead".
    val age = parsedAge ?: 0
    println("In 10 years you will be ${age + 10}.")

    // ── 3. WHY validate input? ───────────────────────────────────────
    // Users (and other programs) send unexpected data. A program that
    // crashes on a typo is a broken program. Validate at the boundary:
    // the moment data enters your program.

    // ── 4. PITFALL: end of input ─────────────────────────────────────
    // readln() throws if there is no more input (e.g. input was piped
    // from an empty file). readlnOrNull() returns null instead.

    // ── 5. TRY IT ────────────────────────────────────────────────────
    // a) Ask for two numbers and print their sum.
    // b) Ask for a price with decimals and use toDoubleOrNull().
    // c) Type nothing and press Enter. What does the program do?
}
