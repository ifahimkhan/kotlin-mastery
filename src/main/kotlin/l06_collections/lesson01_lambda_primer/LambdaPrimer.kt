package l06_collections.lesson01_lambda_primer

/**
 * =====================================================================
 *  LESSON 06.1 — Just enough lambdas for collections
 * =====================================================================
 *  GOAL   : Read and write the small `{ ... }` blocks that every
 *           collection function takes. (Level 07 goes deep.)
 *  RUN    : ./gradlew -q run -Plesson=l06_collections.lesson01_lambda_primer.LambdaPrimerKt
 *  OUTPUT :
 *           double(4) = 8
 *           add(2, 3) = 5
 *           doubled -> [2, 4, 6, 8, 10]
 *           evens   -> [2, 4]
 *           same result, three spellings: [2, 4] [2, 4] [2, 4]
 *           names longer than 3 letters -> [Grace, Linus]
 *           shout -> [ADA!, GRACE!, LINUS!]
 *  NEXT   : lesson02_lists/Lists.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: a lambda is a function without a name ────────────
    // { parameters -> body }   The last expression is the result.
    val double = { x: Int -> x * 2 }
    val add = { a: Int, b: Int -> a + b }
    println("double(4) = ${double(4)}")
    println("add(2, 3) = ${add(2, 3)}")

    // ── 2. CONCEPT: passing a lambda to a collection function ────────
    // map    : turn EACH item into something else.
    // filter : keep only the items for which the lambda returns true.
    val numbers = listOf(1, 2, 3, 4, 5)
    println("doubled -> ${numbers.map { x -> x * 2 }}")
    println("evens   -> ${numbers.filter { x -> x % 2 == 0 }}")

    // ── 3. CONCEPT: `it` and trailing lambdas ────────────────────────
    // A lambda with ONE parameter can skip naming it: it's called `it`.
    // When the lambda is the last argument, it goes OUTSIDE the ( ),
    // and empty ( ) can be dropped. All three lines mean the same:
    val a = numbers.filter({ n -> n % 2 == 0 })
    val b = numbers.filter { n -> n % 2 == 0 }
    val c = numbers.filter { it % 2 == 0 }
    println("same result, three spellings: $a $b $c")

    // ── 4. CONCEPT: the type of `it` comes from the collection ───────
    // In a List<String>, `it` is a String, so String functions work.
    val names = listOf("Ada", "Grace", "Linus")
    println("names longer than 3 letters -> ${names.filter { it.length > 3 }}")
    println("shout -> ${names.map { it.uppercase() + "!" }}")

    // ── 5. PITFALL: a stored lambda needs parameter types ────────────
    //   val f = { x -> x * 2 }   // ❌ An explicit type is required on a value parameter.
    //   val f = { x: Int -> x * 2 } // ✅
    // Inside numbers.map { ... } the type is known, so you never write it.

    // ── 6. WHY ───────────────────────────────────────────────────────
    // A lambda says WHAT to do with each item; the collection function
    // does the looping. Less code than a for loop, and fewer bugs.
    // Function types, `return` rules and inline functions: level 07.

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) From listOf(3, 8, 1, 9) keep numbers greater than 2, then square them.
    // b) Turn listOf("kotlin", "java") into their lengths with map { it.length }.
    // c) Write val isShort = { s: String -> s.length < 4 } and use it in filter(isShort).
}
