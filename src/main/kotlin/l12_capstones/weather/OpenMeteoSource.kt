package l12_capstones.weather

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentLength
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.SocketTimeoutException

private const val FORECAST_URL = "https://api.open-meteo.com/v1/forecast"
private const val CURRENT_FIELDS = "temperature_2m,wind_speed_10m"
private const val REQUEST_TIMEOUT_MILLIS = 5_000L
private const val CONNECT_TIMEOUT_MILLIS = 3_000L

// The real answer is about 1 KB. Refuse anything absurdly large instead of
// reading it all into memory.
private const val MAX_BODY_BYTES = 1_000_000L

private val HTTP_CLIENT_ERRORS = 400..499
private const val HTTP_REQUEST_TIMEOUT = 408
private const val HTTP_TOO_MANY_REQUESTS = 429

/*
 * The JSON we care about looks like this (the real answer has many more
 * fields; ignoreUnknownKeys lets us skip them):
 *   { "current": { "temperature_2m": 21.5, "wind_speed_10m": 12.0, ... }, ... }
 * DTOs ("data transfer objects") mirror the JSON exactly; @SerialName maps
 * the API's snake_case names to Kotlin's camelCase.
 */
@Serializable
internal data class OpenMeteoResponse(
    val current: OpenMeteoCurrent,
)

@Serializable
internal data class OpenMeteoCurrent(
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("wind_speed_10m") val windSpeed: Double,
)

private val openMeteoJson = Json { ignoreUnknownKeys = true }

/**
 * Pure parsing: JSON text -> WeatherResult. Kept separate from the HTTP
 * call so it can be tested with a fixed string, no network needed.
 */
fun parseOpenMeteo(text: String, city: City): WeatherResult =
    try {
        val dto = openMeteoJson.decodeFromString(OpenMeteoResponse.serializer(), text)
        WeatherResult.Success(Weather(city, dto.current.temperature, dto.current.windSpeed))
    } catch (e: SerializationException) {
        WeatherResult.Failure(city, FailureReason.BadData(e.message?.lineSequence()?.firstOrNull() ?: "unreadable"))
    } catch (e: IllegalArgumentException) {
        WeatherResult.Failure(city, FailureReason.BadData(e.message?.lineSequence()?.firstOrNull() ?: "unreadable"))
    }

/**
 * Pure mapping: an HTTP error status -> why it failed.
 * 4xx means "your request is wrong" (bad coordinates, wrong URL): asking
 * again won't help, so it is BadData (not retryable). The exceptions are
 * 408 (request timeout) and 429 (too many requests), which DO get better
 * later, like every 5xx server error: those are Network (retryable).
 */
fun failureForStatus(status: Int): FailureReason {
    val permanent = status in HTTP_CLIENT_ERRORS && status != HTTP_REQUEST_TIMEOUT && status != HTTP_TOO_MANY_REQUESTS
    return if (permanent) FailureReason.BadData("HTTP $status") else FailureReason.Network("HTTP $status")
}

/**
 * The real WeatherSource: the free Open-Meteo API (no API key needed).
 *
 * Who closes the HttpClient? Whoever created it:
 * - `OpenMeteoSource()` creates its own client, so [close] closes it.
 *   Use it with `use { }` (AutoCloseable).
 * - `OpenMeteoSource(client)` borrows YOUR client (e.g. shared by several
 *   sources), so [close] leaves it open: closing it is your job.
 */
class OpenMeteoSource private constructor(
    private val client: HttpClient,
    private val ownsClient: Boolean,
) : WeatherSource, AutoCloseable {
    /** Creates and owns a client with sensible timeouts. */
    constructor() : this(defaultClient(), ownsClient = true)

    /** Borrows [client]; the caller stays responsible for closing it. */
    constructor(client: HttpClient) : this(client, ownsClient = false)

    override suspend fun current(city: City): WeatherResult =
        try {
            val response =
                client.get(FORECAST_URL) {
                    parameter("latitude", city.latitude)
                    parameter("longitude", city.longitude)
                    parameter("current", CURRENT_FIELDS)
                }
            val size = response.contentLength()
            if (size != null && size > MAX_BODY_BYTES) {
                WeatherResult.Failure(city, FailureReason.BadData("answer too large: $size bytes"))
            } else {
                parseOpenMeteo(response.bodyAsText(), city)
            }
        } catch (e: CancellationException) {
            // Never turn cancellation into a failure value (l09 lesson 11).
            throw e
        } catch (e: HttpRequestTimeoutException) {
            WeatherResult.Failure(city, FailureReason.Timeout)
        } catch (e: ConnectTimeoutException) {
            WeatherResult.Failure(city, FailureReason.Timeout)
        } catch (e: SocketTimeoutException) {
            WeatherResult.Failure(city, FailureReason.Timeout)
        } catch (e: ResponseException) {
            // expectSuccess = true makes 4xx/5xx answers throw this.
            WeatherResult.Failure(city, failureForStatus(e.response.status.value))
        } catch (e: IOException) {
            WeatherResult.Failure(city, FailureReason.Network(e.message ?: e::class.simpleName.orEmpty()))
        } catch (e: Exception) {
            // The last safety net. The contract says "never throws", and
            // HTTP engines can throw surprising unchecked exceptions (e.g. an
            // unresolvable host on some engines). Report them, don't crash.
            WeatherResult.Failure(city, FailureReason.Network("${e::class.simpleName}: ${e.message.orEmpty()}"))
        }

    override fun close() {
        if (ownsClient) client.close()
    }

    private companion object {
        fun defaultClient(): HttpClient =
            HttpClient(CIO) {
                expectSuccess = true
                // Never let a request hang forever (a client without timeouts can).
                install(HttpTimeout) {
                    requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
                    connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
                }
            }
    }
}
