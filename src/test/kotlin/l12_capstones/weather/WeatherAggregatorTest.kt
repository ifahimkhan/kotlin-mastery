package l12_capstones.weather

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import l12_capstones.weather.FakeWeatherSource.Reply
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Acceptance tests for the weather capstone, grouped by milestone.
 * Everything runs on virtual time: a 5-second timeout takes no real time,
 * and currentTime tells us exactly when things happened.
 */
// currentTime / advanceTimeBy / runCurrent are still experimental in kotlinx-coroutines-test.
@OptIn(ExperimentalCoroutinesApi::class)
@TestMethodOrder(MethodOrderer.MethodName::class)
abstract class WeatherAggregatorTest(
    private val aggregator: WeatherAggregator,
) {
    private val lisbon = City("Lisbon", 38.72, -9.14)
    private val oslo = City("Oslo", 59.91, 10.75)
    private val tokyo = City("Tokyo", 35.68, 139.69)
    private val rome = City("Rome", 41.9, 12.5)

    private val config =
        AggregatorConfig(maxConcurrent = 2, timeoutMillis = 1_000, maxAttempts = 3, initialBackoffMillis = 200)

    private fun ok(delay: Long = 0) = Reply.Ok(20.0, 5.0, delay)

    private fun network(message: String = "down") = Reply.Fail(FailureReason.Network(message))

    // ── Milestone 1 ─────────────────────────────────────────────────

    @Test
    fun `m1 fetchOne returns a success at once`() =
        runTest {
            val source = FakeWeatherSource(mapOf("Lisbon" to listOf(ok(delay = 100))))
            val result = aggregator.fetchOne(source, lisbon, config)
            assertEquals(WeatherResult.Success(Weather(lisbon, 20.0, 5.0)), result)
            assertEquals(1, source.calls("Lisbon"))
            assertEquals(100, currentTime)
        }

    @Test
    fun `m1 fetchOne retries retryable failures with growing waits`() =
        runTest {
            val source = FakeWeatherSource(mapOf("Oslo" to listOf(network(), network(), ok())))
            val result = aggregator.fetchOne(source, oslo, config)
            assertIs<WeatherResult.Success>(result)
            assertEquals(3, source.calls("Oslo"))
            assertEquals(200 + 400, currentTime, "Hint: wait 200 ms, then 200 * 2 = 400 ms")
        }

    @Test
    fun `m1 fetchOne times out each slow attempt`() =
        runTest {
            val source = FakeWeatherSource(mapOf("Tokyo" to listOf(ok(delay = 10_000))))
            val result = aggregator.fetchOne(source, tokyo, config)
            assertEquals(WeatherResult.Failure(tokyo, FailureReason.Timeout), result)
            assertEquals(3, source.calls("Tokyo"))
            assertEquals(1_000 + 200 + 1_000 + 400 + 1_000, currentTime, "Three 1 s timeouts plus two waits")
        }

    @Test
    fun `m1 fetchOne gives up at once on bad data and returns the last failure`() =
        runTest {
            val bad = FakeWeatherSource(mapOf("Rome" to listOf(Reply.Fail(FailureReason.BadData("garbled")))))
            assertEquals(
                WeatherResult.Failure(rome, FailureReason.BadData("garbled")),
                aggregator.fetchOne(bad, rome, config),
            )
            assertEquals(1, bad.calls("Rome"), "Hint: a non-retryable failure is not tried again")

            val down = FakeWeatherSource(mapOf("Oslo" to listOf(network("a"), network("b"), network("c"))))
            assertEquals(
                WeatherResult.Failure(oslo, FailureReason.Network("c")),
                aggregator.fetchOne(down, oslo, config),
            )
        }

    @Test
    fun `m1 fetchOne retries after a timeout and then succeeds`() =
        runTest {
            val source = FakeWeatherSource(mapOf("Tokyo" to listOf(ok(delay = 10_000), ok())))
            assertIs<WeatherResult.Success>(aggregator.fetchOne(source, tokyo, config))
            assertEquals(2, source.calls("Tokyo"), "Hint: a timeout is retryable")
            assertEquals(1_000 + 200, currentTime)
        }

    @Test
    fun `m1 fetchOne with maxAttempts 1 never waits`() =
        runTest {
            val source = FakeWeatherSource(mapOf("Oslo" to listOf(network("a"), ok())))
            val once = config.copy(maxAttempts = 1)
            assertEquals(
                WeatherResult.Failure(oslo, FailureReason.Network("a")),
                aggregator.fetchOne(source, oslo, once),
            )
            assertEquals(1, source.calls("Oslo"))
            assertEquals(0, currentTime, "Hint: no retry means no backoff wait")
        }

    @Test
    fun `m1 fetchOne multiplies the wait by a fractional backoffFactor`() =
        runTest {
            val source = FakeWeatherSource(mapOf("Oslo" to listOf(network(), network(), ok())))
            aggregator.fetchOne(source, oslo, config.copy(backoffFactor = 1.5))
            assertEquals(200 + 300, currentTime, "Hint: 200 ms, then 200 * 1.5 = 300 ms")
        }

    @Test
    fun `m1 fetchOne turns a throwing source into a retryable failure`() =
        runTest {
            val crash = Reply.Crash(IllegalStateException("bug in source"))
            val source = FakeWeatherSource(mapOf("Oslo" to listOf(crash, ok())))
            assertIs<WeatherResult.Success>(
                aggregator.fetchOne(source, oslo, config),
                "Hint: catch everything except CancellationException and treat it as Network",
            )
            assertEquals(2, source.calls("Oslo"))
        }

    // ── Milestone 2 ─────────────────────────────────────────────────

    @Test
    fun `m2 fetchAll keeps request order and respects the concurrency limit`() =
        runTest {
            val source =
                FakeWeatherSource(
                    mapOf(
                        "Lisbon" to listOf(ok(delay = 300)),
                        "Oslo" to listOf(ok(delay = 100)),
                        "Tokyo" to listOf(ok(delay = 100)),
                        "Rome" to listOf(ok(delay = 100)),
                    ),
                )
            val report = aggregator.fetchAll(source, listOf(lisbon, oslo, tokyo, rome), config)
            assertEquals(listOf("Lisbon", "Oslo", "Tokyo", "Rome"), report.results.map { it.city.name })
            assertEquals(2, source.peakConcurrency, "Hint: never more than maxConcurrent calls at once")
            assertEquals(300, currentTime, "Lisbon (300) runs while Oslo, Tokyo, Rome share the other slot")
        }

    @Test
    fun `m2 fetchAll returns partial results when some cities fail`() =
        runTest {
            val source =
                FakeWeatherSource(
                    mapOf(
                        "Lisbon" to listOf(ok()),
                        "Rome" to listOf(Reply.Fail(FailureReason.BadData("broken"))),
                    ),
                )
            val report = aggregator.fetchAll(source, listOf(lisbon, rome), config)
            assertEquals(listOf("Lisbon"), report.successes.map { it.city.name })
            assertEquals(listOf("Rome"), report.failures.map { it.city.name })
        }

    @Test
    fun `m2 fetchAll survives a source that throws for one city`() =
        runTest {
            val source =
                FakeWeatherSource(
                    mapOf(
                        "Lisbon" to listOf(ok(delay = 100)),
                        "Oslo" to listOf(Reply.Crash(IllegalArgumentException("unresolved host"))),
                    ),
                )
            val report = aggregator.fetchAll(source, listOf(lisbon, oslo), config.copy(maxAttempts = 1))
            assertEquals(listOf("Lisbon"), report.successes.map { it.city.name }, "Lisbon must not be cancelled")
            val failure = assertIs<WeatherResult.Failure>(report.results[1])
            assertIs<FailureReason.Network>(failure.reason)
        }

    @Test
    fun `m2 fetchAll of no cities is an empty report`() =
        runTest {
            val report = aggregator.fetchAll(FakeWeatherSource(emptyMap()), emptyList(), config)
            assertEquals(emptyList(), report.results)
        }

    @Test
    fun `m2 cancelling fetchAll cancels every fetch in flight`() =
        runTest {
            val source =
                FakeWeatherSource(mapOf("Lisbon" to listOf(ok(delay = 500)), "Oslo" to listOf(ok(delay = 500))))
            val job = launch { aggregator.fetchAll(source, listOf(lisbon, oslo), config) }
            advanceTimeBy(100)
            runCurrent()
            assertEquals(2, source.inFlightNow, "Both cities should be fetching by now")
            job.cancelAndJoin()
            assertEquals(0, source.inFlightNow, "Hint: use structured concurrency (coroutineScope)")
            assertEquals(100, currentTime)
        }

    // ── Milestone 3 ─────────────────────────────────────────────────

    @Test
    fun `m3 formatReport prints one line per city and a summary`() {
        val report =
            WeatherReport(
                listOf(
                    WeatherResult.Success(Weather(lisbon, 21.5, 12.0)),
                    WeatherResult.Success(Weather(tokyo, -3.25, 7.0)),
                    WeatherResult.Failure(oslo, FailureReason.Timeout),
                    WeatherResult.Failure(rome, FailureReason.Network("connection reset")),
                ),
            )
        assertEquals(
            listOf(
                "Lisbon     21.5 C  wind 12.0 km/h",
                "Tokyo      -3.3 C  wind 7.0 km/h",
                "Oslo       unavailable: timeout",
                "Rome       unavailable: network: connection reset",
                "2 of 4 cities updated",
            ),
            aggregator.formatReport(report),
        )
    }

    // ── Milestone 4 ─────────────────────────────────────────────────

    @Test
    fun `m4 refreshing fetches now and then after every period`() =
        runTest {
            val source = FakeWeatherSource(mapOf("Lisbon" to listOf(ok(delay = 100))))
            aggregator.refreshing(source, listOf(lisbon), config, periodMillis = 1_000).test {
                awaitItem()
                assertEquals(100, currentTime)
                awaitItem()
                assertEquals(100 + 1_000 + 100, currentTime, "Hint: wait the period AFTER each round")
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(2, source.calls("Lisbon"))
        }

    @Test
    fun `m4 refreshing rejects a bad period when called`() {
        val source = FakeWeatherSource(emptyMap())
        assertFailsWith<IllegalArgumentException> { aggregator.refreshing(source, listOf(lisbon), config, 0) }
    }

    @Test
    fun `m4 a dashboard StateFlow holds the latest report`() =
        runTest {
            val source = FakeWeatherSource(mapOf("Lisbon" to listOf(ok(delay = 100))))
            val board = aggregator.dashboard(backgroundScope, source, listOf(lisbon), config, periodMillis = 1_000)
            assertNull(board.value, "No report before the first round finishes")
            advanceTimeBy(150)
            runCurrent()
            assertEquals(listOf("Lisbon"), board.value?.successes?.map { it.city.name })
        }

    @Test
    fun `m4 a dashboard StateFlow updates on the next refresh`() =
        runTest {
            val replies = listOf(Reply.Ok(20.0, 5.0, delayMillis = 100), Reply.Ok(25.0, 5.0, delayMillis = 100))
            val source = FakeWeatherSource(mapOf("Lisbon" to replies))
            val board = aggregator.dashboard(backgroundScope, source, listOf(lisbon), config, periodMillis = 1_000)
            advanceTimeBy(150)
            runCurrent()
            assertEquals(listOf(20.0), board.value?.successes?.map { it.temperatureC })
            advanceTimeBy(1_100) // 1000 ms period + 100 ms second fetch
            runCurrent()
            assertEquals(listOf(25.0), board.value?.successes?.map { it.temperatureC }, "Second round expected")
        }
}
