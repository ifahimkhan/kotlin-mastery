package l02_control_flow.practice.fizzbuzz

/**
 * =====================================================================
 *  PRACTICE 02.A — FizzBuzz three ways
 * =====================================================================
 *  GOAL   : Solve the same problem with an if-chain, a `when` and a
 *           range transformation, and compare readability.
 *  RUN    : ./gradlew -q run -Plesson=l02_control_flow.practice.fizzbuzz.FizzBuzzKt
 *  OUTPUT :
 *           if-chain : 1 2 Fizz 4 Buzz Fizz 7 8 Fizz Buzz 11 Fizz 13 14 FizzBuzz
 *           when     : 1 2 Fizz 4 Buzz Fizz 7 8 Fizz Buzz 11 Fizz 13 14 FizzBuzz
 *           map      : 1 2 Fizz 4 Buzz Fizz 7 8 Fizz Buzz 11 Fizz 13 14 FizzBuzz
 *           all three agree: true
 *  DESIGN : Every version RETURNS a List<String> instead of printing, so
 *           tests can prove all three give the same answer.
 *  EXTEND : Add "Bazz" for multiples of 7 to all three versions.
 * =====================================================================
 */

private const val FIZZ = 3
private const val BUZZ = 5

/** Version 1: classic loop + if / else if chain. */
fun fizzBuzzWithIf(limit: Int): List<String> {
    val result = mutableListOf<String>() // a growable list (level 06)
    for (n in 1..limit) {
        if (n % FIZZ == 0 && n % BUZZ == 0) {
            result.add("FizzBuzz")
        } else if (n % FIZZ == 0) {
            result.add("Fizz")
        } else if (n % BUZZ == 0) {
            result.add("Buzz")
        } else {
            result.add(n.toString())
        }
    }
    return result
}

/** Version 2: loop + `when` without a subject. */
fun fizzBuzzWithWhen(limit: Int): List<String> {
    val result = mutableListOf<String>()
    for (n in 1..limit) {
        val word =
            when {
                n % (FIZZ * BUZZ) == 0 -> "FizzBuzz"
                n % FIZZ == 0 -> "Fizz"
                n % BUZZ == 0 -> "Buzz"
                else -> n.toString()
            }
        result.add(word)
    }
    return result
}

/**
 * Version 3: transform the range directly. `map` runs the lambda for each
 * number and collects the results — no mutable list, no loop to write.
 * Lambdas and map are covered in levels 06 and 07; enjoy the preview.
 */
fun fizzBuzzWithMap(limit: Int): List<String> =
    (1..limit).map { n ->
        val fizz = if (n % FIZZ == 0) "Fizz" else ""
        val buzz = if (n % BUZZ == 0) "Buzz" else ""
        (fizz + buzz).ifEmpty { n.toString() }
    }

fun main() {
    val limit = 15
    val withIf = fizzBuzzWithIf(limit)
    val withWhen = fizzBuzzWithWhen(limit)
    val withMap = fizzBuzzWithMap(limit)

    println("if-chain : ${withIf.joinToString(" ")}")
    println("when     : ${withWhen.joinToString(" ")}")
    println("map      : ${withMap.joinToString(" ")}")
    println("all three agree: ${withIf == withWhen && withWhen == withMap}")
}
