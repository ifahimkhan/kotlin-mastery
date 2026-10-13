package l05_oop.lesson03_properties

/**
 * =====================================================================
 *  LESSON 05.3 — Properties: getters, setters and backing fields
 * =====================================================================
 *  GOAL   : Customize what happens when a property is read or written:
 *           validation, normalization, read-only from outside, and
 *           values computed on every read.
 *  RUN    : ./gradlew -q run -Plesson=l05_oop.lesson03_properties.PropertiesKt
 *  OUTPUT :
 *           Thermostat target: 21
 *           Set to 35 -> still 21 (outside 10..30, ignored)
 *           Set to 25 -> now 25
 *           Accepted changes so far: 1
 *           Rectangle 3 x 4: area = 12
 *           After width = 5: area = 20
 *           Full name: Ada Lovelace, initials: A.L.
 *           username stored as: "ada_l"
 *  NEXT   : lesson04_visibility/Visibility.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: a custom setter with validation ──────────────────
    // `thermostat.target = 35` looks like a plain assignment, but it
    // actually CALLS the setter written in the class below.
    val thermostat = Thermostat()
    println("Thermostat target: ${thermostat.target}")
    thermostat.target = 35
    println("Set to 35 -> still ${thermostat.target} (outside $MIN_TARGET..$MAX_TARGET, ignored)")
    thermostat.target = 25
    println("Set to 25 -> now ${thermostat.target}")

    // ── 2. CONCEPT: private set ──────────────────────────────────────
    // Everyone may READ `changes`, only the class may WRITE it.
    println("Accepted changes so far: ${thermostat.changes}")
    //   thermostat.changes = 0
    //   // ❌ Cannot access 'changes': it is private in 'l05_oop.lesson03_properties.Thermostat'.

    // ── 3. CONCEPT: computed properties (no stored value) ────────────
    // `area` has only a getter, so it is recalculated on EVERY read and
    // can never be out of date.
    val rectangle = Rectangle(3, 4)
    println("Rectangle ${rectangle.width} x ${rectangle.height}: area = ${rectangle.area}")
    rectangle.width = 5
    println("After width = 5: area = ${rectangle.area}")

    val person = Person("Ada", "Lovelace")
    println("Full name: ${person.fullName}, initials: ${person.initials}")

    // ── 4. CONCEPT: normalizing in a setter ──────────────────────────
    val account = UserAccount()
    account.username = "  Ada_L "
    println("username stored as: \"${account.username}\"")

    // ── 5. WHY not public fields like in Java? ───────────────────────
    // In Kotlin, every property ALREADY has a getter and setter (generated
    // for you). You can add logic to them later without changing a single
    // caller: `x.target = 25` keeps working. No getTarget()/setTarget() noise.

    // ── 6. PITFALL: assigning the property inside its own setter ─────
    //   set(value) { target = value }  // calls the setter again... forever:
    //                                  // StackOverflowError at runtime!
    //   set(value) { field = value }   // ✅ `field` = the stored value itself

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Make the thermostat throw (require) instead of ignoring 35.
    //    Which behavior would you prefer as a caller? Why?
    // b) Add a computed property `isSquare` to Rectangle.
    // c) Make `username` reject names shorter than 3 characters.
}

const val MIN_TARGET = 10
const val MAX_TARGET = 30
private const val DEFAULT_TARGET = 21

class Thermostat {
    var target: Int = DEFAULT_TARGET
        set(value) {
            if (value in MIN_TARGET..MAX_TARGET) {
                field = value // `field` is the hidden storage ("backing field")
                changes += 1
            }
        }

    var changes: Int = 0
        private set
}

class Rectangle(var width: Int, var height: Int) {
    val area: Int
        get() = width * height
}

class Person(val firstName: String, val lastName: String) {
    val fullName: String
        get() = "$firstName $lastName"

    val initials: String
        get() = "${firstName.first()}.${lastName.first()}."
}

class UserAccount {
    var username: String = ""
        set(value) {
            field = value.trim().lowercase()
        }
}
