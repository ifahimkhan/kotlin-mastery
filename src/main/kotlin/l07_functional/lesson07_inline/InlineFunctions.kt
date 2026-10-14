package l07_functional.lesson07_inline

/**
 * =====================================================================
 *  LESSON 07.7 — Inline functions, returns from lambdas, noinline, crossinline
 * =====================================================================
 *  GOAL   : Know what `inline` does, why `return` behaves differently
 *           inside lambdas, and when you need noinline / crossinline.
 *  RUN    : ./gradlew -q run -Plesson=l07_functional.lesson07_inline.InlineFunctionsKt
 *  OUTPUT :
 *           [before] work [after]
 *           firstNegative([4, -2, 7, -9]) = -2
 *           positives: 4 7
 *           labelled: 4 7
 *           saved actions: 1
 *           scheduled task ran: hello
 *  NEXT   : lesson08_receivers/LambdasWithReceiver.kt
 * =====================================================================
 */

// ── 1. inline: the compiler COPIES the body into each call site ───────
// Without inline, every lambda passed here becomes a function object at
// runtime. With inline, the lambda's code is pasted in place: no object.
// That's why the standard library marks forEach, map, filter, let,
// apply... as inline.
inline fun wrap(block: () -> Unit) {
    print("[before] ")
    block()
    println(" [after]")
}

// ── 2. non-local return ───────────────────────────────────────────────
// forEach is inline, so a bare `return` inside its lambda returns from
// the ENCLOSING function (firstNegative), not just from the lambda.
fun firstNegative(numbers: List<Int>): Int? {
    numbers.forEach {
        if (it < 0) return it // leaves firstNegative immediately
    }
    return null
}

// ── 3. return@label: leave only the lambda (like `continue`) ──────────
fun printPositives(numbers: List<Int>) {
    print("positives:")
    numbers.forEach {
        if (it < 0) return@forEach // skip this item, keep looping
        print(" $it")
    }
    println()
}

// You can name the label yourself.
fun printPositivesLabelled(numbers: List<Int>) {
    print("labelled:")
    numbers.forEach each@{
        if (it < 0) return@each
        print(" $it")
    }
    println()
}

// ── 4. noinline: keep ONE lambda as a real object ─────────────────────
// An inlined lambda has no object, so it can't be stored. Mark the one
// you want to store as noinline. (Both are private: a public inline
// function may only touch public declarations, because its body is
// copied into callers that can't see private ones.)
private val savedActions = mutableListOf<() -> Unit>()

private inline fun runAndSave(now: () -> Unit, noinline later: () -> Unit) {
    now()
    savedActions.add(later) // allowed: `later` is a real object
}

// ── 5. crossinline: inlined, but called from ANOTHER context ──────────
// Here block() runs inside ANOTHER lambda (the one we return), i.e. in a
// different function that runs later. A non-local `return` from there
// would make no sense, so crossinline forbids it for callers.
inline fun schedule(crossinline block: () -> String): () -> Unit =
    { println("scheduled task ran: ${block()}") }

fun main() {
    wrap { print("work") }

    println("firstNegative([4, -2, 7, -9]) = ${firstNegative(listOf(4, -2, 7, -9))}")
    printPositives(listOf(4, -2, 7, -9))
    printPositivesLabelled(listOf(4, -2, 7, -9))

    runAndSave(now = {}, later = { println("never called in this demo") })
    println("saved actions: ${savedActions.size}")

    val task = schedule { "hello" } // nothing printed yet: we only got a function back
    task() // now the stored lambda runs, and calls block() inside it

    // ── 6. PITFALL: bare return in a NON-inline lambda ──────────────
    //   val check = { n: Int -> if (n < 0) return; n }
    //   // ❌ 'return' is prohibited here.
    // A plain lambda is not inlined, so there is no enclosing function to
    // return from. Use the last expression, or return@label.

    // ── 7. PITFALL: storing an inline parameter ─────────────────────
    //   inline fun keep(block: () -> Unit) { val saved = block }
    //   // ❌ Illegal usage of inline parameter 'block: () -> Unit'. Add 'noinline'
    //   //    modifier to the parameter declaration.

    // ── 8. PITFALL: calling an inline parameter from another lambda ──
    //   inline fun later(block: () -> Unit): () -> Unit = { block() }
    //   // ❌ Cannot inline 'block: () -> Unit' here: it might contain non-local
    //   //    returns. Add 'crossinline' modifier to parameter declaration
    //   //    'block: () -> Unit'.

    // ── 9. WHY / WHEN ───────────────────────────────────────────────
    // Use inline for SMALL functions that take lambdas. Inlining a big
    // function copies a lot of code into every caller, and inlining a
    // function without lambda parameters gains nothing. The compiler
    // warns: "Expected performance impact from inlining is insignificant.
    // Inlining works best for functions with parameters of function types."
    // `inline` + `reified` type parameters: level 08.

    // ── 10. TRY IT ──────────────────────────────────────────────────
    // a) Remove `inline` from wrap. Does main still compile? Why?
    // b) Write fun firstLongWord(words: List<String>): String? with forEach + return.
    // c) Change return@forEach in printPositives to plain return. What is printed now?
}
