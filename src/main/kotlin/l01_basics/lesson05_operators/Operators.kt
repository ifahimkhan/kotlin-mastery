package l01_basics.lesson05_operators

/**
 * =====================================================================
 *  LESSON 01.5 — Operators
 * =====================================================================
 *  GOAL   : Use arithmetic, assignment, increment, comparison, logical
 *           and equality operators, and know their traps.
 *  RUN    : ./gradlew -q run -Plesson=l01_basics.lesson05_operators.OperatorsKt
 *  OUTPUT :
 *           17 + 5 = 22, 17 - 5 = 12, 17 * 5 = 85, 17 / 5 = 3, 17 % 5 = 2
 *           -17 % 5 = -2  <-- sign follows the left side
 *           (-17).rem(5) = -2, (-17).mod(5) = 3
 *           balance after += 50, -= 20, *= 2 : 260
 *           postfix: printed 5, then counter is 6
 *           prefix : printed 7, counter is 7
 *           3 < 5 = true, 3 >= 5 = false, 3 != 5 = true
 *           checking left... false && (right never runs) = false
 *           checking left... true || (right never runs) = true
 *           first == second  (same content): true
 *           first === second (same object) : false
 *           5 in 1..10 = true, 'z' in "kotlin" = false
 *  NEXT   : lesson06_input/UserInput.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: arithmetic ───────────────────────────────────────
    val a = 17
    val b = 5
    println("$a + $b = ${a + b}, $a - $b = ${a - b}, $a * $b = ${a * b}, $a / $b = ${a / b}, $a % $b = ${a % b}")

    // PITFALL: % is the REMAINDER, and keeps the sign of the left side.
    println("-17 % 5 = ${-17 % 5}  <-- sign follows the left side")
    // rem() is the same as %. mod() always has the sign of the RIGHT side,
    // which is what you want for "wrap around" math like clock hours.
    println("(-17).rem(5) = ${(-17).rem(5)}, (-17).mod(5) = ${(-17).mod(5)}")

    // ── 2. CONCEPT: compound assignment ──────────────────────────────
    var balance = 100
    balance += 50 // balance = balance + 50
    balance -= 20
    balance *= 2
    println("balance after += 50, -= 20, *= 2 : $balance")

    // ── 3. CONCEPT: increment / decrement ────────────────────────────
    // counter++ uses the OLD value, then adds 1 (postfix).
    // ++counter adds 1 first, then uses the NEW value (prefix).
    var counter = 5
    println("postfix: printed ${counter++}, then counter is $counter")
    println("prefix : printed ${++counter}, counter is $counter")
    // Tip: avoid ++ inside bigger expressions; it makes code hard to read.

    // ── 4. CONCEPT: comparison ───────────────────────────────────────
    println("3 < 5 = ${3 < 5}, 3 >= 5 = ${3 >= 5}, 3 != 5 = ${3 != 5}")

    // ── 5. CONCEPT: logical operators short-circuit ──────────────────
    // && stops as soon as the left side is false.
    // || stops as soon as the left side is true.
    // The right side below would print "RIGHT RAN" if it were evaluated.
    println("false && (right never runs) = ${checkLeft(false) && checkRight()}")
    println("true || (right never runs) = ${checkLeft(true) || checkRight()}")

    // ── 6. CONCEPT: equality ─────────────────────────────────────────
    // ==  structural equality: same content? (calls equals())
    // === referential equality: the very same object in memory?
    val first = StringBuilder("hi").toString()
    val second = StringBuilder("hi").toString()
    println("first == second  (same content): ${first == second}")
    println("first === second (same object) : ${first === second}")
    // In Java, == on objects compares references — a classic bug.
    // In Kotlin, == is what you almost always want.

    // ── 7. CONCEPT: `in` ─────────────────────────────────────────────
    println("5 in 1..10 = ${5 in 1..10}, 'z' in \"kotlin\" = ${'z' in "kotlin"}")

    // ── 8. TRY IT ────────────────────────────────────────────────────
    // a) Is 2024 a leap year? (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
    // b) Predict the output of: var x = 1; println(x++ + ++x). Then run it.
    // c) Swap the order in section 5 so checkRight() runs. What changes?
}

// Helper functions: they print, so you can SEE whether they were called.
// (Functions are covered properly in level 03.)
private fun checkLeft(value: Boolean): Boolean {
    print("checking left... ")
    return value
}

private fun checkRight(): Boolean {
    print("RIGHT RAN ")
    return true
}
