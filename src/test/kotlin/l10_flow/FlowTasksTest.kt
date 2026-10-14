// currentTime (virtual time) is still marked experimental in kotlinx-coroutines-test.
@file:OptIn(ExperimentalCoroutinesApi::class)

package l10_flow

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Shared checks for Level 10. Subclasses decide WHICH implementation is
 * tested: the learner's exercises or the reference solutions.
 *
 * runTest uses virtual time: delay(1000) is instant, and `currentTime`
 * tells how much virtual time has passed. `test { }` comes from Turbine:
 * it collects a flow and lets you check the values one by one.
 */
@TestMethodOrder(MethodOrderer.MethodName::class) // ex01, ex02, ... in order
abstract class FlowTasksTest(
    private val tasks: FlowTasks,
) {
    @Test
    fun `ex01 evenSquares keeps even numbers and squares them`() =
        runTest {
            assertEquals(listOf(4, 16), tasks.evenSquares(flowOf(1, 2, 3, 4)).toList())
            assertEquals(listOf(0, 4), tasks.evenSquares(flowOf(0, -2, -3)).toList(), "Hint: 0 and -2 are even too")
            assertEquals(emptyList(), tasks.evenSquares(emptyFlow()).toList())
        }

    @Test
    fun `ex02 runningTotal emits a total after each number`() =
        runTest {
            val totals = tasks.runningTotal(flowOf(1, 2, 3)).toList()
            assertEquals(listOf(1, 3, 6), totals, "Hint: no extra 0 at the start")
            assertEquals(emptyList(), tasks.runningTotal(emptyFlow()).toList())
        }

    @Test
    fun `ex03 numbered counts from 1`() =
        runTest {
            assertEquals(
                listOf("1. a", "2. b"),
                tasks.numbered(flowOf("a", "b")).toList(),
                "Hint: withIndex starts at 0",
            )
        }

    @Test
    fun `ex04 untilFirstNegative includes the negative and stops`() =
        runTest {
            assertEquals(listOf(3, 7, -1), tasks.untilFirstNegative(flowOf(3, 7, -1, 5)).toList())
            val stopsEarly =
                flow {
                    emit(1)
                    emit(-2)
                    throw AssertionError("Hint: stop collecting the upstream after the first negative")
                }
            tasks.untilFirstNegative(stopsEarly).test {
                assertEquals(1, awaitItem())
                assertEquals(-2, awaitItem(), "Hint: the first negative number is emitted too")
                awaitComplete()
            }
        }

    @Test
    fun `ex05 batches groups items and validates size early`() =
        runTest {
            val pairs = tasks.batches(flowOf(1, 2, 3, 4, 5), 2).toList()
            assertEquals(listOf(listOf(1, 2), listOf(3, 4), listOf(5)), pairs)
            assertEquals(listOf(listOf("a", "b")), tasks.batches(flowOf("a", "b"), 3).toList())
            assertEquals(emptyList(), tasks.batches(emptyFlow<Int>(), 2).toList())
            assertFailsWith<IllegalArgumentException>("Hint: check size BEFORE building the flow") {
                tasks.batches(flowOf(1), 0) // no collect: must fail on the call itself
            }
        }

    @Test
    fun `ex06 average or null`() =
        runTest {
            assertEquals(2.5, tasks.average(flowOf(1, 2, 3, 4)))
            assertEquals(-1.0, tasks.average(flowOf(-1)))
            assertNull(tasks.average(emptyFlow()), "Hint: an empty flow has no average")
        }

    @Test
    fun `ex07 latestPerKey keeps the newest value of each key`() =
        runTest {
            val events = flowOf("a" to 1, "b" to 2, "a" to 3)
            assertEquals(
                listOf(mapOf("a" to 1), mapOf("a" to 1, "b" to 2), mapOf("a" to 3, "b" to 2)),
                tasks.latestPerKey(events).toList(),
                "Hint: no empty map at the start",
            )
        }

    @Test
    fun `ex08 searchQueries waits for a pause and skips repeats`() =
        runTest {
            val typed =
                flow {
                    emit("k")
                    delay(100)
                    emit("ko")
                    delay(100)
                    emit("kot")
                    delay(200) // shorter than the wait...
                    emit("   ") // ...but blank: must NOT reset the wait for "kot"
                    delay(500)
                    emit("kotlin")
                    delay(500)
                    emit("kotlin ") // same query after trim: must not repeat
                }
            assertEquals(
                listOf("kot", "kotlin"),
                tasks.searchQueries(typed, 300).toList(),
                "Hint: skip blanks BEFORE debounce, and use distinctUntilChanged after it",
            )
            assertFailsWith<IllegalArgumentException> { tasks.searchQueries(flowOf("x"), 0) }
        }

    @Test
    fun `ex09 sumOfLatest adds the latest values`() =
        runTest {
            val a =
                flow {
                    emit(1)
                    delay(200)
                    emit(5)
                }
            val b =
                flow {
                    delay(100)
                    emit(10)
                }
            assertEquals(listOf(11, 15), tasks.sumOfLatest(a, b).toList())
            assertEquals(200, currentTime, "Hint: combine runs both flows at the same time")
        }

    @Test
    fun `ex10 withRetries collects again after a failure`() =
        runTest {
            var attempts = 0
            val flaky =
                flow {
                    attempts++
                    if (attempts < 3) throw IOException("network down")
                    emit("data")
                }
            assertEquals(listOf("data"), tasks.withRetries(flaky, 2).toList())
            assertEquals(3, attempts)

            attempts = 0
            assertFailsWith<IOException>("Hint: after the last retry the error must reach the collector") {
                tasks.withRetries(flaky, 1).toList()
            }
            attempts = 0
            assertFailsWith<IOException>("Hint: retries = 0 means try once and never again") {
                tasks.withRetries(flaky, 0).toList()
            }
            assertEquals(1, attempts, "Hint: with retries = 0 the flow is collected exactly once")
            assertFailsWith<IllegalArgumentException> { tasks.withRetries(flowOf("x"), -1) }
        }
}
