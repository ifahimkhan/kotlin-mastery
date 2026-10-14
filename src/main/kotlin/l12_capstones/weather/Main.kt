package l12_capstones.weather

import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import l12_capstones.weather.FakeWeatherSource.Reply
import l12_capstones.weather.reference.WeatherAggregatorReference

/**
 * =====================================================================
 *  CAPSTONE 12.B — Weather aggregator (demo)
 * =====================================================================
 *  GOAL   : Fetch the weather for several cities at once, with
 *           timeouts, retries and partial results, then keep it fresh.
 *  RUN    : Offline (fake data, always works):
 *             ./gradlew -q run -Plesson=l12_capstones.weather.MainKt
 *           Live, from the real Open-Meteo API (needs internet):
 *             ./gradlew -q run -Plesson=l12_capstones.weather.MainKt --args="--live"
 *  NOTE   : Main uses reference/WeatherAggregatorReference. Switch it to
 *           starter.WeatherAggregatorStarter once your tests pass.
 *  NEXT   : ../scheduler/README.md
 * =====================================================================
 */

private const val LIVE_FLAG = "--live"
private const val REFRESH_MILLIS = 1_000L
private const val ROUNDS = 2

// A real network is slower than our fake: give each live call more time.
private const val LIVE_TIMEOUT_MILLIS = 5_000L

private val CITIES =
    listOf(
        City("Lisbon", 38.72, -9.14),
        City("Oslo", 59.91, 10.75),
        City("Tokyo", 35.68, 139.69),
        City("Nairobi", -1.29, 36.82),
    )

/** Offline data: Oslo is flaky (fails once), Tokyo is too slow. */
private fun fakeSource() =
    FakeWeatherSource(
        mapOf(
            "Lisbon" to listOf(Reply.Ok(21.5, 12.0, delayMillis = 100)),
            "Oslo" to listOf(Reply.Fail(FailureReason.Network("connection reset")), Reply.Ok(4.0, 20.5)),
            "Tokyo" to listOf(Reply.Ok(18.0, 7.0, delayMillis = 5_000)),
            "Nairobi" to listOf(Reply.Ok(25.25, 9.0, delayMillis = 50)),
        ),
    )

fun main(args: Array<String>) =
    runBlocking {
        val aggregator: WeatherAggregator = WeatherAggregatorReference
        val config = AggregatorConfig(maxConcurrent = 2, timeoutMillis = 1_000, maxAttempts = 2)

        if (LIVE_FLAG in args) {
            println("Asking Open-Meteo (live)...")
            // use { }: the HttpClient is closed even if something throws.
            val liveConfig = config.copy(timeoutMillis = LIVE_TIMEOUT_MILLIS)
            OpenMeteoSource().use { source ->
                aggregator.formatReport(aggregator.fetchAll(source, CITIES, liveConfig)).forEach(::println)
            }
        } else {
            println("Offline demo with fake data (pass --live for real weather).")
            aggregator.refreshing(fakeSource(), CITIES, config, REFRESH_MILLIS)
                .take(ROUNDS)
                .collect { report ->
                    println("--- update ---")
                    aggregator.formatReport(report).forEach(::println)
                }
        }
    }
