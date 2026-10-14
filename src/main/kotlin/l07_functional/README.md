# Level 07 — Functional programming

> Functions are values: store them, pass them, return them and combine
> them. This level explains the machinery behind `map`, `apply`,
> `buildString` and every Kotlin DSL.

## Lessons

| # | Lesson | You will learn |
|---|--------|----------------|
| 07.1 | [LambdasInDepth](lesson01_lambdas/LambdasInDepth.kt) | full lambda syntax, last expression = result, `it`, `_`, destructuring |
| 07.2 | [FunctionTypes](lesson02_function_types/FunctionTypes.kt) | `(Int) -> Int`, `invoke`, functions in maps, nullable function types, `typealias` |
| 07.3 | [HigherOrderFunctions](lesson03_higher_order/HigherOrderFunctions.kt) | taking and returning functions, `applyTwice`, composition |
| 07.4 | [FunctionReferences](lesson04_references/FunctionReferences.kt) | `::fn`, `String::length`, `::Person`, bound `obj::method` |
| 07.5 | [Closures](lesson05_closures/Closures.kt) | captured variables, counter factories, the captured-`var` trap |
| 07.6 | [ScopeFunctions](lesson06_scope_functions/ScopeFunctions.kt) | `let`, `run`, `with`, `apply`, `also`: this vs it, result vs object |
| 07.7 | [InlineFunctions](lesson07_inline/InlineFunctions.kt) | `inline`, non-local `return`, `return@label`, `noinline`, `crossinline` |
| 07.8 | [LambdasWithReceiver](lesson08_receivers/LambdasWithReceiver.kt) | `T.() -> R`, how `buildString`/`apply` work, a mini DSL |
| 07.9 | [FunctionalPatterns](lesson09_patterns/FunctionalPatterns.kt) | pure functions, pipelines with `fold`, partial application, memoization, `runCatching` |

## Practice projects

| Project | Combines |
|---------|----------|
| [ValidationPipeline](practice/validation/ValidationPipeline.kt) | rules as lambdas, rule factories, combining rules, collecting all errors |
| [EventBus](practice/eventbus/EventBus.kt) | callbacks, returning an "unsubscribe" function, closures, private state |

## Exercises

1. Open [exercises/FunctionalExercises.kt](exercises/FunctionalExercises.kt).
2. Replace each `TODO(...)` with your answer.
3. Check: `./gradlew exercises --tests "l07_*"`. All 10 tests should pass.

## Cheat sheet

```kotlin
val square = { n: Int -> n * n }          // lambda; last expression is the result
val add: (Int, Int) -> Int = { a, b -> a + b }
val maybe: (() -> Unit)? = null; maybe?.invoke()
typealias Rule = (String) -> String?

fun applyTwice(f: (Int) -> Int, x: Int) = f(f(x))      // takes a function
fun multiplier(k: Int): (Int) -> Int = { it * k }      // returns a function

list.map(::isEven); words.map(String::length); names.map(::Person); items.forEach(bus::send)

x?.let { use(it) }        // it, returns lambda result
x.run { compute() }       // this, returns lambda result
with(x) { a(); b() }      // this, returns lambda result
X().apply { a = 1 }       // this, returns the object
x.also { log(it) }        // it, returns the object

inline fun f(block: () -> Unit)        // lambda pasted in, no object
list.forEach { if (bad) return }       // returns from the ENCLOSING function
list.forEach { if (bad) return@forEach } // skips just this item

fun html(block: Builder.() -> Unit)    // receiver lambda: `this` is a Builder
runCatching { s.toInt() }.getOrElse { 0 }
```

## Key ideas

- **A lambda is a value.** Anything you can do with an Int, you can do with a function: store it, pass it, return it.
- **Higher-order functions split the loop from the decision.** The function does the looping; the caller supplies only the part that changes.
- **Closures remember variables, not values.** Capture `val`s to avoid surprises.
- **Choose a scope function with two questions.** Is the object `this` or `it`? Do you want the lambda's result or the object back?
- **`inline` is for small functions that take lambdas.** It also explains why `return` inside `forEach` leaves the whole function.
- **Keep functions pure where you can.** Same input gives the same output with no side effects, so pure functions are easy to test and safe to share.
- **Use `runCatching` only at boundaries.** It catches everything, including real bugs.

**Next:** Level 08 — Generics
