// currentTime (virtual time) is still marked experimental in kotlinx-coroutines-test.
@file:OptIn(ExperimentalCoroutinesApi::class)

package l09_coroutines

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import java.util.Collections
import java.util.concurrent.BrokenBarrierException
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Shared checks for Level 09. Subclasses decide WHICH implementation is
 * tested: the learner's exercises or the reference solutions.
 *
 * runTest uses virtual time: delay(1000) is instant, and `currentTime`
 * tells how much virtual time has passed.
 */
@TestMethodOrder(MethodOrderer.MethodName::class) // ex01, ex02, ... in order
abstract class CoroutinesTasksTest(
    private val tasks: CoroutinesTasks,
) {
    @Test
    fun `ex01 fetchBoth runs both at the same time`() =
        runTest {
            val result = tasks.fetchBoth({ delay(1_000); "user" }, { delay(2_000); "orders" })
            assertEquals("user" to "orders", result)
            assertEquals(2_000, currentTime, "Hint: one after the other takes 3000 ms; start both with async first")
        }

    @Test
    fun `ex02 sumAll adds concurrently`() =
        runTest {
            val producers: List<suspend () -> Int> =
                listOf({ delay(100); 1 }, { delay(300); 2 }, { delay(200); 3 })
            assertEquals(6, tasks.sumAll(producers))
            assertEquals(300, currentTime, "Hint: the total time should be the slowest producer, not the sum")
            assertEquals(0, tasks.sumAll(emptyList()))
        }

    @Test
    fun `ex03 parallelMap keeps the order`() =
        runTest {
            val result =
                tasks.parallelMap(listOf(3, 1, 2)) { n ->
                    delay(n * 100L) // 1 finishes first, 3 last
                    n * 10
                }
            assertEquals(listOf(30, 10, 20), result, "Hint: results must follow the input order")
            assertEquals(300, currentTime, "Hint: run the transforms concurrently")
            assertEquals(emptyList(), tasks.parallelMap(emptyList<Int>()) { it })
        }

    @Test
    fun `ex04 loadOrDefault gives up after the timeout`() =
        runTest {
            assertEquals("fast", tasks.loadOrDefault(500, "default") { delay(100); "fast" })
            assertEquals(100, currentTime)
            assertEquals("default", tasks.loadOrDefault(300, "default") { delay(1_000); "slow" })
            assertEquals(400, currentTime, "Hint: stop waiting after 300 ms, don't wait the full 1000 ms")
            assertFailsWith<IllegalArgumentException> { tasks.loadOrDefault(0, "d") { "x" } }
        }

    @Test
    fun `ex05 retryWithBackoff waits longer each time`() =
        runTest {
            var calls = 0
            val result =
                tasks.retryWithBackoff(times = 4, initialDelayMillis = 100, factor = 2) {
                    calls++
                    check(calls >= 3) { "attempt $calls failed" }
                    "ok after $calls"
                }
            assertEquals("ok after 3", result)
            assertEquals(300, currentTime, "Hint: wait 100 ms, then 200 ms -> 300 ms in total")
        }

    @Test
    fun `ex05 retryWithBackoff throws the last error`() =
        runTest {
            var calls = 0
            val error =
                assertFailsWith<IllegalStateException> {
                    tasks.retryWithBackoff(times = 3, initialDelayMillis = 100, factor = 3) {
                        calls++
                        throw IllegalStateException("failure $calls")
                    }
                }
            assertEquals("failure 3", error.message, "Hint: rethrow the LAST error")
            assertEquals(400, currentTime, "Hint: waits 100 + 300 ms, and no wait after the last attempt")
            assertFailsWith<IllegalArgumentException> { tasks.retryWithBackoff(0, 100, 2) { "x" } }
        }

    @Test
    fun `ex06 countCooperatively counts and can be cancelled`() =
        runTest {
            val all = mutableListOf<Int>()
            tasks.countCooperatively(5) { all += it }
            assertEquals(listOf(0, 1, 2, 3, 4), all)

            val ticks = mutableListOf<Int>()
            val job = launch { tasks.countCooperatively(1_000_000) { ticks += it } }
            repeat(3) { yield() } // let the counter run a few steps
            job.cancelAndJoin()
            assertTrue(ticks.size < 1_000_000, "Hint: the loop never let anyone else run, so cancel came too late")
            assertTrue(ticks.isNotEmpty(), "The counter should have started before it was cancelled")
        }

    @Test
    fun `ex07 countSuccesses survives failures`() =
        runTest {
            val jobs: List<suspend () -> Unit> =
                listOf(
                    { delay(100) },
                    { delay(50); error("boom") },
                    { delay(100) },
                )
            assertEquals(2, tasks.countSuccesses(jobs), "Hint: a failing job must not cancel the others")
            assertEquals(100, currentTime, "Hint: run the jobs concurrently")
            assertEquals(0, tasks.countSuccesses(emptyList()))
        }

    @Test
    fun `ex08 safeCount never loses an update`() =
        runTest {
            assertEquals(5, tasks.safeCount(1, 5))
            assertEquals(
                80_000,
                tasks.safeCount(8, 10_000),
                "Hint: count++ from many threads loses updates; use a Mutex",
            )
            assertFailsWith<IllegalArgumentException> { tasks.safeCount(0, 1) }
        }

    @Test
    fun `ex09 squares sends squares then closes`() =
        runTest {
            assertEquals(listOf(1, 4, 9, 16), with(tasks) { squares(4) }.toList())
            assertEquals(emptyList(), with(tasks) { squares(0) }.toList(), "Hint: the channel must close when done")
            assertFailsWith<IllegalArgumentException>(
                "Hint: call require BEFORE produce { }, so the caller gets the exception",
            ) { with(tasks) { squares(-1) } }
        }

    @Test
    fun `ex10 parallelSumOf sums slices on several cores at once`() =
        runTest {
            assertEquals(60L, tasks.parallelSumOf(listOf(1, 2, 3), 2) { it * 10L })
            assertEquals(5_050L, tasks.parallelSumOf((1..100).toList(), 7) { it.toLong() })
            assertEquals(6L, tasks.parallelSumOf(listOf(1, 2, 3), 10) { it.toLong() }, "Hint: more chunks than items")
            assertEquals(0L, tasks.parallelSumOf(emptyList(), 4) { it.toLong() })
            assertFailsWith<IllegalArgumentException> { tasks.parallelSumOf(listOf(1), 0) { it.toLong() } }

            // Two slices, one item each. The barrier only opens if BOTH slices
            // wait at it at the same instant, i.e. on two threads in parallel.
            val barrier = CyclicBarrier(2)
            val threadNames = Collections.synchronizedList(mutableListOf<String>())
            val met =
                tasks.parallelSumOf(listOf(1, 1), 2) {
                    threadNames += Thread.currentThread().name
                    try {
                        barrier.await(1, TimeUnit.SECONDS)
                        1L
                    } catch (e: TimeoutException) {
                        0L
                    } catch (e: BrokenBarrierException) {
                        0L
                    }
                }
            assertTrue(
                threadNames.all { it.startsWith("DefaultDispatcher-worker") },
                "Hint: run the slices on Dispatchers.Default, they ran on $threadNames",
            )
            assertEquals(2L, met, "Hint: the slices took turns; start each one with async on Dispatchers.Default")
        }
}
