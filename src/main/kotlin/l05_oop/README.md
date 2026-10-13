# Level 05 — Object-oriented programming

> Group data and behavior into classes, hide what must not be touched,
> and model "one of a fixed set" so the compiler checks every case.

## Lessons

| # | Lesson | You will learn |
|---|--------|----------------|
| 05.1 | [ClassesAndObjects](lesson01_classes/ClassesAndObjects.kt) | classes, objects, properties, methods, references, no `new` |
| 05.2 | [Constructors](lesson02_constructors/Constructors.kt) | primary constructor, default values, `init` order, secondary constructors |
| 05.3 | [Properties](lesson03_properties/Properties.kt) | custom getters/setters, `field`, `private set`, computed properties |
| 05.4 | [Visibility](lesson04_visibility/Visibility.kt) | `public`, `private`, `protected`, `internal`, encapsulation |
| 05.5 | [Inheritance](lesson05_inheritance/Inheritance.kt) | `open`, `override`, `super`, final by default, abstract classes |
| 05.6 | [Interfaces](lesson06_interfaces/Interfaces.kt) | default methods, abstract properties, many interfaces, `super<A>` |
| 05.7 | [DataClasses](lesson07_data_classes/DataClasses.kt) | `equals`/`hashCode`/`toString`, `copy`, destructuring, the body-property trap |
| 05.8 | [ObjectsAndCompanions](lesson08_objects/ObjectsAndCompanions.kt) | `object` singletons, `companion object` factories, anonymous objects |
| 05.9 | [EnumsAndSealed](lesson09_enums_sealed/EnumsAndSealed.kt) | enums with data and methods, `entries`, sealed interfaces, exhaustive `when` |
| 05.10 | [NestedValueAndJava](lesson10_nested_value_java/NestedValueAndJava.kt) | nested vs `inner`, `@JvmInline value class`, data class vs Java POJO |

The Java class used by lesson 05.10 is in
[`src/main/java/l05_oop/lesson10_nested_value_java`](../../java/l05_oop/lesson10_nested_value_java/JavaBook.java).

## Practice projects

| Project | Combines |
|---------|----------|
| [BankAccount](practice/bank/BankAccount.kt) | `private set`, inheritance (`SavingsAccount`), `protected`, enums, data classes, `require` for bad arguments, sealed results for refusals |
| [Shapes](practice/shapes/Shapes.kt) | sealed hierarchy, validation in `init`, exhaustive `when`, area/perimeter |
| [ParkingLot](practice/parking/ParkingLot.kt) | enums that compare by order, sealed vehicles and results, a class guarding its private state |

## Exercises

1. Open [exercises/OopExercises.kt](exercises/OopExercises.kt).
2. Replace each `TODO(...)`. For exercises 1, 2, 7 and 8, complete the classes started at the bottom of the file.
3. Check: `./gradlew exercises --tests "l05_*"`. All 10 tests should pass.

## Cheat sheet

```kotlin
class Dog(val name: String, var age: Int = 0) {  // primary constructor + properties
    init { require(name.isNotBlank()) }           // runs on construction
    fun bark() = println("$name: Woof!")          // method
}

var balance = 0
    private set                                   // read: everyone, write: this class

val area: Int get() = width * height              // computed on every read
var target = 21
    set(value) { if (value in 10..30) field = value }   // `field` = stored value

open class Animal { open fun sound() = "..." }    // open = may be extended/overridden
class Cat : Animal() { override fun sound() = "Meow" }
abstract class Shape { abstract fun area(): Double }

interface Flyer { val speed: Int; fun fly() = "at $speed" }   // default method

data class Point(val x: Int, val y: Int)          // equals/hashCode/toString/copy
val moved = p.copy(y = 5);  val (x, y) = p

object Config { const val NAME = "app" }          // singleton
class Ticket private constructor() { companion object { fun issue() = Ticket() } }

enum class Light { RED, GREEN }                   // fixed values
sealed interface State {                          // fixed kinds, different data
    data object Loading : State
    data class Done(val items: List<String>) : State
}
when (state) { State.Loading -> ...; is State.Done -> ... }   // no else needed

@JvmInline value class UserId(val value: Int)     // type-safe wrapper, no runtime cost
inner class Wheel                                 // can see the outer object
```

## Key ideas

- **Encapsulate.** Keep state private and change it only through methods that enforce the rules.
- **Final by default.** Mark a class `open` only when you have designed it to be extended.
- **Prefer interfaces** for "can do" and abstract classes for "is a kind of" with shared state.
- **Data classes are values.** Keep their properties `val` (and their types read-only) and change them with `copy()`. `copy()` is shallow: a `MutableList` inside is shared by both copies.
- **Sealed types + exhaustive `when`, with no `else`.** When you add a new case, the compiler shows every place that must handle it.
- **Two kinds of "no".** An invalid argument is a caller bug, so `require(...)` throws `IllegalArgumentException`. A legitimate business refusal like "insufficient funds" is a normal outcome, so return `false` or a sealed result type.

**Next:** Level 06 — Collections
