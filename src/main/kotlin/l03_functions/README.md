# Level 03 — Functions

> Name your ideas, reuse them everywhere and make them read like plain English.

## Lessons

| # | Lesson | You will learn |
|---|--------|----------------|
| 03.1 | [FunctionBasics](lesson01_basics/FunctionBasics.kt) | declaring and calling, parameters, return types, block vs expression body |
| 03.2 | [DefaultAndNamedArgs](lesson02_default_named/DefaultAndNamedArgs.kt) | default values, named arguments, why Kotlin rarely needs overloads |
| 03.3 | [VarargAndSpread](lesson03_vararg/VarargAndSpread.kt) | `vararg`, the spread operator `*`, vararg position |
| 03.4 | [LocalFunctionsUnitNothing](lesson04_local_unit_nothing/LocalFunctionsUnitNothing.kt) | local functions, `Unit`, `Nothing`, `TODO()` |
| 03.5 | [ExtensionFunctions](lesson05_extensions/ExtensionFunctions.kt) | extension functions and properties, static resolution, safe `removeLastComma` |
| 03.6 | [InfixAndTailrec](lesson06_infix_tailrec/InfixAndTailrec.kt) | `infix`, recursion, `StackOverflowError`, `tailrec` |

## Practice project

| Project | Combines |
|---------|----------|
| [StringUtils](practice/stringutils/StringUtils.kt) | extension functions, default args, `require`, edge cases (empty text) |

## Exercises

1. Read the task descriptions in [FunctionsTasks.kt](FunctionsTasks.kt).
2. Replace each `TODO(...)` in [exercises/FunctionsExercises.kt](exercises/FunctionsExercises.kt).
3. Check: `./gradlew exercises --tests "l03_*"`. All 8 tests should pass.

## Cheat sheet

```kotlin
fun add(a: Int, b: Int): Int { return a + b }   // block body
fun square(n: Int) = n * n                      // expression body, type inferred

fun greet(name: String = "Guest") = "Hi, $name" // default value
greet(name = "Ada")                             // named argument

fun sum(vararg n: Int): Int { ... }             // any number of args
sum(1, 2, 3); sum(*intArrayOf(1, 2))            // spread an array

fun log() { }                                   // returns Unit
fun fail(msg: String): Nothing = throw IllegalStateException(msg)

fun String.shout() = uppercase() + "!"          // extension function
val Int.squared: Int get() = this * this        // extension property

infix fun Int.percentOf(total: Int) = total.toDouble() * this / 100
10 percentOf 250                                // 25.0

tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
```

## Key ideas

- **Small, well-named functions** are easier to read, reuse and test.
- **Defaults + named arguments** replace most overloads and make Boolean flags readable.
- **A block body without a return type returns `Unit`.** Use `=` or declare the type.
- **Extensions add functions, not behaviour overrides.** They're resolved by the *declared* type.
- **Always ask "what if the string is empty?"** before using indexes.
- **Deep recursion overflows the stack.** Use `tailrec` (or a loop) with an accumulator.

**Next:** Level 04 — Null safety
