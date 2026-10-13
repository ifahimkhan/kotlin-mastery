package l02_control_flow.practice.guessing

import kotlin.random.Random

/**
 * =====================================================================
 *  PRACTICE 02.B — Number guessing game
 * =====================================================================
 *  GOAL   : Combine a loop, `when`, input validation and random numbers
 *           into an interactive game.
 *  RUN    : Click ▶ in IntelliJ and play in the Run window, or
 *           ./gradlew -q run -Plesson=l02_control_flow.practice.guessing.NumberGuessingGameKt
 *  OUTPUT : (example session: the secret is 42)
 *           I'm thinking of a number between 1 and 100.
 *           Your guess: 50
 *           Too high
 *           Your guess: abc
 *           Please type a whole number from 1 to 100.
 *           Your guess: 500
 *           Please type a whole number from 1 to 100.
 *           Your guess: 42
 *           Correct
 *           You got it in 2 tries!
 *  DESIGN : `parseGuess` and `evaluateGuess` are pure (no input, no
 *           output, no randomness), so they are easy to test. `main`
 *           only does I/O. Invalid guesses don't count as tries.
 *  EXTEND : Limit the player to 7 tries, then reveal the number.
 * =====================================================================
 */

const val MIN_SECRET = 1
const val MAX_SECRET = 100

const val TOO_LOW = "Too low"
const val TOO_HIGH = "Too high"
const val CORRECT = "Correct"

/**
 * Turns the player's text into a guess, or null if it isn't a whole
 * number inside MIN_SECRET..MAX_SECRET. (`?.takeIf`: level 04.)
 */
fun parseGuess(text: String): Int? = text.trim().toIntOrNull()?.takeIf { it in MIN_SECRET..MAX_SECRET }

/** Compares a guess with the secret and returns a hint for the player. */
fun evaluateGuess(secret: Int, guess: Int): String =
    when {
        guess < secret -> TOO_LOW
        guess > secret -> TOO_HIGH
        else -> CORRECT
    }

fun main() {
    val secret = Random.nextInt(MIN_SECRET, MAX_SECRET + 1) // upper bound is exclusive
    println("I'm thinking of a number between $MIN_SECRET and $MAX_SECRET.")

    var tries = 0
    // while (true) + break: the loop's only exit is the clear `break` below.
    while (true) {
        print("Your guess: ")
        // readlnOrNull() returns null when input has ENDED (e.g. Ctrl+D, or a
        // piped file ran out). Without this check the loop would never stop.
        val line = readlnOrNull()
        if (line == null) {
            println()
            println("No more input. The number was $secret.")
            return
        }
        val guess = parseGuess(line)
        if (guess == null) {
            println("Please type a whole number from $MIN_SECRET to $MAX_SECRET.")
            continue // skip the rest of this round, ask again
        }
        tries++
        val hint = evaluateGuess(secret, guess)
        println(hint)
        if (hint == CORRECT) break
    }

    println("You got it in $tries tries!")
}
