package l05_oop.lesson05_inheritance

import kotlin.math.PI
import kotlin.math.roundToLong

/**
 * =====================================================================
 *  LESSON 05.5 — Inheritance and abstract classes
 * =====================================================================
 *  GOAL   : Build specialized classes on top of a general one with
 *           `open`, `override` and `super`; use abstract classes for
 *           "every subclass MUST provide this".
 *  RUN    : ./gradlew -q run -Plesson=l05_oop.lesson05_inheritance.InheritanceKt
 *  OUTPUT :
 *           Generic: ...some generic animal sound...
 *           Rex: Woof!
 *           Tom: Meow! (then the parent adds: ...some generic animal sound...)
 *           --- one loop, every animal behaves its own way ---
 *           Generic: ...some generic animal sound...
 *           Rex: Woof!
 *           Tom: Meow! (then the parent adds: ...some generic animal sound...)
 *           Circle: area 12.57
 *           Square: area 4.0
 *           Base init sees: label=null  <-- not initialized yet!
 *  NEXT   : lesson06_interfaces/Interfaces.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: open + override ──────────────────────────────────
    // Dog and Cat INHERIT name and describe() from Animal, and
    // OVERRIDE sound() with their own version.
    val generic = Animal("Generic")
    val rex = Dog("Rex")
    val tom = Cat("Tom")
    println(generic.describe())
    println(rex.describe())
    println(tom.describe()) // Cat calls super.sound() to reuse the parent

    // ── 2. CONCEPT: polymorphism ─────────────────────────────────────
    // A Dog IS an Animal, so it fits in a list of Animals. Each object
    // still uses ITS OWN sound(). (Lists are level 06.)
    println("--- one loop, every animal behaves its own way ---")
    val animals: List<Animal> = listOf(generic, rex, tom)
    for (animal in animals) {
        println(animal.describe())
    }

    // ── 3. CONCEPT: abstract classes ─────────────────────────────────
    // Shape can't know how to compute an area, so area() is abstract:
    // no body, and every subclass is FORCED to implement it.
    val shapes: List<Shape> = listOf(Circle(radius = 2.0), Square(side = 2.0))
    for (shape in shapes) {
        println(shape.report())
    }

    // ── 4. WHY final by default? ─────────────────────────────────────
    // In Java every class can be extended unless marked final, and
    // subclasses often break when the parent changes. Kotlin flips it:
    // classes and methods are FINAL unless you say `open`. Extension is
    // something the author of a class must plan for.
    // Note: an `override` is itself open again. Dog's sound() could be
    // overridden by a subclass of Dog (if Dog were open). Write
    // `final override fun sound()` to stop that.

    // ── 5. PITFALL: these lines don't compile ────────────────────────
    //   class Puppy : Dog("Pup")
    //   // ❌ This type is final, so it cannot be extended.
    //   (in Dog) override fun describe() = "..."
    //   // ❌ 'describe' in 'Animal' is final and cannot be overridden.
    //   val shape = Shape("?")
    //   // ❌ Cannot create an instance of an abstract class.

    // ── 6. PITFALL: calling an open function from a constructor ──────
    // Init order (lesson 05.2): the PARENT is built first. If the parent's
    // init calls an open function, the CHILD's override runs before the
    // child's own properties are set. `label` is a non-null String, yet
    // it is still null here. Never call open functions from init blocks.
    Derived()

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Add a Cow that says "Moo!" and put it in the animals list.
    // b) Add a Rectangle(width, height) shape.
    // c) Make Dog `open` and create a Puppy that says "Yip!".
}

private const val HUNDREDTHS = 100.0

private fun roundTo2Decimals(value: Double): Double = (value * HUNDREDTHS).roundToLong() / HUNDREDTHS

// `open`: other classes may inherit from Animal.
open class Animal(val name: String) {
    // `open`: subclasses may replace this method.
    open fun sound(): String = "...some generic animal sound..."

    // Not open: every animal describes itself the same way.
    fun describe(): String = "$name: ${sound()}"
}

// `: Animal(name)` = "inherits from Animal, passing name to its constructor".
class Dog(name: String) : Animal(name) {
    override fun sound(): String = "Woof!"
}

class Cat(name: String) : Animal(name) {
    // `super.sound()` calls the PARENT's version.
    override fun sound(): String = "Meow! (then the parent adds: ${super.sound()})"
}

open class Base {
    init {
        println("Base init sees: ${describeSelf()}  <-- not initialized yet!")
    }

    open fun describeSelf(): String = "base"
}

class Derived : Base() {
    private val label: String = "derived"

    override fun describeSelf(): String = "label=$label"
}

abstract class Shape(val name: String) {
    abstract fun area(): Double

    fun report(): String = "$name: area ${roundTo2Decimals(area())}"
}

class Circle(val radius: Double) : Shape("Circle") {
    override fun area(): Double = PI * radius * radius
}

class Square(val side: Double) : Shape("Square") {
    override fun area(): Double = side * side
}
