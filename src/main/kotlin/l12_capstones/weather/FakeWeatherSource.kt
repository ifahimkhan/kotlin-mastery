package l12_capstones.weather

import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * A scripted WeatherSource for tests and offline demos.
 *
 * Each city has a list of replies, one per call: the 1st call gets the
 * 1st reply, the 2nd call the 2nd, and the last reply repeats forever.
 * So `listOf(Fail(Network("down")), Ok(20.0, 5.0))` means "fails once,
 * then works". It also records calls and the peak number of calls in
 * flight, so tests can check retries and concurrency limits.
 */
class FakeWeatherSource(
    private val script: Map<String, List<Reply>>,
) : WeatherSource {
    sealed interface Reply {
        val delayMillis: Long

        data class Ok(val temperatureC: Double, val windKmh: Double, override val delayMillis: Long = 0) : Reply

        data class Fail(val reason: FailureReason, override val delayMillis: Long = 0) : Reply

        /** Breaks the "never throws" contract on purpose, to test defensive code. */
        data class Crash(val error: Exception, override val delayMillis: Long = 0) : Reply
    }

    private val callCounts = ConcurrentHashMap<String, AtomicInteger>()
    private val inFlight = AtomicInteger()
    private val peak = AtomicInteger()

    /** How many times [cityName] was asked for. */
    fun calls(cityName: String): Int = callCounts[cityName]?.get() ?: 0

    /** The most calls that were running at the same moment. */
    val peakConcurrency: Int get() = peak.get()

    /** How many calls are running right now (0 once everything finished or was cancelled). */
    val inFlightNow: Int get() = inFlight.get()

    override suspend fun current(city: City): WeatherResult {
        val replies = script[city.name]
            ?: return WeatherResult.Failure(city, FailureReason.BadData("unknown city ${city.name}"))
        val index = callCounts.getOrPut(city.name) { AtomicInteger() }.getAndIncrement()
        val reply = replies[index.coerceAtMost(replies.lastIndex)]

        peak.accumulateAndGet(inFlight.incrementAndGet(), ::maxOf)
        try {
            delay(reply.delayMillis)
        } finally {
            // finally: also runs when a timeout cancels us mid-delay.
            inFlight.decrementAndGet()
        }
        return when (reply) {
            is Reply.Ok -> WeatherResult.Success(Weather(city, reply.temperatureC, reply.windKmh))
            is Reply.Fail -> WeatherResult.Failure(city, reply.reason)
            is Reply.Crash -> throw reply.error
        }
    }
}
