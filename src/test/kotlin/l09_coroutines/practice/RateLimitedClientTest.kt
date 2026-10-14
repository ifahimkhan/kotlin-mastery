package l09_coroutines.practice

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import l09_coroutines.practice.ratelimit.Api
import l09_coroutines.practice.ratelimit.ApiError
import l09_coroutines.practice.ratelimit.ApiResult
import l09_coroutines.practice.ratelimit.RateLimitedClient
import l09_coroutines.practice.ratelimit.RateLimiter
import l09_coroutines.practice.ratelimit.Response
import l09_coroutines.practice.ratelimit.RetryPolicy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// currentTime, advanceTimeBy, runCurrent and UnconfinedTestDispatcher are
// still marked experimental in kotlinx-coroutines-test, so we opt in.
@OptIn(ExperimentalCoroutinesApi::class)
class RateLimitedClientTest {
    private val policy = RetryPolicy(maxAttempts = 3, initialBackoffMs = BACKOFF, maxBackoffMs = MAX_BACKOFF)

    /** Replies with [responses] in order, then repeats the last one. Counts calls. */
    private class ScriptedApi(private val responses: List<Response>) : Api {
        var calls = 0
            private set

        override suspend fun get(path: String): Response = responses[minOf(calls++, responses.lastIndex)]
    }

    @Test
    fun `backoff doubles and is capped`() {
        val p = RetryPolicy(maxAttempts = 5, initialBackoffMs = 100, maxBackoffMs = 500)
        assertEquals(listOf(100L, 200L, 400L, 500L, 500L), (1..5).map { p.backoffBefore(it) })
    }

    @Test
    fun `retries busy errors with backoff, then succeeds`() = runTest {
        val api = ScriptedApi(listOf(BUSY, BUSY, Response.Ok("data")))
        val client = RateLimitedClient(api, RateLimiter(backgroundScope, MANY, WINDOW), policy, TIMEOUT)

        assertEquals(ApiResult.Success("data", attempts = 3), client.get("/x"))
        assertEquals(BACKOFF + 2 * BACKOFF, testScheduler.currentTime, "waited 100 then 200 ms (virtual)")
    }

    @Test
    fun `non-retryable error is returned at once`() = runTest {
        val notFound = Response.Error(ApiError.NotFound("/x"))
        val api = ScriptedApi(listOf(notFound))
        val client = RateLimitedClient(api, RateLimiter(backgroundScope, MANY, WINDOW), policy, TIMEOUT)

        assertEquals(ApiResult.Failure(ApiError.NotFound("/x"), attempts = 1), client.get("/x"))
        assertEquals(1, api.calls)
        assertEquals(0L, testScheduler.currentTime)
    }

    @Test
    fun `gives up after maxAttempts`() = runTest {
        val api = ScriptedApi(listOf(BUSY))
        val client = RateLimitedClient(api, RateLimiter(backgroundScope, MANY, WINDOW), policy, TIMEOUT)

        assertEquals(ApiResult.Failure(ApiError.ServerBusy, attempts = 3), client.get("/x"))
        assertEquals(3, api.calls)
    }

    @Test
    fun `slow calls time out and count as retryable`() = runTest {
        val slowApi = Api { delay(10 * TIMEOUT); Response.Ok("too late") }
        val client = RateLimitedClient(slowApi, RateLimiter(backgroundScope, MANY, WINDOW), policy, TIMEOUT)

        assertEquals(ApiResult.Failure(ApiError.TimedOut, attempts = 3), client.get("/x"))
        assertEquals(3 * TIMEOUT + BACKOFF + 2 * BACKOFF, testScheduler.currentTime)
    }

    @Test
    fun `limiter allows only permits calls per window`() = runTest {
        val limiter = RateLimiter(backgroundScope, permits = 2, windowMs = WINDOW)
        val startTimes = List(5) {
            limiter.acquire()
            testScheduler.currentTime
        }
        assertEquals(listOf(0L, 0L, WINDOW, WINDOW, 2 * WINDOW), startTimes)
    }

    @Test
    fun `invalid configuration fails fast`() {
        assertFailsWith<IllegalArgumentException> {
            RetryPolicy(maxAttempts = 0, initialBackoffMs = 1, maxBackoffMs = 1)
        }
        assertFailsWith<IllegalArgumentException> {
            RetryPolicy(maxAttempts = 1, initialBackoffMs = 5, maxBackoffMs = 1)
        }
    }

    private companion object {
        const val BACKOFF = 100L
        const val MAX_BACKOFF = 1_000L
        const val TIMEOUT = 500L
        const val WINDOW = 1_000L
        const val MANY = 100
        val BUSY = Response.Error(ApiError.ServerBusy)
    }
}
