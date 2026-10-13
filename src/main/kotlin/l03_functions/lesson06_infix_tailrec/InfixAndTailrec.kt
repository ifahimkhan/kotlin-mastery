package l03_functions.lesson06_infix_tailrec

/**
 * =====================================================================
 *  LESSON 03.6 — Infix functions and tail recursion
 * =====================================================================
 *  GOAL   : Write functions that read like operators (`a times b`),
 *           and recursive functions that can't overflow the stack.
 *  RUN    : ./gradlew -q run -Plesson=l03_functions.lesson06_infix_tailrec.InfixAndTailrecKt
 *  OUTPUT :
 *           1 to "one" = (1, one)
 *           "ab" times 3 = ababab
 *           10 percentOf 250 = 25.0
 *           factorial(5) = 120
 *           countDown(3): 3 2 1 Go!
 *           sumUpTo(100000) = 5000050000
 *           gcd(48, 18) = 6
 *  NEXT   : practice/stringutils/StringUtils.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: infix functions ──────────────────────────────────
    // `infix` lets you call a ONE-parameter member/extension function
    // without the dot and parentheses. You already know one: `to`.
    val pair = 1 to "one" // same as 1.to("one")
    println("1 to \"one\" = $pair")
    println("\"ab\" times 3 = ${"ab" times 3}")
    println("10 percentOf 250 = ${10 percentOf 250}")
    // Other built-in infix functions: until, downTo, step, and, or, shl.

    // ── 2. CONCEPT: recursion ────────────────────────────────────────
    // A function that calls itself. Needs a BASE CASE to stop.
    println("factorial(5) = ${factorial(5)}")
    print("countDown(3): ")
    countDown(3)

    // ── 3. PITFALL: deep recursion → StackOverflowError ──────────────
    // Each call adds a frame to the call stack. A plain recursive sum
    // of 1..100_000 would crash with StackOverflowError:
    //   fun naiveSum(n: Long): Long = if (n == 0L) 0 else n + naiveSum(n - 1)
    // (Not run here on purpose. Try it yourself in the TRY IT section.)

    // ── 4. CONCEPT: tailrec ──────────────────────────────────────────
    // If the recursive call is the VERY LAST thing the function does,
    // mark it `tailrec`: the compiler turns it into a loop. No stack growth.
    println("sumUpTo(100000) = ${sumUpTo(100_000)}")
    println("gcd(48, 18) = ${gcd(48, 18)}")

    // ── 5. WHY tailrec? ──────────────────────────────────────────────
    // Recursive code can be shorter and closer to the math definition
    // (like gcd), and tailrec keeps it as fast and safe as a loop.
    // If the call is NOT in tail position, the compiler warns you:
    // "A function is marked as tail-recursive but no tail calls are found".

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Write `infix fun Int.isDivisibleBy(d: Int): Boolean`.
    // b) Run naiveSum(100_000) inside main and read the error.
    // c) Write a tailrec `power(base, exp, acc = 1L)`.
}

infix fun String.times(count: Int): String = repeat(count)

// Convert to Double BEFORE multiplying: `total * this` as Ints would
// overflow for big values like 50 percentOf 100_000_000 (level 01!).
infix fun Int.percentOf(total: Int): Double = total.toDouble() * this / PERCENT

// NOT tail recursive: after the call returns we still multiply by n.
fun factorial(n: Int): Long = if (n <= 1) 1L else n * factorial(n - 1)

fun countDown(n: Int) {
    if (n == 0) {
        println("Go!")
        return
    }
    print("$n ")
    countDown(n - 1) // last action → would also qualify for tailrec
}

// Tail recursive: the accumulator carries the running total, so the
// recursive call is the last thing that happens.
tailrec fun sumUpTo(n: Long, acc: Long = 0): Long = if (n == 0L) acc else sumUpTo(n - 1, acc + n)

// Euclid's algorithm: gcd(a, b) = gcd(b, a % b) until b is 0.
tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

private const val PERCENT = 100.0
