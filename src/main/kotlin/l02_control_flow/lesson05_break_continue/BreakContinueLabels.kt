package l02_control_flow.lesson05_break_continue

/**
 * =====================================================================
 *  LESSON 02.5 — break, continue and labels
 * =====================================================================
 *  GOAL   : Leave a loop early with `break`, skip an iteration with
 *           `continue`, and control NESTED loops with labels.
 *  RUN    : ./gradlew -q run -Plesson=l02_control_flow.lesson05_break_continue.BreakContinueLabelsKt
 *  OUTPUT :
 *           first multiple of 7 above 50: 56
 *           odd numbers only: 1 3 5 7 9
 *           without label: (1,1) (2,1) (2,2) (3,1) (3,2) (3,3)
 *           with break@outer: (1,1) (2,1) (2,2) (3,1)
 *           pair summing to 10: 3 + 7
 *  NEXT   : practice/fizzbuzz/FizzBuzz.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: break — stop the loop now ────────────────────────
    var found = 0
    for (n in 51..100) {
        if (n % 7 == 0) {
            found = n
            break // no need to look further
        }
    }
    println("first multiple of 7 above 50: $found")

    // ── 2. CONCEPT: continue — skip to the next iteration ────────────
    print("odd numbers only:")
    for (n in 1..10) {
        if (n % 2 == 0) continue // skip even numbers
        print(" $n")
    }
    println()

    // ── 3. CONCEPT: nested loops ─────────────────────────────────────
    // A plain `break` only leaves the INNERMOST loop.
    print("without label:")
    for (row in 1..3) {
        for (col in 1..3) {
            if (col > row) break // leaves the inner loop only
            print(" ($row,$col)")
        }
    }
    println()

    // ── 4. CONCEPT: labels — break the OUTER loop ────────────────────
    // Name a loop with `name@` and then use `break@name`/`continue@name`.
    print("with break@outer:")
    outer@ for (row in 1..3) {
        for (col in 1..3) {
            if (row == 3 && col == 2) break@outer // leaves BOTH loops
            if (col > row) continue@outer // next row
            print(" ($row,$col)")
        }
    }
    println()

    // ── 5. PRACTICAL: search in two dimensions ───────────────────────
    val numbers = listOf(3, 9, 4, 7, 1) // lists: level 06
    var answer = "none"
    search@ for (i in numbers.indices) {
        for (j in i + 1..<numbers.size) {
            if (numbers[i] + numbers[j] == 10) {
                answer = "${numbers[i]} + ${numbers[j]}"
                break@search
            }
        }
    }
    println("pair summing to 10: $answer")

    // ── 6. WHY labels? ───────────────────────────────────────────────
    // Without labels you'd need a "found" flag checked in every loop.
    // Still, deep nesting is hard to read: often a small function with
    // `return` (level 03) is even clearer than a label.

    // ── 7. PITFALL: break inside `when` in a loop ────────────────────
    // In Kotlin, `break`/`continue` inside `when` affect the LOOP
    // (since Kotlin 1.4). There's no "fall-through" like Java's switch.

    // ── 8. TRY IT ────────────────────────────────────────────────────
    // a) Print numbers 1..50 but stop at the first one divisible by 13.
    // b) Print 1..20 skipping multiples of 3 with continue.
    // c) Find the first (row, col) in a 5x5 grid where row * col == 12.
}
