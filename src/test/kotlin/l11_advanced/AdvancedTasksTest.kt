package l11_advanced

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Shared checks for Level 11. Subclasses decide WHICH implementation is
 * tested: the learner's exercises or the reference solutions.
 */
@TestMethodOrder(MethodOrderer.MethodName::class) // ex01, ex02, ... in order
abstract class AdvancedTasksTest(
    private val tasks: AdvancedTasks,
) {
    @Test
    fun `ex01 safeDivide wraps the ArithmeticException`() {
        assertEquals(Result.success(4), tasks.safeDivide(8, 2))
        val failure = tasks.safeDivide(1, 0)
        assertTrue(failure.isFailure, "Hint: dividing by zero must give a Failure, not a crash")
        assertIs<ArithmeticException>(failure.exceptionOrNull())
    }

    @Test
    fun `ex02 parsePercent separates bad text from bad numbers`() {
        assertEquals(Result.success(42), tasks.parsePercent(" 42 "))
        assertEquals(Result.success(0), tasks.parsePercent("0"))
        assertEquals(Result.success(100), tasks.parsePercent("100"))
        assertIs<NumberFormatException>(tasks.parsePercent("abc").exceptionOrNull())
        for (outOfRange in listOf("150", "-1")) {
            val error = tasks.parsePercent(outOfRange).exceptionOrNull()
            assertIs<IllegalArgumentException>(error, "Hint: map would let require's exception escape; use mapCatching")
            assertFalse(
                error is NumberFormatException,
                "Hint: \"$outOfRange\" IS a number; report the range problem, not a parse problem",
            )
        }
    }

    @Test
    fun `ex03 useBoth closes in reverse order, even on failure`() {
        val log = mutableListOf<String>()
        tasks.useBoth(log, failInside = false)
        assertEquals(listOf("open a", "open b", "work", "close b", "close a"), log)

        val failingLog = mutableListOf<String>()
        assertFailsWith<IllegalStateException> { tasks.useBoth(failingLog, failInside = true) }
        assertEquals(
            listOf("open a", "open b", "work", "close b", "close a"),
            failingLog,
            "Hint: use {} must still close both resources when the work throws",
        )
    }

    @Test
    fun `ex04 cents supports plus, times and comparison`() {
        val tea = tasks.cents(250)
        val cake = tasks.cents(400)
        assertEquals(650, (tea + cake).amount)
        assertEquals(750, (tea * 3).amount)
        assertTrue(tea < cake)
        assertEquals(cake.amount, maxOf(tea, cake).amount)
        assertEquals(0, tasks.cents(0).amount)
        assertFailsWith<IllegalArgumentException>("Hint: negative money is a caller bug: require") { tasks.cents(-1) }
    }

    @Test
    fun `ex05 countingList counts add calls and still works as a list`() {
        val list = tasks.countingList()
        list.add("a")
        list.add("b")
        list.add("c")
        assertEquals(3, list.adds)
        assertEquals(listOf("a", "b", "c"), list.toList())
        list.removeAt(0)
        assertEquals(listOf("b", "c"), list.toList(), "Hint: every other method should still work: `by`")
        assertEquals(3, list.adds, "Removing must not change the add count")
    }

    @Test
    fun `ex06 thermostat ignores out-of-range values`() {
        val thermostat = tasks.thermostat(20)
        assertEquals(20, thermostat.celsius)
        thermostat.celsius = 25
        assertEquals(25, thermostat.celsius)
        thermostat.celsius = MAX_CELSIUS + 1
        assertEquals(25, thermostat.celsius, "Hint: vetoable returns false to keep the old value")
        thermostat.celsius = MIN_CELSIUS
        assertEquals(MIN_CELSIUS, thermostat.celsius, "The limits themselves are allowed")
        assertFailsWith<IllegalArgumentException> { tasks.thermostat(MAX_CELSIUS + 1) }
    }

    @Test
    fun `ex07 trimmedHolder stores trimmed text`() {
        val holder = tasks.trimmedHolder()
        assertEquals("", holder.text)
        holder.text = "  hi  "
        assertEquals("hi", holder.text)
        holder.text = "a b"
        assertEquals("a b", holder.text, "Only the ends are trimmed")
    }

    @Test
    fun `ex08 routes collects declarations in order`() {
        val routes =
            tasks.routes {
                get("/home")
                post("/login")
                get("/about")
            }
        assertEquals(listOf(Route("GET", "/home"), Route("POST", "/login"), Route("GET", "/about")), routes)
        assertEquals(emptyList(), tasks.routes { })
        assertFailsWith<IllegalArgumentException>("Hint: require path to start with /") { tasks.routes { get("home") } }
    }

    @Test
    fun `ex09 nextLight follows the rules`() {
        assertEquals(Light.Green, tasks.nextLight(Light.Red, LightEvent.Timer))
        assertEquals(Light.Yellow, tasks.nextLight(Light.Green, LightEvent.Timer))
        assertEquals(Light.Red, tasks.nextLight(Light.Yellow, LightEvent.Timer))
        assertEquals(Light.Off, tasks.nextLight(Light.Off, LightEvent.Timer))
        for (light in listOf(Light.Red, Light.Green, Light.Yellow, Light.Off)) {
            assertEquals(Light.Off, tasks.nextLight(light, LightEvent.PowerOff))
        }
        assertEquals(Light.Red, tasks.nextLight(Light.Off, LightEvent.PowerOn))
        assertEquals(Light.Green, tasks.nextLight(Light.Green, LightEvent.PowerOn), "A light that is on stays")
    }

    @Test
    fun `ex10 requiredFields reads the annotation`() {
        assertEquals(listOf("email", "name"), tasks.requiredFields(SignUp::class.java))
        assertEquals(emptyList(), tasks.requiredFields(Plain::class.java))
    }

    private data class SignUp(
        @field:Required val name: String,
        val nickname: String,
        @field:Required val email: String,
    )

    private data class Plain(
        val id: Int,
    )
}
