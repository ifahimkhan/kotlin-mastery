# Level 11 — Advanced Kotlin

> The tools library authors use every day: exceptions and `Result`,
> delegation with `by`, operator overloading, type-safe builders (DSLs),
> annotations, state machines, Java interop and contracts.
>
> Earlier lessons said "(try/catch: later level)". **This is that level.**
> Lesson 11.1 covers exceptions properly.

## Lessons

| # | Lesson | You will learn |
|---|--------|----------------|
| 11.1 | [Exceptions](lesson01_exceptions/Exceptions.kt) | `try` as an expression, `finally`, `throw` is `Nothing`, your own exception hierarchy, `use {}`, no checked exceptions |
| 11.2 | [ResultBasics](lesson02_result/ResultBasics.kt) | `runCatching`, `map`/`mapCatching`/`recover`/`fold`/`getOrElse`, Result vs a sealed error type, the coroutine pitfall |
| 11.3 | [ClassDelegation](lesson03_class_delegation/ClassDelegation.kt) | `: Interface by inner`, decorators without boilerplate, why `toString` is not delegated |
| 11.4 | [PropertyDelegation](lesson04_property_delegation/PropertyDelegation.kt) | `lazy`, `observable`, `vetoable`, map-backed properties, your own `ReadWriteProperty`, `by this::other`, `provideDelegate` |
| 11.5 | [OperatorOverloading](lesson05_operator_overloading/OperatorOverloading.kt) | `+ - * unary-`, `compareTo`, `get`/`set`, `in`, `..`, `invoke`, `iterator`, the `+=` ambiguity |
| 11.6 | [DslBuilders](lesson06_dsl_builders/DslBuilders.kt) | nested builders, immutable results, `@DslMarker` against scope leakage |
| 11.7 | [AnnotationsAndReflection](lesson07_annotations_reflection/AnnotationsAndReflection.kt) | `@Target`, `@Retention`, reading annotations at runtime, `::class`, property and function references, what needs kotlin-reflect |
| 11.8 | [StateMachines](lesson08_state_machines/StateMachines.kt) | sealed states and events, one pure `next()`, rejected moves as values, history with `fold` |
| 11.9 | [JavaInterop](lesson09_java_interop/JavaInterop.kt) | `@file:JvmName`, `@JvmName`, `@JvmOverloads`, `@JvmStatic`, `@JvmField`, `@Throws`, nullability at the boundary |
| 11.10 | [ContractsAndMore](lesson10_contracts_and_more/ContractsAndMore.kt) | `contract { returns(true) implies ... }`, `callsInPlace`, `typealias`, context parameters |

## Practice projects

| Project | Combines |
|---------|----------|
| [HtmlDsl](practice/html/HtmlDsl.kt) | nested builders, `@DslMarker`, `unaryPlus`, escaping, an immutable tree, recursive rendering |
| [ConfigDsl](practice/config/ConfigDsl.kt) | builders with defaults, validation with `require` at `build()`, data-class results |
| [OrderStateMachine](practice/orders/OrderStateMachine.kt) | sealed states and events, a pure reducer, rejected moves as values, an audit trail |

## Exercises

1. Open [exercises/AdvancedExercises.kt](exercises/AdvancedExercises.kt) and read the tasks in [AdvancedTasks.kt](AdvancedTasks.kt).
2. Replace each `TODO(...)` with your answer. For Ex 4–7, finish the small classes at the bottom of the file.
3. Check: `./gradlew exercises --tests "l11_*"`. All 10 tests should pass.

## Cheat sheet

```kotlin
val n = try { s.toInt() } catch (e: NumberFormatException) { 0 }   // try is an expression
Resource().use { it.read() }                                        // closes even on errors

runCatching { s.toInt() }                 // Result<Int>
    .mapCatching { require(it > 0); it }  // map would let require's exception escape
    .getOrElse { 0 }

class Loud(inner: Greeter) : Greeter by inner { override fun greet(n: String) = ... }

val config by lazy { load() }                                       // once, thread-safe
var volume by Delegates.vetoable(0) { _, _, new -> new in 0..10 }   // reject a change
val name: String by map                                             // read from a map

operator fun plus(other: Money) = Money(cents + other.cents)        // a + b
operator fun get(row: Int, col: Int): Int                           // grid[1, 2]
operator fun invoke(x: Int): Int                                    // thing(5)

@DslMarker annotation class MenuDsl
fun menu(block: MenuBuilder.() -> Unit) = MenuBuilder().apply(block).build()

@Target(AnnotationTarget.FIELD) @Retention(AnnotationRetention.RUNTIME) annotation class Required
field.isAnnotationPresent(Required::class.java)

@JvmStatic  @JvmField  @JvmOverloads  @Throws(IOException::class)  @file:JvmName("Utils")

contract { returns(true) implies (value != null) }   // writing one needs @OptIn(ExperimentalContracts::class)
context(log: AuditLog) fun save(id: String) = log.record(id)
context(AuditLog()) { save("x") }                    // provide the context argument
```

## Key ideas

- **Exceptions are for the unexpected.** Expected outcomes (not enough money, illegal state change) are clearer as result values. Use `require` for caller bugs.
- **The compiler trusts contracts, it doesn't check them.** A wrong contract gives wrong smart casts.
- **Context parameters compile with Kotlin 2.4.20 without extra flags.** They replaced the old "context receivers" experiment.
- **`runCatching` catches everything,** including `CancellationException`. In coroutines, catch specific exceptions or rethrow cancellation.
- **Prefer delegation to inheritance.** `by` works with any interface and any object, but it forwards only the interface's members (not `toString`/`equals`/`hashCode`).
- **An operator should mean the obvious thing.** `money + money` reads well, `user + user` does not.
- **A DSL is plain Kotlin.** Lambdas with receiver plus builders. `@DslMarker` keeps inner blocks from calling outer builders by accident.
- **Pure state machines are easy to test.** One function `(state, event) -> result`, exhaustive `when`, no `else`.
- **Java needs a little help.** The `@Jvm*` annotations and `@Throws` make a Kotlin API feel native from Java.

**Next:** Level 12 — Capstone projects
