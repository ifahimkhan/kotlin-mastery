package l07_functional

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Shared checks for Level 07. Subclasses decide WHICH implementation is
 * tested: the learner's exercises or the reference solutions.
 */
@TestMethodOrder(MethodOrderer.MethodName::class) // ex01, ex02, ... in order
abstract class FunctionalTasksTest(
    private val tasks: FunctionalTasks,
) {
    @Test
    fun `ex01 applyN applies the function n times`() {
        assertEquals(8, tasks.applyN({ it * 2 }, 3, 1))
        assertEquals(15, tasks.applyN({ it + 5 }, 2, 5))
        assertEquals(42, tasks.applyN({ it * 2 }, 0, 42), "Hint: n == 0 returns x unchanged")
        assertFailsWith<IllegalArgumentException>("Hint: a negative n is a caller bug, use require") {
            tasks.applyN({ it }, -1, 0)
        }
    }

    @Test
    fun `ex02 compose runs inner first then outer`() {
        assertEquals(21, tasks.compose({ it + 1 }, { it * 10 })(2), "Hint: inner (right) runs first: 2 * 10 + 1")
        assertEquals(30, tasks.compose({ it * 10 }, { it + 1 })(2))
        assertEquals(5, tasks.compose({ it }, { it })(5))
    }

    @Test
    fun `ex03 makeCounter counts from start and counters are independent`() {
        val a = tasks.makeCounter(5)
        val b = tasks.makeCounter(100)
        assertEquals(5, a(), "Hint: the first call returns start itself")
        assertEquals(6, a())
        assertEquals(100, b(), "Hint: each counter needs its own variable")
        assertEquals(7, a())
        assertEquals(101, b())
    }

    @Test
    fun `ex04 countMatching counts predicate matches`() {
        assertEquals(2, tasks.countMatching(listOf("a", "bb", "ccc")) { it.length > 1 })
        assertEquals(0, tasks.countMatching(emptyList()) { true })
        assertEquals(3, tasks.countMatching(listOf("x", "y", "z")) { true })
    }

    @Test
    fun `ex05 firstError returns the first error and stops there`() {
        val calls = mutableListOf<String>()
        val notBlank: (String) -> String? = {
            calls.add("notBlank")
            if (it.isBlank()) "must not be blank" else null
        }
        val shortEnough: (String) -> String? = {
            calls.add("shortEnough")
            if (it.length > 5) "too long" else null
        }
        val noDigits: (String) -> String? = {
            calls.add("noDigits")
            if (it.any { c -> c.isDigit() }) "no digits allowed" else null
        }
        val all = listOf(notBlank, shortEnough, noDigits)

        assertNull(tasks.firstError("ok", all))
        assertEquals("too long", tasks.firstError("abcdefg1", all))

        calls.clear()
        assertEquals("must not be blank", tasks.firstError(" ", all))
        assertEquals(listOf("notBlank"), calls, "Hint: stop at the first error, later validators must not run")

        assertNull(tasks.firstError("anything", emptyList()), "No validators means no errors")
    }

    @Test
    fun `ex06 memoize2 computes each pair once`() {
        var calls = 0
        val area =
            tasks.memoize2 { width, height ->
                calls++
                width.toLong() * height
            }
        assertEquals(6L, area(2, 3))
        assertEquals(6L, area(2, 3))
        assertEquals(1, calls, "Hint: the second area(2, 3) must come from the cache")
        assertEquals(6L, area(3, 2))
        assertEquals(2, calls, "Hint: (3, 2) is a different pair from (2, 3)")
        assertEquals(0L, area(0, 5))
        assertEquals(3, calls)
    }

    @Test
    fun `ex07 retry stops at the first success`() {
        val tried = mutableListOf<Int>()
        val result =
            tasks.retry(5) { number ->
                tried.add(number)
                number == 3
            }
        assertEquals(3, result)
        assertEquals(listOf(1, 2, 3), tried, "Hint: attempts are numbered from 1 and stop after a success")

        assertNull(tasks.retry(3) { false }, "Hint: no success means null")
        assertNull(tasks.retry(0) { true }, "Hint: maxAttempts == 0 makes no calls")
        assertFailsWith<IllegalArgumentException> { tasks.retry(-1) { true } }
    }

    @Test
    fun `ex08 fixFirst fixes the first of three arguments`() {
        val digits = tasks.fixFirst({ a, b, c -> a * 100 + b * 10 + c }, 1)
        assertEquals(123, digits(2, 3), "Hint: 1 is the FIRST argument, (2, 3) are the other two in order")
        assertEquals(100, digits(0, 0))
        val minus = tasks.fixFirst({ a, b, c -> a - b - c }, 10)
        assertEquals(5, minus(3, 2))
    }

    @Test
    fun `ex09 buildGreeting starts from defaults and applies the block`() {
        assertEquals("Hello, World!", tasks.buildGreeting { })
        assertEquals("HELLO, ADA!", tasks.buildGreeting { name = "Ada"; shout = true })
        assertEquals(
            "Hi, Alan?",
            tasks.buildGreeting {
                greeting = "Hi"
                name = "Alan"
                punctuation = "?"
            },
        )
    }

    @Test
    fun `ex10 tracedPipeline returns every stage in order`() {
        val clean = tasks.tracedPipeline(listOf({ it.trim() }, { it.uppercase() }))
        assertEquals(listOf("  hi ", "hi", "HI"), clean("  hi "), "Hint: the input itself is the first stage")
        val order = tasks.tracedPipeline(listOf({ it + "a" }, { it + "b" }))
        assertEquals(listOf("x", "xa", "xab"), order("x"), "Hint: steps run left to right")
        assertEquals(listOf("same"), tasks.tracedPipeline(emptyList())("same"), "Hint: no steps -> just the input")
    }
}
