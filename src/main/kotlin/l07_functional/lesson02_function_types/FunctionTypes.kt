package l07_functional.lesson02_function_types

/**
 * =====================================================================
 *  LESSON 07.2 — Function types
 * =====================================================================
 *  GOAL   : Write the TYPE of a function, store functions in variables
 *           and lists, call them, and handle nullable function types.
 *  RUN    : ./gradlew -q run -Plesson=l07_functional.lesson02_function_types.FunctionTypesKt
 *  OUTPUT :
 *           add(2, 3) = 5
 *           add.invoke(2, 3) = 5
 *           now() = tick
 *           log("saved") printed:
 *           [log] saved
 *           10 -> double -> 20
 *           10 -> negate -> -10
 *           10 -> square -> 100
 *           no callback set: null
 *           callback set: done!
 *           discount(200) = 180
 *  NEXT   : lesson03_higher_order/HigherOrderFunctions.kt
 * =====================================================================
 */

// ── typealias: a short, meaningful name for a long function type ──────
typealias PriceRule = (Int) -> Int

fun main() {
    // ── 1. CONCEPT: reading a function type ──────────────────────────
    //   (Int, Int) -> Int   "takes two Ints, returns an Int"
    //   () -> String        "takes nothing, returns a String"
    //   (String) -> Unit    "takes a String, returns nothing useful"
    val add: (Int, Int) -> Int = { a, b -> a + b } // types come from the variable
    val now: () -> String = { "tick" }
    val log: (String) -> Unit = { message -> println("[log] $message") }

    // ── 2. CONCEPT: calling ─────────────────────────────────────────
    // f(x) is short for f.invoke(x). Both do the same thing.
    println("add(2, 3) = ${add(2, 3)}")
    println("add.invoke(2, 3) = ${add.invoke(2, 3)}")
    println("now() = ${now()}")
    println("log(\"saved\") printed:")
    log("saved")

    // ── 3. CONCEPT: functions are values ────────────────────────────
    // They can live in lists and maps like any other value.
    val steps: Map<String, (Int) -> Int> =
        mapOf(
            "double" to { it * 2 },
            "negate" to { -it },
            "square" to { it * it },
        )
    for ((name, step) in steps) {
        println("10 -> $name -> ${step(10)}")
    }

    // ── 4. CONCEPT: nullable function types ─────────────────────────
    // Parentheses matter:
    //   ((Int) -> Int)?   the FUNCTION may be null
    //   (Int) -> Int?     the function is never null, its RESULT may be
    val noCallback: (() -> String)? = callbackFor(enabled = false)
    println("no callback set: ${noCallback?.invoke()}")
    val callback: (() -> String)? = callbackFor(enabled = true)
    println("callback set: ${callback?.invoke()}")

    // PITFALL: you can't call a nullable function directly.
    //   val f: ((Int) -> Int)? = null
    //   f(1)
    //   // ❌ Reference has a nullable type '((Int) -> Int)?'. Use explicit '?.invoke'
    //   //    to make a function-like call instead.

    // ── 5. CONCEPT: typealias ────────────────────────────────────────
    // PriceRule (top of file) reads better than (Int) -> Int everywhere.
    // It is only a new NAME, not a new type: any (Int) -> Int fits.
    // 10% off. Int division truncates: price 15 gives 15 - 1 = 14, not 13.5.
    val discount: PriceRule = { price -> price - price / 10 }
    println("discount(200) = ${discount(200)}")

    // ── 6. WHY ───────────────────────────────────────────────────────
    // Once functions are values, you can pass behaviour around: "here is
    // HOW to sort / filter / react". That is the whole idea of the next
    // lesson, higher-order functions.

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Declare val isLong: (String) -> Boolean and test it on two words.
    // b) Add a "half" step to the steps map. Where does it appear in the output, and why?
    // c) Write typealias Validator = (String) -> Boolean and store two validators in a list.
}

// Returns a callback or null. (A helper, so the compiler can't know the
// answer in advance and smart-cast the variable to non-null.)
private fun callbackFor(enabled: Boolean): (() -> String)? {
    if (!enabled) return null
    return { "done!" }
}
