package l05_oop

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Shared checks for Level 05. Subclasses decide WHICH implementation is
 * tested: the learner's exercises or the reference solutions.
 */
@TestMethodOrder(MethodOrderer.MethodName::class) // ex1, ex2, ... in order
abstract class OopTasksTest(
    private val tasks: OopTasks,
) {
    @Test
    fun `ex1 counter increments and resets to its start`() {
        val counter = tasks.createCounter(start = 10)
        assertEquals(10, counter.value)
        counter.increment()
        counter.increment()
        assertEquals(12, counter.value)
        counter.reset()
        assertEquals(10, counter.value, "Hint: reset() goes back to START, not to 0")
        assertEquals(0, tasks.createCounter().value, "Default start is 0")
    }

    @Test
    fun `ex1 counters are independent objects`() {
        val a = tasks.createCounter()
        val b = tasks.createCounter()
        a.increment()
        assertEquals(1, a.value)
        assertEquals(0, b.value, "Hint: each object needs its OWN value property")
    }

    @Test
    fun `ex2 thermometer computes fahrenheit and rejects impossible values`() {
        val thermometer = tasks.createThermometer(100.0)
        assertEquals(212.0, thermometer.fahrenheit, DELTA)
        thermometer.celsius = -40.0
        assertEquals(-40.0, thermometer.fahrenheit, DELTA, "Hint: fahrenheit must be computed on every read")
        assertFailsWith<IllegalArgumentException> { thermometer.celsius = -300.0 }
        assertEquals(-40.0, thermometer.celsius, DELTA, "A rejected value must not replace the old one")
        thermometer.celsius = ABSOLUTE_ZERO_CELSIUS
        assertEquals(ABSOLUTE_ZERO_CELSIUS, thermometer.celsius, DELTA, "Absolute zero itself is allowed")
    }

    @Test
    fun `ex2 thermometer rejects an impossible start value`() {
        assertFailsWith<IllegalArgumentException>("Hint: the setter does not run for the initial value; check in init") {
            tasks.createThermometer(-500.0)
        }
    }

    @Test
    fun `ex3 moveBy returns a new point and keeps the original`() {
        val start = Point(1, 2)
        assertEquals(Point(4, 0), tasks.moveBy(start, dx = 3, dy = -2))
        assertEquals(Point(1, 2), start)
        assertEquals(start, tasks.moveBy(start, 0, 0))
    }

    @Test
    fun `ex4 deliveryCost handles every kind`() {
        assertEquals(0, tasks.deliveryCost(Delivery.Pickup))
        assertEquals(15, tasks.deliveryCost(Delivery.Standard(km = 10)))
        assertEquals(30, tasks.deliveryCost(Delivery.Express(km = 10)))
        assertEquals(5, tasks.deliveryCost(Delivery.Standard(km = 0)))
    }

    @Test
    fun `ex5 nextLight cycles through all lights`() {
        assertEquals(TrafficLight.GREEN, tasks.nextLight(TrafficLight.RED))
        assertEquals(TrafficLight.YELLOW, tasks.nextLight(TrafficLight.GREEN))
        assertEquals(TrafficLight.RED, tasks.nextLight(TrafficLight.YELLOW), "Hint: after YELLOW comes RED again")
    }

    @Test
    fun `ex6 add sums the same currency and rejects mixed ones`() {
        assertEquals(Money(400, "EUR"), tasks.add(Money(150, "EUR"), Money(250, "EUR")))
        assertEquals(Money(-50, "USD"), tasks.add(Money(50, "USD"), Money(-100, "USD")))
        assertFailsWith<IllegalArgumentException>("Hint: require(...) that the currencies match") {
            tasks.add(Money(1, "EUR"), Money(1, "USD"))
        }
    }

    @Test
    fun `ex7 account guards its balance`() {
        val account = tasks.createAccount("Ada", initialCents = 1_000)
        assertEquals("Ada", account.owner)
        assertEquals(1_000, account.balanceCents)
        account.deposit(500)
        assertEquals(1_500, account.balanceCents)
        assertTrue(account.withdraw(1_500))
        assertEquals(0, account.balanceCents)
        assertFalse(account.withdraw(1), "Can't withdraw more than the balance")
        assertEquals(0, account.balanceCents, "A refused withdrawal changes nothing")
        assertFailsWith<IllegalArgumentException>("Hint: require(cents > 0)") { account.withdraw(0) }
        assertFailsWith<IllegalArgumentException> { account.withdraw(-5) }
        assertFailsWith<IllegalArgumentException> { account.deposit(0) }
        assertFailsWith<IllegalArgumentException> { account.deposit(-1) }
    }

    @Test
    fun `ex8 robot reuses the default greeting`() {
        val robot = tasks.createRobotGreeter("R2")
        assertEquals("R2", robot.name)
        assertEquals("BEEP. Hello, I'm R2", robot.greet(), "Hint: \"BEEP. \" + super.greet()")
    }

    private companion object {
        const val DELTA = 1e-9
    }
}
