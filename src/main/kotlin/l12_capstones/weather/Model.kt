package l12_capstones.weather

/** A place to ask about. Coordinates are what the Open-Meteo API needs. */
data class City(
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

/** One successful reading. */
data class Weather(
    val city: City,
    val temperatureC: Double,
    val windKmh: Double,
)

/**
 * Why a city could not be read. [retryable] separates "try again later"
 * problems (a slow or flaky network) from "trying again will not help"
 * problems (the server sent nonsense).
 */
sealed interface FailureReason {
    val retryable: Boolean

    data object Timeout : FailureReason {
        override val retryable = true
    }

    data class Network(val message: String) : FailureReason {
        override val retryable = true
    }

    data class BadData(val message: String) : FailureReason {
        override val retryable = false
    }
}

/** A failed city is a normal outcome (a value), not an exception. */
sealed interface WeatherResult {
    val city: City

    data class Success(val weather: Weather) : WeatherResult {
        override val city: City get() = weather.city
    }

    data class Failure(override val city: City, val reason: FailureReason) : WeatherResult
}

/** One round of readings, in the order the cities were requested. */
data class WeatherReport(
    val results: List<WeatherResult>,
) {
    val successes: List<Weather> get() = results.filterIsInstance<WeatherResult.Success>().map { it.weather }
    val failures: List<WeatherResult.Failure> get() = results.filterIsInstance<WeatherResult.Failure>()
}

/**
 * Knobs for fetching. Validated on creation (require): a config that
 * makes no sense is a programming mistake, so it fails fast.
 */
data class AggregatorConfig(
    val maxConcurrent: Int = 3,
    val timeoutMillis: Long = 2_000,
    val maxAttempts: Int = 3,
    val initialBackoffMillis: Long = 200,
    val backoffFactor: Double = 2.0,
) {
    init {
        require(maxConcurrent >= 1) { "maxConcurrent must be >= 1, was $maxConcurrent" }
        require(timeoutMillis > 0) { "timeoutMillis must be > 0, was $timeoutMillis" }
        require(maxAttempts >= 1) { "maxAttempts must be >= 1, was $maxAttempts" }
        require(initialBackoffMillis >= 0) { "initialBackoffMillis must be >= 0, was $initialBackoffMillis" }
        require(backoffFactor >= 1.0) { "backoffFactor must be >= 1.0, was $backoffFactor" }
    }
}

/**
 * Anything that can report the current weather for a city: the real
 * Open-Meteo API, or a fake for tests and offline demos.
 *
 * Contract: never throws for network or data problems - those come back
 * as WeatherResult.Failure. (It may still be cancelled, of course.)
 */
interface WeatherSource {
    suspend fun current(city: City): WeatherResult
}
