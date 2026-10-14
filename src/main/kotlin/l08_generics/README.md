# Level 08 — Generics and the type system

> Write code once and use it for every type, safely. Learn what `<T>`,
> `out`, `in`, `*`, `reified` and `Nothing` really mean, and why
> `List<String>` is a `List<Any>` but `MutableList<String>` is not.

## Lessons

| # | Lesson | You will learn |
|---|--------|----------------|
| 08.1 | [WhyGenerics](lesson01_why/WhyGenerics.kt) | casts that crash vs a generic `Box<T>`, type parameters |
| 08.2 | [GenericFunctions](lesson02_functions/GenericFunctions.kt) | `fun <T>`, inference, several type parameters, generic extensions |
| 08.3 | [Constraints](lesson03_constraints/Constraints.kt) | `T : Comparable<T>`, class bounds, `where`, `T : Any` |
| 08.4 | [VarianceOut](lesson04_variance_out/VarianceOut.kt) | covariance, `out` producers, why `MutableList` is invariant |
| 08.5 | [VarianceIn](lesson05_variance_in/VarianceIn.kt) | contravariance, `in` consumers, `Comparable<in T>`, why Comparator needs a use-site `in`, use-site projections |
| 08.6 | [StarAndErasure](lesson06_star_erasure/StarAndErasure.kt) | `List<*>`, type erasure, why unchecked casts lie |
| 08.7 | [Reified](lesson07_reified/Reified.kt) | `inline fun <reified T>`, `is T`, `as? T`, `T::class` |
| 08.8 | [TypeSystem](lesson08_type_system/TypeSystem.kt) | `Any`/`Any?`/`Unit`/`Nothing`, `Nothing` in generic types, `typealias`, `T & Any` |

## Practice projects

| Project | Combines |
|---------|----------|
| [ResultType](practice/result/ResultType.kt) | your own `Outcome<out T, out E>` with `Nothing`, `map`/`flatMap`/`fold`, validation chain |
| [GenericRepository](practice/repository/GenericRepository.kt) | `Repository<T : Entity<ID>, ID>`, copy-on-write, generic extension functions |
| [TypeSafeCache](practice/cache/TypeSafeCache.kt) | typed keys, one hidden unchecked cast, LRU eviction with `LinkedHashMap` |

## Exercises

1. Open [exercises/GenericsExercises.kt](exercises/GenericsExercises.kt).
2. Replace each `TODO(...)` with your answer. For Ex 5, finish the `MyStack` class at the bottom.
3. Check: `./gradlew exercises --tests "l08_*"`. All 11 tests should pass.

## Cheat sheet

```kotlin
class Box<T>(var value: T)                   // generic class
fun <T> firstOrNull(items: List<T>): T?      // generic function
fun <A, B> swap(p: Pair<A, B>): Pair<B, A>   // several type parameters
fun <T> List<T>.secondOrNull(): T?           // generic extension

fun <T : Comparable<T>> max(a: T, b: T): T   // upper bound
fun <T> f(x: T) where T : Named, T : Comparable<T>  // several bounds
fun <T : Any> key(x: T)                      // no nulls allowed

interface Source<out T> { fun next(): T }    // producer: Source<Dog> is a Source<Animal>
interface Sink<in T> { fun accept(x: T) }    // consumer: Sink<Animal> is a Sink<Dog>
fun copy(from: Array<out Any>, to: Array<Any>)  // use-site projection
fun describe(list: List<*>)                  // "a list of something"

inline fun <reified T> List<Any?>.only(): List<T> = filterIsInstance<T>()

sealed interface Outcome<out T, out E> {        // own name: don't shadow kotlin.Result
    data class Success<out T>(val value: T) : Outcome<T, Nothing>
    data class Failure<out E>(val error: E) : Outcome<Nothing, E>  // fits every Outcome<T, E>
}
typealias Scores = Map<String, List<Int>>
fun <T> orDefault(x: T, d: T & Any): T & Any  // definitely non-null
```

## Key ideas

- **Generics move mistakes from runtime to compile time.** No casts means no `ClassCastException`.
- **A bound is a two-way promise.** Callers pass types that fit, and the function may use what the bound offers.
- **"Producer out, consumer in".** A type that only returns `T` can be `out`, and one that only takes `T` can be `in`. One that does both stays invariant.
- **An instance's type arguments are erased at runtime.** A `List<String>` object is just a `List` to the JVM, so `is List<String>` can't be checked and unchecked casts can lie. `reified` (in inline functions) restores only the top-level class: `is T` with `T = List<String>` still checks just `List`.
- **`Nothing` is the subtype of everything.** That is why `emptyList<Nothing>()` fits any `List<T>` and a `Failure` fits every `Outcome<T, E>`.
- **`typealias` is only a new name.** For a truly new type, use a value class (level 05).

**Next:** Level 09 — Coroutines
