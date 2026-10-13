# Level 02 — Control flow

> Make decisions and repeat work: `if`, `when`, ranges, loops, `break`/`continue` and labels.

## Lessons

| # | Lesson | You will learn |
|---|--------|----------------|
| 02.1 | [IfExpression](lesson01_if/IfExpression.kt) | `if`/`else if`/`else`, `if` as an expression, blocks as values |
| 02.2 | [When](lesson02_when/When.kt) | `when` with and without a subject, several values, ranges, `is`, exhaustiveness |
| 02.3 | [Ranges](lesson03_ranges/Ranges.kt) | `..`, `..<`, `until`, `downTo`, `step`, `in`/`!in`, char ranges, empty ranges |
| 02.4 | [Loops](lesson04_loops/Loops.kt) | `for` over ranges/strings/`indices`/`withIndex`, `while`, `do-while`, `repeat` |
| 02.5 | [BreakContinueLabels](lesson05_break_continue/BreakContinueLabels.kt) | `break`, `continue`, labeled loops `break@outer` |

## Practice projects

| Project | Combines |
|---------|----------|
| [FizzBuzz](practice/fizzbuzz/FizzBuzz.kt) | the same problem solved three ways: if-chain, `when`, `map` |
| [NumberGuessingGame](practice/guessing/NumberGuessingGame.kt) | `do-while`, input validation, `Random`, a pure testable function |
| [GradeCalculator](practice/grades/GradeCalculator.kt) | validation with `require`, loops, `when` with conditions |

## Exercises

1. Open [exercises/ControlFlowExercises.kt](exercises/ControlFlowExercises.kt).
2. Replace each `TODO(...)` with your answer.
3. Check: `./gradlew exercises --tests "l02_*"`. All 8 tests should pass.

## Cheat sheet

```kotlin
val max = if (a > b) a else b          // if is an expression

val name = when (day) {                // when with a subject
    1 -> "Mon"
    6, 7 -> "Weekend"                  // several values
    in 2..5 -> "Weekday"               // range
    else -> "?"
}

when {                                 // when without a subject
    n % 15 == 0 -> "FizzBuzz"          // most specific first!
    n % 3 == 0 -> "Fizz"
    else -> n.toString()
}

for (i in 1..5) {}                     // 1 2 3 4 5
for (i in 0..<5) {}                    // 0 1 2 3 4
for (i in 5 downTo 1 step 2) {}        // 5 3 1
for ((i, c) in "abc".withIndex()) {}   // index + value
while (cond) {}                        // check first
do {} while (cond)                     // body runs at least once
repeat(3) {}                           // three times

outer@ for (r in rows) {
    for (c in cols) if (done) break@outer
}
```

## Key ideas

- **`if` and `when` return values.** Prefer `val x = if/when ...` over assigning a `var` in each branch.
- **The first matching `when` branch wins.** Put the most specific condition first.
- **`5..1` is empty, not a countdown.** Use `5 downTo 1`.
- **Pick the loop that matches the job.** Use `for` when you know the items, `while` until something happens, and `do-while` for "at least once".
- **A plain `break` leaves only the innermost loop.** Use labels, or better, a function with `return`.

**Next:** Level 03 — Functions
