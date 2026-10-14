package l09_coroutines.lesson16_testing

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * =====================================================================
 *  LESSON 09.16 — Testing coroutines
 * =====================================================================
 *  GOAL   : Write coroutine code that is easy to test (inject waits and
 *           dispatchers), then test it with runTest and VIRTUAL time:
 *           a one-hour delay finishes in milliseconds.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.lesson16_testing.TestingCoroutinesKt
 *           Tests: src/test/kotlin/l09_coroutines/Lesson16ExampleTest.kt
 *  OUTPUT :
 *           price below 90: 85
 *           calls made: 4
 *           report has 2 lines
 *  NEXT   : practice/downloader/Downloader.kt
 * =====================================================================
 */

private const val DEMO_POLL_MS = 20L
private const val DEMO_LIMIT = 90
private const val DEMO_MAX_POLLS = 10
private const val START_PRICE = 100
private const val PRICE_DROP = 5

/**
 * Polls a price until it drops below [limit]. Testable on purpose:
 * - the price source is a parameter (a fake in tests),
 * - the wait between polls is a parameter, not a hidden constant.
 * Returns the price, or null after [maxPolls] tries.
 */
class PriceWatcher(
    private val fetchPrice: suspend () -> Int,
    private val pollEveryMs: Long,
) {
    init {
        require(pollEveryMs > 0) { "pollEveryMs must be positive, was $pollEveryMs" }
    }

    suspend fun waitUntilBelow(limit: Int, maxPolls: Int): Int? {
        require(maxPolls > 0) { "maxPolls must be positive, was $maxPolls" }
        repeat(maxPolls) { attempt ->
            val price = fetchPrice()
            if (price < limit) return price
            if (attempt < maxPolls - 1) delay(pollEveryMs)
        }
        return null
    }
}

/**
 * Blocking work belongs on Dispatchers.IO, but a test wants control over
 * it. So the dispatcher is a constructor parameter with a sensible default.
 */
class ReportWriter(private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) {
    suspend fun write(lines: List<String>): String = withContext(ioDispatcher) {
        lines.joinToString(separator = "\n") // stands in for writing a file
    }
}

fun main() = runBlocking {
    // ── 1. CONCEPT: design for testing ───────────────────────────────
    // A fake price source: falls by PRICE_DROP on every call.
    var price = START_PRICE
    var calls = 0
    val watcher = PriceWatcher(
        fetchPrice = { calls++; price.also { price -= PRICE_DROP } },
        pollEveryMs = DEMO_POLL_MS,
    )
    println("price below $DEMO_LIMIT: ${watcher.waitUntilBelow(DEMO_LIMIT, DEMO_MAX_POLLS)}")
    println("calls made: $calls")

    val report = ReportWriter().write(listOf("line 1", "line 2"))
    println("report has ${report.lines().size} lines")

    // ── 2. CONCEPT: runTest and virtual time ─────────────────────────
    // In a real app the watcher might poll every MINUTE. Waiting that
    // long in a test is unacceptable. runTest (kotlinx-coroutines-test)
    // replaces the clock with a VIRTUAL one: delay() just moves the
    // virtual clock forward, instantly.
    //
    //   @Test
    //   fun `polls once a minute`() = runTest {
    //       val watcher = PriceWatcher(fetchPrice = { 120 }, pollEveryMs = 60_000)
    //       val result = watcher.waitUntilBelow(limit = 100, maxPolls = 3)
    //       assertNull(result)
    //       assertEquals(120_000, testScheduler.currentTime) // 2 waits, 0 ms real time
    //   }
    //
    // See it run: src/test/kotlin/l09_coroutines/Lesson16ExampleTest.kt

    // ── 3. CONCEPT: controlling time by hand ─────────────────────────
    // Inside runTest:
    //   launch { ... }         starts work, but it waits for the scheduler
    //   advanceTimeBy(1_000)   runs tasks due BEFORE currentTime + 1000; a
    //                          task due exactly at that moment needs a
    //                          following runCurrent()
    //   runCurrent()           runs what is due right now
    //   advanceUntilIdle()     runs until nothing is left to do
    //   testScheduler.currentTime  the virtual clock in milliseconds
    //   backgroundScope.launch { } for endless work; cancelled at the end
    // Some of these are still marked experimental, so test classes add
    // @OptIn(ExperimentalCoroutinesApi::class).

    // ── 4. CONCEPT: test dispatchers ─────────────────────────────────
    // StandardTestDispatcher(testScheduler): new coroutines wait in a
    //   queue until the test advances time. Predictable; the default.
    // UnconfinedTestDispatcher(testScheduler): new coroutines start at
    //   once. Handy for collectors that must start before you emit.
    // Pass one of them where production code takes a dispatcher:
    //   ReportWriter(StandardTestDispatcher(testScheduler))

    // ── 5. PITFALL: hard-coded Dispatchers.IO ────────────────────────
    // withContext(Dispatchers.IO) inside a class escapes the test
    // scheduler: delays there use the REAL clock and the test gets slow
    // or flaky. Inject the dispatcher, as ReportWriter does.

    // ── 6. WHY virtual time? ─────────────────────────────────────────
    // Tests stay fast (a day of retries in milliseconds) and
    // deterministic (no "sleep and hope" timing).

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) In Lesson16ExampleTest, change pollEveryMs to one hour. Does
    //    the test get slower?
    // b) Write a test that the watcher returns the FIRST price below
    //    the limit and makes no extra calls.
}
