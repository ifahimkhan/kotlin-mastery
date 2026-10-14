package l09_coroutines.practice.ratelimit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

/**
 * =====================================================================
 *  PRACTICE 09.C — A rate-limited API client with retries
 * =====================================================================
 *  GOAL   : Call an unreliable API politely: at most N calls per time
 *           window, a timeout per call, and retries with exponential
 *           backoff for errors that are worth retrying.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.practice.ratelimit.RateLimitedClientKt
 *  OUTPUT :
 *           /prices -> EUR 42 after 3 attempt(s)
 *           /missing -> failed with NotFound(path=/missing) after 1 attempt(s)
 *           /slow -> failed with TimedOut after 3 attempt(s)
 *  DESIGN : - Failures are VALUES (sealed ApiResult / ApiError), not
 *             exceptions: a busy server is a normal business outcome.
 *             Bad configuration (zero attempts...) is a caller bug and
 *             fails fast with require.
 *           - The limiter is a channel of "tokens": acquire() takes one
 *             and a coroutine gives it back after the window. It needs a
 *             scope for those coroutines, passed in by the caller.
 *           - The Api is an interface, so tests plug in a fake; all
 *             waiting is delay(), so tests run on virtual time.
 *  EXTEND : Add random "jitter" to the backoff; read a Retry-After hint
 *           from the error; share one limiter between two clients.
 * =====================================================================
 */

/** What can go wrong with a call. Only some errors are worth a retry. */
sealed interface ApiError {
    val retryable: Boolean

    data object ServerBusy : ApiError {
        override val retryable = true
    }

    data object TimedOut : ApiError {
        override val retryable = true
    }

    data class NotFound(val path: String) : ApiError {
        override val retryable = false
    }
}

/** One raw answer from the server. */
sealed interface Response {
    data class Ok(val body: String) : Response
    data class Error(val error: ApiError) : Response
}

/** The server we talk to. A fake implements it in tests and in main. */
fun interface Api {
    suspend fun get(path: String): Response
}

/** The client's final answer, including how many attempts it took. */
sealed interface ApiResult {
    val attempts: Int

    data class Success(val body: String, override val attempts: Int) : ApiResult
    data class Failure(val error: ApiError, override val attempts: Int) : ApiResult
}

data class RetryPolicy(
    val maxAttempts: Int,
    val initialBackoffMs: Long,
    val maxBackoffMs: Long,
    val multiplier: Int = DEFAULT_MULTIPLIER,
) {
    init {
        require(maxAttempts > 0) { "maxAttempts must be positive, was $maxAttempts" }
        require(initialBackoffMs >= 0) { "initialBackoffMs must not be negative, was $initialBackoffMs" }
        require(maxBackoffMs >= initialBackoffMs) { "maxBackoffMs must be >= initialBackoffMs" }
        require(multiplier >= 1) { "multiplier must be at least 1, was $multiplier" }
    }

    /** Wait before retry number [retry] (1 = first retry): initial * multiplier^(retry-1), capped. */
    fun backoffBefore(retry: Int): Long {
        require(retry >= 1) { "retry starts at 1, was $retry" }
        var wait = initialBackoffMs
        repeat(retry - 1) { wait = minOf(wait * multiplier, maxBackoffMs) }
        return minOf(wait, maxBackoffMs)
    }

    private companion object {
        const val DEFAULT_MULTIPLIER = 2
    }
}

/**
 * Allows at most [permits] acquire() calls in any [windowMs] window.
 * Tokens are returned by coroutines started in [scope], so cancel that
 * scope when you are done (tests use runTest's backgroundScope).
 */
class RateLimiter(private val scope: CoroutineScope, permits: Int, private val windowMs: Long) {
    private val tokens = Channel<Unit>(capacity = permits)

    init {
        require(permits > 0) { "permits must be positive, was $permits" }
        require(windowMs > 0) { "windowMs must be positive, was $windowMs" }
        repeat(permits) { tokens.trySend(Unit) } // start with a full bucket
    }

    suspend fun acquire() {
        // NOTE: if the caller is cancelled in the tiny gap after receive()
        // took a token but before the launch below starts, that token is
        // lost for good. Rare and harmless for a demo; a production limiter
        // would return tokens in a `finally` or use kotlinx Semaphore.
        tokens.receive() // suspends while no token is left
        scope.launch {
            delay(windowMs)
            tokens.send(Unit) // give the token back after the window
        }
    }
}

class RateLimitedClient(
    private val api: Api,
    private val limiter: RateLimiter,
    private val policy: RetryPolicy,
    private val callTimeoutMs: Long,
) {
    init {
        require(callTimeoutMs > 0) { "callTimeoutMs must be positive, was $callTimeoutMs" }
    }

    suspend fun get(path: String): ApiResult {
        var attempt = 0
        while (true) {
            attempt++
            limiter.acquire()
            val response = withTimeoutOrNull(callTimeoutMs) { api.get(path) }
                ?: Response.Error(ApiError.TimedOut)
            when (response) {
                is Response.Ok -> return ApiResult.Success(response.body, attempt)
                is Response.Error -> {
                    val giveUp = !response.error.retryable || attempt >= policy.maxAttempts
                    if (giveUp) return ApiResult.Failure(response.error, attempt)
                    delay(policy.backoffBefore(attempt))
                }
            }
        }
    }
}

// ── Demo ────────────────────────────────────────────────────────────

private const val DEMO_PERMITS = 3
private const val DEMO_WINDOW_MS = 200L
private const val DEMO_TIMEOUT_MS = 100L
private const val DEMO_SLOW_MS = 1_000L
private const val DEMO_BUSY_TIMES = 2
private const val DEMO_MAX_ATTEMPTS = 3
private const val DEMO_BACKOFF_MS = 50L
private const val DEMO_MAX_BACKOFF_MS = 400L

/** A fake server: /prices is busy twice, /missing doesn't exist, /slow never answers in time. */
class FakeServer : Api {
    private var priceCalls = 0

    override suspend fun get(path: String): Response = when (path) {
        "/prices" -> if (++priceCalls <= DEMO_BUSY_TIMES) Response.Error(ApiError.ServerBusy) else Response.Ok("EUR 42")
        "/slow" -> {
            delay(DEMO_SLOW_MS)
            Response.Ok("too late")
        }
        else -> Response.Error(ApiError.NotFound(path))
    }
}

fun describe(path: String, result: ApiResult): String = when (result) {
    is ApiResult.Success -> "$path -> ${result.body} after ${result.attempts} attempt(s)"
    is ApiResult.Failure -> "$path -> failed with ${result.error} after ${result.attempts} attempt(s)"
}

fun main() = runBlocking {
    coroutineScope {
        val client = RateLimitedClient(
            api = FakeServer(),
            limiter = RateLimiter(this, DEMO_PERMITS, DEMO_WINDOW_MS),
            policy = RetryPolicy(DEMO_MAX_ATTEMPTS, DEMO_BACKOFF_MS, DEMO_MAX_BACKOFF_MS),
            callTimeoutMs = DEMO_TIMEOUT_MS,
        )
        for (path in listOf("/prices", "/missing", "/slow")) {
            println(describe(path, client.get(path)))
        }
    } // waits for the last tokens to be returned, then ends
}
