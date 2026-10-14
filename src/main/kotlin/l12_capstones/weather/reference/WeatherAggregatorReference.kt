package l12_capstones.weather.reference

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull
import l12_capstones.weather.AggregatorConfig
import l12_capstones.weather.City
import l12_capstones.weather.FailureReason
import l12_capstones.weather.WeatherAggregator
import l12_capstones.weather.WeatherReport
import l12_capstones.weather.WeatherResult
import l12_capstones.weather.WeatherSource
import java.util.Locale

private const val NAME_WIDTH = 10

/** One finished answer to the weather capstone. Try starter/ first! */
object WeatherAggregatorReference : WeatherAggregator {
    override suspend fun fetchOne(source: WeatherSource, city: City, config: AggregatorConfig): WeatherResult {
        var backoff = config.initialBackoffMillis.toDouble()
        var last: WeatherResult.Failure? = null
        repeat(config.maxAttempts) { attempt ->
            if (attempt > 0) {
                delay(backoff.toLong())
                backoff *= config.backoffFactor
            }
            // withTimeoutOrNull turns "too slow" into null instead of an
            // exception, so a timeout is just another failure value here.
            val result = withTimeoutOrNull(config.timeoutMillis) { askSafely(source, city) }
                ?: WeatherResult.Failure(city, FailureReason.Timeout)
            when (result) {
                is WeatherResult.Success -> return result
                is WeatherResult.Failure -> {
                    if (!result.reason.retryable) return result
                    last = result
                }
            }
        }
        // maxAttempts >= 1 (checked by AggregatorConfig), so `last` is set here.
        return checkNotNull(last)
    }

    override suspend fun fetchAll(source: WeatherSource, cities: List<City>, config: AggregatorConfig): WeatherReport {
        // A Semaphore limits how many SUSPENDING calls are in flight;
        // limitedParallelism would only limit threads (l09 lesson 06).
        // Trade-off: a city keeps its permit while it waits between retries,
        // so a flaky city can hold a slot during its backoff. Simple and
        // fair enough here; releasing the permit around delay() would let
        // other cities go first at the cost of more code.
        val permits = Semaphore(config.maxConcurrent)
        // coroutineScope: fetchAll returns only when every child is done,
        // and if the caller is cancelled, every child is cancelled too.
        val results =
            coroutineScope {
                cities
                    .map { city -> async { permits.withPermit { fetchOne(source, city, config) } } }
                    .awaitAll() // awaitAll keeps the order of the list, not of completion
            }
        return WeatherReport(results)
    }

    override fun formatReport(report: WeatherReport): List<String> {
        val lines =
            report.results.map { result ->
                val name = result.city.name.padEnd(NAME_WIDTH)
                when (result) {
                    is WeatherResult.Success -> {
                        val weather = result.weather
                        "$name ${oneDecimal(weather.temperatureC)} C  wind ${oneDecimal(weather.windKmh)} km/h"
                    }
                    is WeatherResult.Failure -> "$name unavailable: ${describe(result.reason)}"
                }
            }
        return lines + "${report.successes.size} of ${report.results.size} cities updated"
    }

    override fun refreshing(
        source: WeatherSource,
        cities: List<City>,
        config: AggregatorConfig,
        periodMillis: Long,
    ): Flow<WeatherReport> {
        // Validate when called, not when collected (l10 lesson 07 rule).
        require(periodMillis > 0) { "periodMillis must be > 0, was $periodMillis" }
        return flow {
            while (true) {
                emit(fetchAll(source, cities, config))
                delay(periodMillis)
            }
        }
    }

    /**
     * The WeatherSource contract says "never throws", but a buggy source
     * might. Inside fetchAll that exception would cancel every OTHER city
     * (coroutineScope cancels siblings on failure), so treat it as an
     * ordinary, retryable failure instead. Cancellation (including the
     * TimeoutCancellationException of withTimeoutOrNull) must pass through.
     */
    private suspend fun askSafely(source: WeatherSource, city: City): WeatherResult =
        try {
            source.current(city)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            WeatherResult.Failure(city, FailureReason.Network("source failed: ${e::class.simpleName}"))
        }

    // Locale.ROOT: "%.1f" in a German or French locale would print "21,5".
    // "%.1f" rounds half away from zero (HALF_UP): -3.25 -> "-3.3", 25.25 -> "25.3".
    private fun oneDecimal(value: Double): String = String.format(Locale.ROOT, "%.1f", value)

    private fun describe(reason: FailureReason): String =
        when (reason) {
            FailureReason.Timeout -> "timeout"
            is FailureReason.Network -> "network: ${reason.message}"
            is FailureReason.BadData -> "bad data: ${reason.message}"
        }
}
