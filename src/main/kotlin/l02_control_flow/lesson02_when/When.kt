package l02_control_flow.lesson02_when

/**
 * =====================================================================
 *  LESSON 02.2 — when: Kotlin's super switch
 * =====================================================================
 *  GOAL   : Replace long if-else chains with `when`: match values,
 *           several values, ranges and types, with or without a subject.
 *  RUN    : ./gradlew -q run -Plesson=l02_control_flow.lesson02_when.WhenKt
 *  OUTPUT :
 *           day 1 is Monday
 *           day 6 is Saturday
 *           day 9 is unknown
 *           6 -> weekend, 3 -> weekday
 *           score 95 -> A, 82 -> B, 40 -> F
 *           42 is an Int: next is 43
 *           "kotlin" is a String of length 6
 *           3.5 is a Double
 *           true is something else
 *           15 -> FizzBuzz, 9 -> Fizz, 7 -> 7
 *           traffic light RED means stop
 *  NEXT   : lesson03_ranges/Ranges.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: when with a subject ──────────────────────────────
    // Compares the subject with each branch, top to bottom. The first
    // match runs. `else` catches everything else.
    println("day 1 is ${dayName(1)}")
    println("day 6 is ${dayName(6)}")
    println("day 9 is ${dayName(9)}")

    // ── 2. CONCEPT: several values in one branch ─────────────────────
    println("6 -> ${dayType(6)}, 3 -> ${dayType(3)}")

    // ── 3. CONCEPT: ranges in branches ───────────────────────────────
    // `in 90..100` means "between 90 and 100, both included".
    println("score 95 -> ${letterGrade(95)}, 82 -> ${letterGrade(82)}, 40 -> ${letterGrade(40)}")

    // ── 4. CONCEPT: matching types with `is` ─────────────────────────
    // After `is Int ->` Kotlin KNOWS value is an Int inside that branch
    // (a "smart cast"), so you can call Int functions on it.
    println(describe(42))
    println(describe("kotlin"))
    println(describe(3.5))
    println(describe(true))

    // ── 5. CONCEPT: when without a subject ───────────────────────────
    // Each branch is any Boolean condition: a cleaner if-else-if chain.
    println("15 -> ${fizzBuzz(15)}, 9 -> ${fizzBuzz(9)}, 7 -> ${fizzBuzz(7)}")

    // ── 6. CONCEPT: exhaustive when (preview) ────────────────────────
    // With an enum (level 05) the compiler knows ALL possible values.
    // If every value is covered, no `else` is needed — and if someone
    // adds a new value later, the compiler points at every `when` to fix.
    val light = TrafficLight.RED
    val action =
        when (light) {
            TrafficLight.RED -> "stop"
            TrafficLight.YELLOW -> "slow down"
            TrafficLight.GREEN -> "go"
        }
    println("traffic light $light means $action")

    // ── 7. PITFALL: order matters ────────────────────────────────────
    // The first matching branch wins. This is WRONG:
    //   when {
    //       n % 3 == 0 -> "Fizz"          // 15 stops here...
    //       n % 15 == 0 -> "FizzBuzz"     // ...so this never runs for 15
    //   }
    // Put the most specific condition first.

    // ── 8. TRY IT ────────────────────────────────────────────────────
    // a) Write when(month) returning the number of days (ignore leap years).
    // b) Add `is List<*> ->` to describe() and print the list size.
    // c) Rewrite describeTemperature from lesson 02.1 with `when`.
}

private fun dayName(day: Int): String =
    when (day) {
        1 -> "Monday"
        2 -> "Tuesday"
        3 -> "Wednesday"
        4 -> "Thursday"
        5 -> "Friday"
        6 -> "Saturday"
        7 -> "Sunday"
        else -> "unknown"
    }

private fun dayType(day: Int): String =
    when (day) {
        6, 7 -> "weekend"
        in 1..5 -> "weekday"
        else -> "invalid"
    }

private fun letterGrade(score: Int): String =
    when (score) {
        in 90..100 -> "A"
        in 80..89 -> "B"
        in 70..79 -> "C"
        in 60..69 -> "D"
        else -> "F"
    }

// `Any` = "a value of any type" (level 05).
private fun describe(value: Any): String =
    when (value) {
        is Int -> "$value is an Int: next is ${value + 1}"
        is String -> "\"$value\" is a String of length ${value.length}"
        is Double -> "$value is a Double"
        else -> "$value is something else"
    }

private fun fizzBuzz(n: Int): String =
    when {
        n % 15 == 0 -> "FizzBuzz"
        n % 3 == 0 -> "Fizz"
        n % 5 == 0 -> "Buzz"
        else -> n.toString()
    }

// enum classes are explained in level 05.
private enum class TrafficLight { RED, YELLOW, GREEN }
