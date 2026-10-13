package l05_oop.lesson01_classes

/**
 * =====================================================================
 *  LESSON 05.1 — Classes and objects
 * =====================================================================
 *  GOAL   : Describe a "thing" once with a class, then create as many
 *           objects (instances) of it as you need, each with its own data.
 *  RUN    : ./gradlew -q run -Plesson=l05_oop.lesson01_classes.ClassesAndObjectsKt
 *  OUTPUT :
 *           Rex says Woof! (age 3)
 *           Bella says Woof! (age 5)
 *           Rex had a birthday: age 4
 *           Two separate objects: rex === bella is false
 *           Same object: sameDog === rex is true
 *           After sameDog.haveBirthday(): rex.age = 5
 *           In dog years Rex is 35
 *  NEXT   : lesson02_constructors/Constructors.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: class = blueprint, object = thing built from it ──
    // `Dog` (bottom of this file) describes WHAT every dog has (name, age)
    // and WHAT every dog can do (bark, haveBirthday). Calling `Dog(...)`
    // builds a new object. Kotlin has no `new` keyword.
    val rex = Dog("Rex", 3)
    val bella = Dog("Bella", 5)
    rex.bark()
    bella.bark()

    // ── 2. CONCEPT: every object has its own state ───────────────────
    // Changing rex does not touch bella.
    rex.haveBirthday()
    println("Rex had a birthday: age ${rex.age}")
    println("Two separate objects: rex === bella is ${rex === bella}")

    // ── 3. CONCEPT: variables hold REFERENCES ────────────────────────
    // `val sameDog = rex` does not copy the dog. Both names point to the
    // SAME object, so a change through one name is visible through the other.
    val sameDog = rex
    println("Same object: sameDog === rex is ${sameDog === rex}")
    sameDog.haveBirthday()
    println("After sameDog.haveBirthday(): rex.age = ${rex.age}")

    // Methods can also compute and return values.
    println("In dog years ${rex.name} is ${rex.ageInDogYears()}")

    // ── 4. WHY classes? ──────────────────────────────────────────────
    // Without classes, a dog would be two loose variables (name, age)
    // and functions that must be handed both every time. A class keeps
    // data and the code that works on it together, in one named place.

    // ── 5. PITFALL: Java habits ──────────────────────────────────────
    //   val max = new Dog("Max", 1)  // ❌ Unresolved reference 'new'.
    //   val max = Dog("Max", 1)      // ✅

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Add a `fun rename(newName: String)`. What must change in the class?
    // b) Create a third dog and make it bark twice.
    // c) Write a class `Book(val title: String, val pages: Int)` with a
    //    method `isLong()` that returns true above 300 pages.
}

private const val DOG_YEARS_PER_HUMAN_YEAR = 7

// `val name` and `var age` in the parentheses declare PROPERTIES:
// data stored inside every Dog object. `val` = read-only, `var` = changeable.
class Dog(val name: String, var age: Int) {
    // Functions inside a class are called METHODS. They can use the
    // object's properties directly.
    fun bark() {
        println("$name says Woof! (age $age)")
    }

    fun haveBirthday() {
        age += 1
    }

    fun ageInDogYears(): Int = age * DOG_YEARS_PER_HUMAN_YEAR
}
