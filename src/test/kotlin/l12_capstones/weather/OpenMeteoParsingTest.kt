package l12_capstones.weather

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.isActive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** The JSON parsing of the real source, checked offline with fixed text. */
class OpenMeteoParsingTest {
    private val lisbon = City("Lisbon", 38.72, -9.14)

    // A trimmed copy of a real Open-Meteo answer; the extra fields must be ignored.
    private val sample =
        """
        {
          "latitude": 38.72, "longitude": -9.14, "timezone": "GMT",
          "current_units": { "temperature_2m": "°C", "wind_speed_10m": "km/h" },
          "current": { "time": "2026-10-14T12:00", "interval": 900,
                       "temperature_2m": 21.5, "wind_speed_10m": 12.0 }
        }
        """.trimIndent()

    @Test
    fun `a real answer becomes a Success and unknown fields are ignored`() {
        assertEquals(WeatherResult.Success(Weather(lisbon, 21.5, 12.0)), parseOpenMeteo(sample, lisbon))
    }

    @Test
    fun `missing fields or broken JSON become BadData, never an exception`() {
        val missing = parseOpenMeteo("""{ "current": { "temperature_2m": 21.5 } }""", lisbon)
        assertIs<FailureReason.BadData>(assertIs<WeatherResult.Failure>(missing).reason)
        val broken = parseOpenMeteo("<html>Service Unavailable</html>", lisbon)
        assertIs<FailureReason.BadData>(assertIs<WeatherResult.Failure>(broken).reason)
    }

    @Test
    fun `a bad config fails fast`() {
        listOf(
            { AggregatorConfig(maxConcurrent = 0) },
            { AggregatorConfig(timeoutMillis = 0) },
            { AggregatorConfig(maxAttempts = 0) },
            { AggregatorConfig(backoffFactor = 0.5) },
        ).forEach { make -> kotlin.test.assertFailsWith<IllegalArgumentException> { make() } }
    }

    @Test
    fun `HTTP errors map to retryable or permanent failures`() {
        assertEquals(FailureReason.BadData("HTTP 400"), failureForStatus(400))
        assertEquals(FailureReason.BadData("HTTP 404"), failureForStatus(404))
        assertEquals(FailureReason.Network("HTTP 408"), failureForStatus(408), "408 request timeout: retry")
        assertEquals(FailureReason.Network("HTTP 429"), failureForStatus(429), "429 too many requests: retry")
        assertEquals(FailureReason.Network("HTTP 503"), failureForStatus(503), "5xx: the server may recover")
    }

    @Test
    fun `closing a source does not close a borrowed client`() {
        val client = HttpClient(CIO)
        try {
            OpenMeteoSource(client).close()
            assertTrue(client.isActive, "The caller owns a client it passed in")
        } finally {
            client.close()
        }
    }
}
