package l05_oop

/**
 * The list of Level 05 exercises.
 *
 * Both [l05_oop.exercises.OopExercises] (your answers) and
 * [l05_oop.solutions.OopSolutions] (reference answers) implement this
 * interface, so the SAME tests can check both.
 *
 * Some exercises ask you to WRITE A CLASS. The tests can't know your class
 * name, so they use the small interfaces/types below (Counter, Thermometer,
 * ...) and a factory function that returns your object.
 */
interface OopTasks {
    /**
     * Ex 1. A counter that starts at [start]. increment() adds 1, reset()
     * goes back to [start]. Nobody outside may set `value` directly.
     */
    fun createCounter(start: Int = 0): Counter

    /**
     * Ex 2. A thermometer holding a Celsius value. Setting celsius below
     * [ABSOLUTE_ZERO_CELSIUS] throws IllegalArgumentException and keeps the
     * old value. Creating one below absolute zero throws too.
     * fahrenheit is computed: celsius * 9 / 5 + 32.
     */
    fun createThermometer(celsius: Double): Thermometer

    /** Ex 3. A NEW point moved by (dx, dy). The original must not change. */
    fun moveBy(point: Point, dx: Int, dy: Int): Point

    /**
     * Ex 4. Delivery cost in whole euros:
     * Pickup -> 0, Standard -> 5 + 1 per km, Express -> 10 + 2 per km.
     * Use an exhaustive `when` (no else branch).
     */
    fun deliveryCost(delivery: Delivery): Int

    /** Ex 5. The next light in the cycle RED -> GREEN -> YELLOW -> RED. */
    fun nextLight(light: TrafficLight): TrafficLight

    /**
     * Ex 6. Add two amounts of money. Different currencies throw
     * IllegalArgumentException.  add(Money(150, "EUR"), Money(250, "EUR")) == Money(400, "EUR")
     */
    fun add(a: Money, b: Money): Money

    /**
     * Ex 7. A bank account in cents.
     * - deposit/withdraw with an amount <= 0 throw IllegalArgumentException
     *   (an invalid argument is a caller bug).
     * - withdraw returns false and changes nothing when the amount is more
     *   than the balance (a normal refusal, not a bug).
     */
    fun createAccount(owner: String, initialCents: Long): Account

    /**
     * Ex 8. A robot greeter. greet() returns "BEEP. " followed by the
     * DEFAULT greeting from [Greeter].  name "R2" -> "BEEP. Hello, I'm R2"
     */
    fun createRobotGreeter(name: String): Greeter
}

const val ABSOLUTE_ZERO_CELSIUS = -273.15

interface Counter {
    val value: Int

    fun increment()

    fun reset()
}

interface Thermometer {
    var celsius: Double
    val fahrenheit: Double
}

data class Point(val x: Int, val y: Int)

sealed interface Delivery {
    data object Pickup : Delivery

    data class Standard(val km: Int) : Delivery

    data class Express(val km: Int) : Delivery
}

enum class TrafficLight { RED, GREEN, YELLOW }

data class Money(val cents: Long, val currency: String)

interface Account {
    val owner: String
    val balanceCents: Long

    fun deposit(cents: Long)

    fun withdraw(cents: Long): Boolean
}

interface Greeter {
    val name: String

    fun greet(): String = "Hello, I'm $name"
}
