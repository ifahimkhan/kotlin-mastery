package l05_oop.lesson07_data_classes

/**
 * =====================================================================
 *  LESSON 05.7 — Data classes
 * =====================================================================
 *  GOAL   : Model plain data with one line: get equals, hashCode,
 *           toString, copy and destructuring for free, and update
 *           values without mutating them.
 *  RUN    : ./gradlew -q run -Plesson=l05_oop.lesson07_data_classes.DataClassesKt
 *  OUTPUT :
 *           plain toString: l05_oop.lesson07_data_classes.PlainPoint@<hash, changes every run>
 *           data  toString: DataPoint(x=1, y=2)
 *           plain a == b  : false
 *           data  a == b  : true
 *           data hash codes equal: true
 *           moved   : DataPoint(x=1, y=5)
 *           original: DataPoint(x=1, y=2)
 *           component1() = 1, component2() = 2
 *           destructured: x=1, y=2
 *           unique points in a set: 1
 *           Person(name=Ada, age=36) -> Person(name=Ada, age=37)
 *           Pen == Pen with stock 5 vs 99: true  <-- body property ignored!
 *           shared basket: [apple, pear]  <-- original changed too!
 *           safe basket  : [apple] -> [apple, pear]
 *  NEXT   : lesson08_objects/ObjectsAndCompanions.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: plain class vs data class ────────────────────────
    val plainA = PlainPoint(1, 2)
    val plainB = PlainPoint(1, 2)
    val dataA = DataPoint(1, 2)
    val dataB = DataPoint(1, 2)

    // A plain class prints its class name + a memory hash code.
    println("plain toString: ${plainA.toString().substringBefore("@")}@<hash, changes every run>")
    println("data  toString: $dataA")

    // ── 2. CONCEPT: equality by CONTENT ──────────────────────────────
    // Plain classes compare identity (same object?). Data classes
    // compare every property in the primary constructor.
    println("plain a == b  : ${plainA == plainB}")
    println("data  a == b  : ${dataA == dataB}")
    println("data hash codes equal: ${dataA.hashCode() == dataB.hashCode()}")

    // ── 3. CONCEPT: copy() instead of mutation ───────────────────────
    // All properties are `val`. To "change" a point, make a NEW one that
    // differs only in the named property. The original stays untouched.
    val moved = dataA.copy(y = 5)
    println("moved   : $moved")
    println("original: $dataA")

    // ── 4. CONCEPT: componentN and destructuring ─────────────────────
    println("component1() = ${dataA.component1()}, component2() = ${dataA.component2()}")
    val (x, y) = dataA // calls component1() and component2() for you
    println("destructured: x=$x, y=$y")

    // equals + hashCode make data classes work in sets and map keys
    // (sets: level 06). Two equal points count once.
    println("unique points in a set: ${setOf(dataA, dataB).size}")

    val ada = Person("Ada", 36)
    val older = ada.copy(age = ada.age + 1)
    println("$ada -> $older")

    // ── 5. PITFALL: only the PRIMARY constructor counts ──────────────
    // `stock` is declared in the body, so equals/toString/copy ignore it.
    val pen1 = Product("Pen")
    val pen2 = Product("Pen")
    pen1.stock = 5
    pen2.stock = 99
    println("${pen1.name} == ${pen2.name} with stock ${pen1.stock} vs ${pen2.stock}: ${pen1 == pen2}  <-- body property ignored!")

    // ── 6. PITFALL: copy() is SHALLOW ────────────────────────────────
    // copy() copies the REFERENCE to a MutableList, not the list itself.
    // Both baskets now point at the same list.
    val shared = SharedBasket(mutableListOf("apple"))
    val sharedCopy = shared.copy()
    sharedCopy.items.add("pear")
    println("shared basket: ${shared.items}  <-- original changed too!")

    // Fix: a read-only List + copy() with a NEW list (`+` builds one, level 06).
    val safe = SafeBasket(listOf("apple"))
    val safeCopy = safe.copy(items = safe.items + "pear")
    println("safe basket  : ${safe.items} -> ${safeCopy.items}")

    // Another rule:
    //   data class Empty()
    //   // ❌ Data class must have at least one primary constructor parameter.

    // ── 7. WHY ───────────────────────────────────────────────────────
    // A Java class with the same features needs ~40 lines (constructor,
    // getters, equals, hashCode, toString). See lesson 10 for the side-by-side.
    // Use data classes for values: points, money, DTOs, events, results.

    // ── 8. TRY IT ────────────────────────────────────────────────────
    // a) Move `stock` into the primary constructor. What changes in the output?
    // b) Use copy() to rename `ada` to "Ada L." without changing her age.
    // c) Destructure `older` into (name, age) and print them.
}

class PlainPoint(val x: Int, val y: Int)

data class DataPoint(val x: Int, val y: Int)

data class Person(val name: String, val age: Int)

data class SharedBasket(val items: MutableList<String>)

data class SafeBasket(val items: List<String>)

data class Product(val name: String) {
    var stock: Int = 0
}
