package l02_control_flow.lesson03_ranges

/**
 * =====================================================================
 *  LESSON 02.3 — Ranges and progressions
 * =====================================================================
 *  GOAL   : Describe sequences of numbers and characters with `..`,
 *           `..<`, `until`, `downTo` and `step`, and test membership.
 *  RUN    : ./gradlew -q run -Plesson=l02_control_flow.lesson03_ranges.RangesKt
 *  OUTPUT :
 *           1..5       : 1 2 3 4 5
 *           1..<5      : 1 2 3 4
 *           1 until 5  : 1 2 3 4
 *           5 downTo 1 : 5 4 3 2 1
 *           0..10 step 3 : 0 3 6 9
 *           10 downTo 0 step 5 : 10 5 0
 *           (1..4).reversed() : 4 3 2 1
 *           'a'..'e'   : a b c d e
 *           7 in 1..10 = true, 11 in 1..10 = false, 11 !in 1..10 = true
 *           'k' in 'a'..'z' = true
 *           first=0 last=9 count=4
 *           5..1 is empty? true
 *  NEXT   : lesson04_loops/Loops.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: closed and half-open ranges ──────────────────────
    // a..b   includes BOTH ends.
    // a..<b  excludes the end (same as `a until b`). Perfect for indexes:
    //        a string of length 5 has indexes 0..<5.
    println("1..5       : ${show(1..5)}")
    println("1..<5      : ${show(1..<5)}")
    println("1 until 5  : ${show(1 until 5)}")

    // ── 2. CONCEPT: counting down and stepping ───────────────────────
    println("5 downTo 1 : ${show(5 downTo 1)}")
    println("0..10 step 3 : ${show(0..10 step 3)}")
    println("10 downTo 0 step 5 : ${show(10 downTo 0 step 5)}")
    println("(1..4).reversed() : ${show((1..4).reversed())}")

    // ── 3. CONCEPT: character ranges ─────────────────────────────────
    println("'a'..'e'   : ${('a'..'e').joinToString(" ")}")

    // ── 4. CONCEPT: membership with `in` / `!in` ─────────────────────
    // Checking `x in a..b` does NOT build a list; it is just
    // `a <= x && x <= b`, so it's fast even for huge ranges.
    println("7 in 1..10 = ${7 in 1..10}, 11 in 1..10 = ${11 in 1..10}, 11 !in 1..10 = ${11 !in 1..10}")
    println("'k' in 'a'..'z' = ${'k' in 'a'..'z'}")

    // ── 5. CONCEPT: a progression knows its first, last and size ─────
    // 0..10 step 3 ends at 9 — the step never goes past the end.
    val progression = 0..10 step 3
    println("first=${progression.first} last=${progression.last} count=${progression.count()}")

    // ── 6. PITFALL: wrong direction = empty range ────────────────────
    // `5..1` does NOT count down. It is simply empty, so a loop over it
    // runs zero times — silently! Use `5 downTo 1` instead.
    println("5..1 is empty? ${(5..1).isEmpty()}")

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Print every even number from 2 to 20 using step.
    // b) Print the alphabet backwards with 'z' downTo 'a'.
    // c) Check whether a year is in the 21st century (2001..2100).
}

// joinToString glues the values together with a separator (level 06).
private fun show(progression: IntProgression): String = progression.joinToString(" ")
