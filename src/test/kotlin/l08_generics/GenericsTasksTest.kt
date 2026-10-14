package l08_generics

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Shared checks for Level 08. Subclasses decide WHICH implementation is
 * tested: the learner's exercises or the reference solutions.
 * Each generic function is tried with several types.
 */
@TestMethodOrder(MethodOrderer.MethodName::class) // ex01, ex02, ... in order
abstract class GenericsTasksTest(
    private val tasks: GenericsTasks,
) {
    @Test
    fun `ex01 firstOrDefault works for any type`() {
        assertEquals(3, tasks.firstOrDefault(listOf(3, 4), 0))
        assertEquals("x", tasks.firstOrDefault(emptyList(), "x"), "Hint: an empty list returns the default")
        assertNull(tasks.firstOrDefault(listOf(null, "b"), "d"), "The first item may itself be null")
    }

    @Test
    fun `ex02 swap reverses the pair types`() {
        val swapped: Pair<Int, String> = tasks.swap("a" to 1)
        assertEquals(1 to "a", swapped)
        assertEquals(true to 'c', tasks.swap('c' to true))
    }

    @Test
    fun `ex03 largest uses Comparable`() {
        assertEquals(9, tasks.largest(listOf(3, 9, 4)))
        assertEquals("pear", tasks.largest(listOf("apple", "pear", "fig")))
        assertEquals(-1, tasks.largest(listOf(-5, -1, -3)), "Hint: don't start from 0, negatives exist")
        assertNull(tasks.largest(emptyList<Int>()))
    }

    @Test
    fun `ex04 mergeSorted keeps order and duplicates`() {
        assertEquals(listOf(1, 2, 4, 4, 9), tasks.mergeSorted(listOf(1, 4, 9), listOf(2, 4)))
        assertEquals(listOf("a", "b", "c"), tasks.mergeSorted(listOf("b"), listOf("a", "c")))
        assertEquals(listOf(5), tasks.mergeSorted(emptyList(), listOf(5)))
        assertEquals(emptyList(), tasks.mergeSorted(emptyList<Int>(), emptyList()))
        // Stable: on equal ranks, items from `first` come before items from `second`.
        val merged =
            tasks.mergeSorted(
                listOf(Ranked(1, "first-a"), Ranked(2, "first-b")),
                listOf(Ranked(1, "second-a")),
            )
        assertEquals(
            listOf("first-a", "second-a", "first-b"),
            merged.map { it.label },
            "Hint: on a tie, take from `first` (use <=, not <)",
        )
    }

    @Test
    fun `ex05a stack is last in first out`() {
        val stack = tasks.createStack<String>()
        assertTrue(stack.isEmpty)
        assertNull(stack.pop(), "Hint: pop on an empty stack returns null")
        assertNull(stack.peek())
        stack.push("a")
        stack.push("b")
        assertEquals(2, stack.size)
        assertFalse(stack.isEmpty)
        assertEquals("b", stack.peek())
        assertEquals(2, stack.size, "peek must not remove")
        assertEquals("b", stack.pop())
        assertEquals("a", stack.pop())
        assertTrue(stack.isEmpty)
    }

    @Test
    fun `ex05b stacks are independent`() {
        val first = tasks.createStack<Int>()
        val second = tasks.createStack<Int>()
        first.push(1)
        assertTrue(second.isEmpty, "Hint: each stack needs its own list")
    }

    @Test
    fun `ex06 mapValuesNotNull drops null results`() {
        val parsed = tasks.mapValuesNotNull(mapOf("a" to "1", "b" to "x", "c" to "3")) { it.toIntOrNull() }
        assertEquals(mapOf("a" to 1, "c" to 3), parsed)
        assertEquals(listOf("a", "c"), parsed.keys.toList(), "Keys keep their order")
        assertEquals(emptyMap(), tasks.mapValuesNotNull(emptyMap<String, Int>()) { it })
    }

    @Test
    fun `ex07 fold handles both sides`() {
        val left: Either<String, Int> = Either.Left("boom")
        val right: Either<String, Int> = Either.Right(21)
        assertEquals("error: boom", tasks.fold(left, { "error: $it" }, { "value: $it" }))
        assertEquals("value: 42", tasks.fold(right, { "error: $it" }, { "value: ${it * 2}" }))
    }

    @Test
    fun `ex08 groupCount counts per key in first-seen order`() {
        val counts = tasks.groupCount(listOf("ant", "bee", "ape")) { it.first() }
        assertEquals(mapOf('a' to 2, 'b' to 1), counts)
        assertEquals(listOf('a', 'b'), counts.keys.toList())
        assertEquals(mapOf(true to 2, false to 1), tasks.groupCount(listOf(2, 3, 4)) { it % 2 == 0 })
        assertEquals(emptyMap(), tasks.groupCount(emptyList<String>()) { it.length })
    }

    @Test
    fun `ex09 copyAll accepts a list of a supertype`() {
        val target: MutableList<Any> = mutableListOf(0)
        tasks.copyAll(listOf("a", "b"), target)
        assertEquals(listOf<Any>(0, "a", "b"), target)
        val numbers: MutableList<Number> = mutableListOf()
        tasks.copyAll(listOf(1, 2), numbers)
        assertEquals(listOf<Number>(1, 2), numbers)
    }

    @Test
    fun `ex10 allOrNull is all or nothing`() {
        assertEquals(listOf(1, 2), tasks.allOrNull(listOf(1, 2)))
        assertNull(tasks.allOrNull(listOf(1, null, 3)))
        assertEquals(emptyList(), tasks.allOrNull(emptyList<String?>()), "Hint: an empty list has no nulls")
    }
}

/** Compares by [rank] only, so two different items can be "equal" (Ex 4 stability check). */
private class Ranked(
    val rank: Int,
    val label: String,
) : Comparable<Ranked> {
    override fun compareTo(other: Ranked): Int = rank.compareTo(other.rank)
}
