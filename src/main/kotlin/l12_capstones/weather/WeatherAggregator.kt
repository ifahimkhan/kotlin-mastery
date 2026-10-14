package l12_capstones.weather

import kotlinx.coroutines.flow.Flow

/**
 * The contract of the weather capstone: what YOU build in
 * starter/WeatherAggregatorStarter.kt. reference/ holds one finished
 * answer; the same acceptance tests (on virtual time) check both.
 *
 * The aggregator never talks to the network itself. It is handed a
 * WeatherSource, so tests can pass a fake that is slow, flaky or broken
 * on purpose.
 */
interface WeatherAggregator {
    // ── Milestone 1: one city, made robust (level 09 lessons 10, 11) ─

    /**
     * Reads one city. Each attempt is limited to config.timeoutMillis
     * (a timed-out attempt counts as FailureReason.Timeout).
     * A retryable failure is tried again, up to config.maxAttempts
     * attempts in total, waiting initialBackoffMillis before the 2nd
     * attempt and multiplying the wait by backoffFactor each time.
     * A non-retryable failure is returned at once.
     * Returns the first Success, or the LAST failure.
     *
     * Defensive rule: if the source breaks its contract and THROWS
     * (anything except cancellation), treat that attempt as a retryable
     * FailureReason.Network failure. Never let it escape: in fetchAll one
     * escaping exception would cancel every other city. Always rethrow
     * CancellationException (l09 lesson 11).
     */
    suspend fun fetchOne(source: WeatherSource, city: City, config: AggregatorConfig): WeatherResult

    // ── Milestone 2: many cities at once (level 09 lessons 5, 8) ─────

    /**
     * Reads every city concurrently with fetchOne, never more than
     * config.maxConcurrent at the same time. Results keep the order of
     * [cities]; one failing city never stops the others. An empty list
     * gives an empty report. Cancelling the caller cancels every fetch.
     */
    suspend fun fetchAll(source: WeatherSource, cities: List<City>, config: AggregatorConfig): WeatherReport

    // ── Milestone 3: a readable report (levels 01, 06) ───────────────

    /**
     * One line per result, then a summary line:
     *   "Lisbon     21.5 C  wind 12.0 km/h"     (name padded to 10, one decimal each)
     *   "Oslo       unavailable: timeout"        (timeout / network: <msg> / bad data: <msg>)
     *   "2 of 3 cities updated"
     * Numbers always use a dot as the decimal separator, and round half
     * away from zero like String.format("%.1f"): -3.25 -> "-3.3",
     * 25.25 -> "25.3". (roundToInt() / 10.0 would give -3.2: not the same!)
     */
    fun formatReport(report: WeatherReport): List<String>

    // ── Milestone 4: keep it fresh (level 10) ─────────────────────────

    /**
     * A cold Flow that fetches all cities immediately, then again every
     * [periodMillis] after each round finishes, forever (until the
     * collector stops). require: periodMillis > 0, checked when called.
     */
    fun refreshing(
        source: WeatherSource,
        cities: List<City>,
        config: AggregatorConfig,
        periodMillis: Long,
    ): Flow<WeatherReport>
}
