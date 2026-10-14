package l12_capstones.weather.starter

import kotlinx.coroutines.flow.Flow
import l12_capstones.weather.AggregatorConfig
import l12_capstones.weather.City
import l12_capstones.weather.WeatherAggregator
import l12_capstones.weather.WeatherReport
import l12_capstones.weather.WeatherResult
import l12_capstones.weather.WeatherSource

/**
 * =====================================================================
 *  CAPSTONE 12.B — Weather aggregator: YOUR implementation
 * =====================================================================
 *  HOW    : Read ../README.md, then replace every TODO() below, one
 *           milestone at a time. The KDoc of each function is in
 *           ../WeatherAggregator.kt.
 *  CHECK  : ./gradlew exercises --tests "l12_capstones.weather*"
 *  STUCK? : ../reference/ has one finished answer. Try first!
 *  TIP    : The tests run on virtual time, so they also check WHEN
 *           things happen (backoff waits, parallel fetching).
 * =====================================================================
 */
object WeatherAggregatorStarter : WeatherAggregator {
    override suspend fun fetchOne(source: WeatherSource, city: City, config: AggregatorConfig): WeatherResult =
        TODO("M1: timeout + retry with backoff")

    override suspend fun fetchAll(source: WeatherSource, cities: List<City>, config: AggregatorConfig): WeatherReport =
        TODO("M2: concurrent, limited, in order")

    override fun formatReport(report: WeatherReport): List<String> = TODO("M3: one line per city + summary")

    override fun refreshing(
        source: WeatherSource,
        cities: List<City>,
        config: AggregatorConfig,
        periodMillis: Long,
    ): Flow<WeatherReport> = TODO("M4: fetch now, then every periodMillis")
}
