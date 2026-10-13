package l02_control_flow.lesson04_loops

/**
 * =====================================================================
 *  LESSON 02.4 — Loops: for, while, do-while, repeat
 * =====================================================================
 *  GOAL   : Repeat work with `for` over ranges and strings, `while`,
 *           `do-while` and `repeat`, and pick the right loop.
 *  RUN    : ./gradlew -q run -Plesson=l02_control_flow.lesson04_loops.LoopsKt
 *  OUTPUT :
 *           for 1..3: 1 2 3
 *           for over a String: K-o-t-l-i-n-
 *           indices: 0=c 1=a 2=t
 *           withIndex: #0 red, #1 green, #2 blue
 *           sum of 1..100 = 5050
 *           while: 1 2 4 8 16 32 64
 *           do-while ran 1 time even though the condition was false
 *           repeat: Hip Hip Hip Hooray!
 *  NEXT   : lesson05_break_continue/BreakContinueLabels.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: for over a range ─────────────────────────────────
    // `for (item in something)` visits each element once. There is NO
    // C-style `for (int i = 0; i < n; i++)` in Kotlin: use ranges.
    print("for 1..3:")
    for (i in 1..3) {
        print(" $i")
    }
    println()

    // ── 2. CONCEPT: for over a String ────────────────────────────────
    print("for over a String: ")
    for (letter in "Kotlin") {
        print("$letter-")
    }
    println()

    // ── 3. CONCEPT: when you need the index ──────────────────────────
    val word = "cat"
    print("indices:")
    for (i in word.indices) { // indices = 0..<word.length
        print(" $i=${word[i]}")
    }
    println()

    // withIndex() gives both index and value at once.
    val colors = listOf("red", "green", "blue") // lists: level 06
    val parts = mutableListOf<String>()
    for ((index, color) in colors.withIndex()) {
        parts.add("#$index $color")
    }
    println("withIndex: ${parts.joinToString(", ")}")

    // ── 4. CONCEPT: accumulating a result ────────────────────────────
    var sum = 0
    for (n in 1..100) {
        sum += n
    }
    println("sum of 1..100 = $sum")

    // ── 5. CONCEPT: while — repeat WHILE a condition is true ─────────
    // Use it when you don't know in advance how many times to loop.
    var power = 1
    print("while:")
    while (power < 100) {
        print(" $power")
        power *= 2
    }
    println()

    // ── 6. CONCEPT: do-while — body runs at least once ───────────────
    // The condition is checked AFTER the body. Typical use: "ask the
    // user until the answer is valid".
    var attempts = 0
    do {
        attempts++
    } while (attempts < 0) // false from the start...
    println("do-while ran $attempts time even though the condition was false")

    // ── 7. CONCEPT: repeat(n) ────────────────────────────────────────
    print("repeat: ")
    repeat(3) {
        print("Hip ")
    }
    println("Hooray!")

    // ── 8. WHY so many loops? ────────────────────────────────────────
    //   know the items / count?  -> for (or repeat)
    //   loop until something happens? -> while
    //   must run at least once? -> do-while

    // ── 9. PITFALL: infinite loops ───────────────────────────────────
    //   var i = 0
    //   while (i < 10) { println(i) }   // ❌ forgot i++ -> never ends!
    // And you can't change the loop variable of a for: `i` is a val.

    // ── 10. TRY IT ───────────────────────────────────────────────────
    // a) Print the 7 times table (7 x 1 = 7 ... 7 x 10 = 70).
    // b) Count the spaces in a sentence with a for loop.
    // c) With while, find the first power of 3 greater than 1000.
}
