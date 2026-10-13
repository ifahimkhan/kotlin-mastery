package l05_oop.solutions

import l05_oop.ABSOLUTE_ZERO_CELSIUS
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
 * Reference answers for Level 05. Try the exercises first!
 * Each answer explains WHY it is written this way.
 */
object OopSolutions : OopTasks {
    override fun createCounter(start: Int): Counter = SimpleCounter(start)

    override fun createThermometer(celsius: Double): Thermometer = ValidatedThermometer(celsius)

    // copy() builds a NEW Point; `point` itself is never modified.
    override fun moveBy(point: Point, dx: Int, dy: Int): Point = point.copy(x = point.x + dx, y = point.y + dy)

    // No `else`: if a new Delivery kind is added, this stops compiling
    // until it is handled. Pickup is a data object, so no `is` is needed.
    override fun deliveryCost(delivery: Delivery): Int =
        when (delivery) {
            Delivery.Pickup -> 0
            is Delivery.Standard -> STANDARD_BASE + STANDARD_PER_KM * delivery.km
            is Delivery.Express -> EXPRESS_BASE + EXPRESS_PER_KM * delivery.km
        }

    // A `when` over all enum values is exhaustive, so no else is needed.
    // (Using ordinal + 1 would silently break if someone reorders the enum.)
    override fun nextLight(light: TrafficLight): TrafficLight =
        when (light) {
            TrafficLight.RED -> TrafficLight.GREEN
            TrafficLight.GREEN -> TrafficLight.YELLOW
            TrafficLight.YELLOW -> TrafficLight.RED
        }

    // Adding EUR to USD is a bug, not a number: fail loudly at once.
    override fun add(a: Money, b: Money): Money {
        require(a.currency == b.currency) { "Cannot add ${a.currency} to ${b.currency}" }
        return Money(a.cents + b.cents, a.currency)
    }

    override fun createAccount(owner: String, initialCents: Long): Account = SimpleAccount(owner, initialCents)

    override fun createRobotGreeter(name: String): Greeter = Robot(name)

    private const val STANDARD_BASE = 5
    private const val STANDARD_PER_KM = 1
    private const val EXPRESS_BASE = 10
    private const val EXPRESS_PER_KM = 2
}

// `private set`: everyone can read value, only this class can change it.
private class SimpleCounter(private val start: Int) : Counter {
    override var value: Int = start
        private set

    override fun increment() {
        value += 1
    }

    override fun reset() {
        value = start
    }
}

private class ValidatedThermometer(initialCelsius: Double) : Thermometer {
    // The setter does NOT run for the initializer, so check here too.
    init {
        require(initialCelsius >= ABSOLUTE_ZERO_CELSIUS) { "Below absolute zero: $initialCelsius" }
    }

    // `require` throws BEFORE `field` is assigned, so a bad value never
    // replaces the old one.
    override var celsius: Double = initialCelsius
        set(value) {
            require(value >= ABSOLUTE_ZERO_CELSIUS) { "Below absolute zero: $value" }
            field = value
        }

    // Computed on every read, so it can never disagree with celsius.
    override val fahrenheit: Double
        get() = celsius * FAHRENHEIT_RATIO + FAHRENHEIT_OFFSET

    private companion object {
        const val FAHRENHEIT_RATIO = 9.0 / 5.0
        const val FAHRENHEIT_OFFSET = 32.0
    }
}

// Same shape as Wallet in lesson 04: private set + methods that guard the rules.
private class SimpleAccount(override val owner: String, initialCents: Long) : Account {
    override var balanceCents: Long = initialCents
        private set

    override fun deposit(cents: Long) {
        require(cents > 0) { "Deposit must be positive, was $cents" }
        balanceCents += cents
    }

    // Two kinds of "no": a non-positive amount is a caller BUG -> require
    // throws. Not enough money is a normal business refusal -> false.
    override fun withdraw(cents: Long): Boolean {
        require(cents > 0) { "Withdrawal must be positive, was $cents" }
        if (cents > balanceCents) return false
        balanceCents -= cents
        return true
    }
}

// super.greet() calls the interface's default body: no copy-pasted text.
private class Robot(override val name: String) : Greeter {
    override fun greet(): String = "BEEP. ${super.greet()}"
}
