package l05_oop.lesson02_constructors

/**
 * =====================================================================
 *  LESSON 05.2 — Constructors and init blocks
 * =====================================================================
 *  GOAL   : Control how objects are built: primary constructors,
 *           default values, init blocks (and their order), and
 *           secondary constructors.
 *  RUN    : ./gradlew -q run -Plesson=l05_oop.lesson02_constructors.ConstructorsKt
 *  OUTPUT :
 *           1. property initializer: name = Ada
 *           2. init block #1: Ada
 *           3. property initializer: greeting = Hi, Ada
 *           4. init block #2: greeting ready
 *           Created: Ada (role=Engineer)
 *           ---
 *           1. property initializer: name = Grace
 *           2. init block #1: Grace
 *           3. property initializer: greeting = Hi, Grace
 *           4. init block #2: greeting ready
 *           Created: Grace (role=Admiral)
 *           ---
 *           Pizza(size=30 cm, cheese=true)
 *           Pizza(size=40 cm, cheese=false)
 *           From a label: Pizza(size=40 cm, cheese=true)
 *  NEXT   : lesson03_properties/Properties.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: the primary constructor ──────────────────────────
    // The parentheses after the class name ARE the primary constructor.
    // `role` has a default value, so it may be left out.
    val ada = Member("Ada")
    println("Created: ${ada.name} (role=${ada.role})")
    println("---")

    // Named arguments (level 03) work with constructors too.
    val grace = Member("Grace", role = "Admiral")
    println("Created: ${grace.name} (role=${grace.role})")
    println("---")

    // ── 2. CONCEPT: initialization order ─────────────────────────────
    // Look at the numbered lines above: property initializers and init
    // blocks run TOP TO BOTTOM, in the order they appear in the class.
    // That's why `greeting` can use `name`: name was set one step earlier.

    // ── 3. CONCEPT: default values instead of many constructors ──────
    println(Pizza().describe())
    println(Pizza(size = 40, cheese = false).describe())

    // ── 4. CONCEPT: secondary constructors ───────────────────────────
    // An extra `constructor(...)` that must call the primary one with
    // `: this(...)`. Useful when callers have a DIFFERENT KIND of input:
    // here a menu label ("small", "large") instead of a size in cm.
    println("From a label: ${Pizza("large").describe()}")

    // ── 5. WHY default values? ───────────────────────────────────────
    // In Java you'd write 4 constructors for every combination of
    // "size given / cheese given". In Kotlin, one primary constructor
    // with defaults + named arguments covers them all. Reach for a
    // secondary constructor only when the input is really different.
    // Never add two constructors that take the same types (e.g. one Int
    // in cm and one Int in inches): Pizza(30) would silently pick one.
    // Use a named factory function instead (lesson 05.8).

    // ── 6. PITFALL: a secondary constructor must delegate ────────────
    //   class Pizza(val size: Int) {
    //       constructor(name: String) { }  // ❌ Primary constructor call expected.
    //   }

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Move init block #2 above `greeting`. Predict the new order, then run.
    // b) Add `init { require(nameArg.isNotBlank()) { "Name required" } }`
    //    and create Member(""). What happens?
    // c) Give Pizza a third parameter `slices: Int = 8`.
}

private const val DEFAULT_ROLE = "Engineer"
private const val DEFAULT_PIZZA_CM = 30
private const val SMALL_PIZZA_CM = 25
private const val LARGE_PIZZA_CM = 40

/** Prints [message] and returns [value]: lets us SEE when an initializer runs. */
private fun trace(message: String, value: String): String {
    println(message)
    return value
}

// `nameArg` has no val/var: it's a plain constructor PARAMETER, usable
// only during initialization. `role` has val: it becomes a PROPERTY.
class Member(nameArg: String, val role: String = DEFAULT_ROLE) {
    val name: String = trace("1. property initializer: name = $nameArg", nameArg)

    init {
        println("2. init block #1: $name")
    }

    val greeting: String = trace("3. property initializer: greeting = Hi, $name", "Hi, $name")

    init {
        println("4. init block #2: greeting ready")
    }
}

class Pizza(val size: Int = DEFAULT_PIZZA_CM, val cheese: Boolean = true) {
    // Secondary constructor: turns a menu label into a size, then
    // delegates to the primary constructor with `: this(...)`.
    constructor(label: String) : this(size = sizeForLabel(label))

    fun describe(): String = "Pizza(size=$size cm, cheese=$cheese)"
}

private fun sizeForLabel(label: String): Int {
    val size =
        when (label) {
            "small" -> SMALL_PIZZA_CM
            "medium" -> DEFAULT_PIZZA_CM
            "large" -> LARGE_PIZZA_CM
            else -> null
        }
    // require(...) throws IllegalArgumentException when false (lesson 05.4).
    require(size != null) { "Unknown pizza label: $label" }
    return size
}
