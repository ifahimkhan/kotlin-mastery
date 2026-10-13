package l05_oop.lesson06_interfaces

/**
 * =====================================================================
 *  LESSON 05.6 — Interfaces
 * =====================================================================
 *  GOAL   : Describe what something CAN DO with an interface, give
 *           default implementations, implement several interfaces,
 *           and resolve clashes with super<Name>.
 *  RUN    : ./gradlew -q run -Plesson=l05_oop.lesson06_interfaces.InterfacesKt
 *  OUTPUT :
 *           Duck: I can fly at 10 km/h
 *           Duck: I can swim
 *           Duck says: Quack! (Flyer: Whoosh, Swimmer: Splash)
 *           Plane: I can fly at 900 km/h
 *           Plane says: Whoosh
 *           Things that fly:
 *           - Duck at 10 km/h
 *           - Plane at 900 km/h
 *  NEXT   : lesson07_data_classes/DataClasses.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: an interface is a promise ────────────────────────
    // "Anything that is a Flyer has a name and a speed, and can fly()."
    // fly() has a DEFAULT body, so Duck and Plane get it for free.
    val duck = Duck()
    val plane = Plane()
    println("${duck.name}: ${duck.fly()}")

    // ── 2. CONCEPT: one class, many interfaces ───────────────────────
    // A class can extend only ONE class, but implement MANY interfaces.
    println("${duck.name}: ${duck.swim()}")

    // ── 3. CONCEPT: resolving a clash ────────────────────────────────
    // Flyer and Swimmer BOTH have a default sound(). Duck must decide,
    // and can call each parent version with super<Flyer> / super<Swimmer>.
    println("${duck.name} says: ${duck.sound()}")

    println("${plane.name}: ${plane.fly()}")
    println("${plane.name} says: ${plane.sound()}") // default from Flyer

    // ── 4. CONCEPT: program against the interface ────────────────────
    // This loop doesn't care WHAT the things are, only that they fly.
    println("Things that fly:")
    val flyers: List<Flyer> = listOf(duck, plane) // lists: level 06
    for (flyer in flyers) {
        println("- ${flyer.name} at ${flyer.speedKmh} km/h")
    }

    // ── 5. WHY interface vs abstract class? ──────────────────────────
    //   interface      : no constructor, no stored state, many per class
    //                    -> "can do" (Flyer, Swimmer, Comparable)
    //   abstract class : constructor + stored state, only ONE per class
    //                    -> "is a kind of" (Shape, Animal)
    // Prefer interfaces; they keep classes loosely connected.

    // ── 6. PITFALL: an unresolved clash ──────────────────────────────
    // Delete Duck's sound() override and you get:
    //   ❌ Class 'Duck' must override 'sound' because it inherits multiple interface methods for it.

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Create a Penguin that is a Swimmer but not a Flyer.
    // b) Add `fun land(): String = "Landing"` to Flyer. Who must change?
    // c) Add a Fish and loop over a List<Swimmer>.
}

interface Flyer {
    val name: String // abstract property: implementers must provide it
    val speedKmh: Int

    fun fly(): String = "I can fly at $speedKmh km/h"

    fun sound(): String = "Whoosh"
}

interface Swimmer {
    fun swim(): String = "I can swim"

    fun sound(): String = "Splash"
}

class Duck : Flyer, Swimmer {
    override val name: String = "Duck"
    override val speedKmh: Int = 10

    override fun sound(): String =
        "Quack! (Flyer: ${super<Flyer>.sound()}, Swimmer: ${super<Swimmer>.sound()})"
}

class Plane : Flyer {
    override val name: String = "Plane"
    override val speedKmh: Int = 900
}
