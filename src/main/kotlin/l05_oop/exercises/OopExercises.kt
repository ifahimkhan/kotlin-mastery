package l05_oop.exercises

import l05_oop.Account
import l05_oop.Counter
import l05_oop.Delivery
import l05_oop.Greeter
import l05_oop.Money
import l05_oop.OopTasks
import l05_oop.Point
import l05_oop.Thermometer
import l05_oop.TrafficLight

/**
 * =====================================================================
 *  LEVEL 05 — EXERCISES (your turn!)
 * =====================================================================
 *  HOW    : Replace each TODO("...") with your answer. For exercises 1,
 *           2, 7 and 8 the class is already started for you at the
 *           bottom of this file: complete its members. The stubs are only
 *           placeholders — rewrite a member freely. For example, replace
 *               override val value: Int get() = TODO(...)
 *           with
 *               override var value: Int = start
 *                   private set
 *           Ex 2 (MyThermometer) also needs an `init { }` block.
 *  CHECK  : ./gradlew exercises --tests "l05_*"
 *           or open src/test/kotlin/l05_oop/OopExercisesTest.kt
 *           and click ▶ next to the class name.
 *  STUCK? : Re-read the lesson in the hint. Still stuck? Peek at
 *           ../solutions/OopSolutions.kt — but try first!
 *
 *  Note: default values (like `start: Int = 0`) live in OopTasks.kt;
 *  an `override` may not repeat them.
 * =====================================================================
 */
object OopExercises : OopTasks {
    // Hint: lesson03_properties (private set) -> complete MyCounter below
    override fun createCounter(start: Int): Counter = MyCounter(start)

    // Hint: lesson03_properties (custom setter, `field`) + lesson02_constructors (init)
    // MyThermometer needs BOTH: an init block for the starting value and a setter.
    override fun createThermometer(celsius: Double): Thermometer = MyThermometer(celsius)

    // Hint: lesson07_data_classes (copy)
    override fun moveBy(point: Point, dx: Int, dy: Int): Point = TODO("Ex 3: return a moved copy")

    // Hint: lesson09_enums_sealed (exhaustive when with `is`, no else)
    override fun deliveryCost(delivery: Delivery): Int = TODO("Ex 4: cost per delivery kind")

    // Hint: lesson09_enums_sealed (when over enum values)
    override fun nextLight(light: TrafficLight): TrafficLight = TODO("Ex 5: RED -> GREEN -> YELLOW -> RED")

    // Hint: lesson07_data_classes (data class) + lesson04_visibility (require)
    override fun add(a: Money, b: Money): Money = TODO("Ex 6: same currency only")

    // Hint: lesson04_visibility (Wallet, require) -> complete MyAccount below
    override fun createAccount(owner: String, initialCents: Long): Account = MyAccount(owner, initialCents)

    // Hint: lesson06_interfaces (super<Name>.method()) -> complete MyRobot below
    override fun createRobotGreeter(name: String): Greeter = MyRobot(name)
}

// Ex 1: value starts at `start`; increment() adds 1; reset() goes back to start.
private class MyCounter(private val start: Int) : Counter {
    override val value: Int
        get() = TODO("Ex 1: store the value (hint: var + private set)")

    override fun increment() {
        TODO("Ex 1: add 1")
    }

    override fun reset() {
        TODO("Ex 1: back to start")
    }
}

// Ex 2: validate in an init block AND in the setter.
private class MyThermometer(initialCelsius: Double) : Thermometer {
    init {
        TODO("Ex 2: reject a start value below ABSOLUTE_ZERO_CELSIUS")
    }

    override var celsius: Double
        get() = TODO("Ex 2: store the value (hint: a property with an initializer + custom set)")
        set(value) {
            TODO("Ex 2: reject values below ABSOLUTE_ZERO_CELSIUS, then store the new value")
        }

    override val fahrenheit: Double
        get() = TODO("Ex 2: computed from celsius")
}

// Ex 7: balanceCents starts at initialCents; outsiders can only read it.
private class MyAccount(override val owner: String, initialCents: Long) : Account {
    override val balanceCents: Long
        get() = TODO("Ex 7: store the balance, starting at initialCents")

    override fun deposit(cents: Long) {
        TODO("Ex 7: positive amounts only")
    }

    override fun withdraw(cents: Long): Boolean = TODO("Ex 7: require positive; false if too much")
}

// Ex 8: reuse Greeter's DEFAULT greet() with super.
private class MyRobot(override val name: String) : Greeter {
    override fun greet(): String = TODO("Ex 8: \"BEEP. \" + the default greeting")
}
