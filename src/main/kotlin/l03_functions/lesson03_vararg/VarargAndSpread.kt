package l03_functions.lesson03_vararg

/**
 * =====================================================================
 *  LESSON 03.3 — vararg and the spread operator
 * =====================================================================
 *  GOAL   : Accept "any number of arguments" with `vararg`, and pass an
 *           existing array into it with the spread operator `*`.
 *  RUN    : ./gradlew -q run -Plesson=l03_functions.lesson03_vararg.VarargAndSpreadKt
 *  OUTPUT :
 *           sum() = 0
 *           sum(5) = 5
 *           sum(1, 2, 3, 4) = 10
 *           numbers inside the function: size=3, first=10
 *           sum(*scores) = 60
 *           sum(1, *scores, 100) = 161
 *           <b>Kotlin</b>
 *           <i><b>Kotlin</b></i>
 *           Shopping: milk, eggs, bread
 *  NEXT   : lesson04_local_unit_nothing/LocalFunctionsUnitNothing.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: vararg ───────────────────────────────────────────
    // `vararg numbers: Int` accepts zero, one or many Ints.
    println("sum() = ${sum()}")
    println("sum(5) = ${sum(5)}")
    println("sum(1, 2, 3, 4) = ${sum(1, 2, 3, 4)}")

    // Inside the function, a vararg is an Array (IntArray here).
    describe(10, 20, 30)

    // ── 2. CONCEPT: spread operator * ────────────────────────────────
    // Already have an array? Spread it into the vararg with `*`.
    val scores = intArrayOf(10, 20, 30)
    println("sum(*scores) = ${sum(*scores)}")
    // You can mix single values and spread arrays:
    println("sum(1, *scores, 100) = ${sum(1, *scores, 100)}")

    // ── 3. CONCEPT: vararg + other parameters ────────────────────────
    // Put the vararg LAST so the other parameters stay positional.
    println(wrap("Kotlin", "b"))
    println(wrap("Kotlin", "i", "b"))

    // If the vararg is not last, the parameters after it must be named.
    printList("milk", "eggs", "bread", title = "Shopping")

    // ── 4. WHY vararg? ───────────────────────────────────────────────
    // The standard library uses it everywhere: listOf(1, 2, 3), setOf(...),
    // arrayOf(...) and maxOf(a, b, c) all take a vararg.
    // (println is NOT one of them: it takes exactly one argument.)

    // ── 5. PITFALL: forgetting the * ─────────────────────────────────
    // sum(scores) // ❌ Argument type mismatch: actual type is 'IntArray', but 'Int' was expected.
    // The function wants Ints, not one array. Spread it: sum(*scores).

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Write `fun average(vararg values: Double): Double` (return 0.0 when empty).
    // b) Write `fun joinWords(separator: String, vararg words: String)`.
    // c) Call sum with two spread arrays: sum(*a, *b).
}

fun sum(vararg numbers: Int): Int {
    var total = 0
    for (n in numbers) total += n
    return total
}

fun describe(vararg numbers: Int) {
    println("numbers inside the function: size=${numbers.size}, first=${numbers[0]}")
}

// Wraps text in HTML tags, innermost tag LAST: wrap("x", "i", "b") -> <i><b>x</b></i>
fun wrap(text: String, vararg tags: String): String {
    var result = text
    for (i in tags.indices.reversed()) {
        result = "<${tags[i]}>$result</${tags[i]}>"
    }
    return result
}

fun printList(vararg items: String, title: String) {
    println("$title: ${items.joinToString(", ")}")
}
