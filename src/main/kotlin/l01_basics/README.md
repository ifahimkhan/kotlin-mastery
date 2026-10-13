# Level 01 — Basics

> Your first Kotlin programs: variables, types, strings, operators and input.

## Lessons

| # | Lesson | You will learn |
|---|--------|----------------|
| 01.1 | [HelloWorld](lesson01_hello/HelloWorld.kt) | `main`, `println`, `print`, comments, program arguments |
| 01.2 | [Variables](lesson02_variables/Variables.kt) | `val` vs `var`, type inference, `const val` |
| 01.3 | [BasicTypes](lesson03_types/BasicTypes.kt) | Int/Long/Double/Char/Boolean, overflow, conversions |
| 01.4 | [Strings](lesson04_strings/Strings.kt) | templates, raw strings, common functions, immutability |
| 01.5 | [Operators](lesson05_operators/Operators.kt) | arithmetic, `%`, `++`, short-circuit, `==` vs `===` |
| 01.6 | [UserInput](lesson06_input/UserInput.kt) | `readln()`, `toIntOrNull()`, validating input |

## Practice projects

| Project | Combines |
|---------|----------|
| [TemperatureConverter](practice/temperature/TemperatureConverter.kt) | input, Double math, constants, validation |
| [BmiCalculator](practice/bmi/BmiCalculator.kt) | two inputs, `require`, classification |

## Exercises

1. Open [exercises/BasicsExercises.kt](exercises/BasicsExercises.kt).
2. Replace each `TODO(...)` with your answer.
3. Check: `./gradlew exercises --tests "l01_*"`. All 8 tests should pass.

## Cheat sheet

```kotlin
val fixed = 10            // read-only
var counter = 0           // reassignable
const val MAX = 3         // compile-time constant (top level)

val big = 3_000_000_000L  // Long literal
val half = 7 / 2.0        // 3.5  (7 / 2 == 3!)
val n = "42".toInt()      // crashes on bad text
val m = "x".toIntOrNull() // null instead of crash

"Hi $name, next year ${age + 1}"  // templates
"""raw "string" """.trimIndent()  // raw string

a == b    // same content
a === b   // same object
```

## Key ideas

- **Prefer `val`.** Use `var` only when a value really changes.
- **Kotlin never converts number types silently.** Call `toLong()`, `toDouble()` and so on yourself.
- **Watch for overflow and Int division.** They are the two most common math bugs.
- **Strings are immutable.** Their functions return new strings.
- **Validate input where it enters the program.**

**Next:** Level 02 — Control flow
