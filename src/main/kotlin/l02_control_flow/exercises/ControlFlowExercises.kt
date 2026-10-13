package l02_control_flow.exercises

import l02_control_flow.ControlFlowTasks

/**
 * =====================================================================
 *  LEVEL 02 — EXERCISES (your turn!)
 * =====================================================================
 *  HOW    : Replace each TODO("...") with your answer.
 *           Keep everything else as it is. A body can be a single
 *           expression after `=` or a block: `{ ... return result }`.
 *  CHECK  : ./gradlew exercises --tests "l02_*"
 *           or open src/test/kotlin/l02_control_flow/ControlFlowExercisesTest.kt
 *           and click ▶ next to the class name.
 *  STUCK? : Re-read the lesson in the hint. Still stuck? Peek at
 *           ../solutions/ControlFlowSolutions.kt — but try first!
 * =====================================================================
 */
object ControlFlowExercises : ControlFlowTasks {
    // Hint: lesson02_when (when without a subject) or lesson01_if
    override fun sign(n: Int): String = TODO("Ex 1: positive / negative / zero")

    // Hint: lesson02_when (PITFALL: order matters!)
    override fun fizzBuzz(n: Int): String = TODO("Ex 2: FizzBuzz for one number")

    // Hint: lesson04_loops (accumulating a result) + lesson03_ranges (empty ranges)
    override fun sumOfRange(from: Int, to: Int): Long = TODO("Ex 3: sum from..to")

    // Hint: lesson04_loops (for over a String). 'A'.lowercaseChar() == 'a'
    override fun countVowels(text: String): Int = TODO("Ex 4: count a, e, i, o, u")

    // Hint: lesson04_loops (loops) + l01_basics/practice/bmi (require)
    override fun factorial(n: Int): Long = TODO("Ex 5: n! as a Long")

    // Hint: lesson05_break_continue (stop as soon as you find a divisor)
    override fun isPrime(n: Int): Boolean = TODO("Ex 6: is n prime?")

    // Hint: lesson02_when (ranges in branches)
    override fun gradeFor(score: Int): Char = TODO("Ex 7: letter grade, validate 0..100")

    // Hint: lesson04_loops (while). n % 10 = last digit, n / 10 drops it.
    override fun reverseDigits(n: Int): Int = TODO("Ex 8: reverse the digits")
}
