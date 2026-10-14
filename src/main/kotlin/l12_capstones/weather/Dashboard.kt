package l12_capstones.weather

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Turns the cold `refreshing` flow into a hot StateFlow that always
 * holds the latest report (null until the first round finishes).
 * This is the shape a UI wants: read `.value` any time, or collect it to
 * redraw on every change (l10 lesson 10, stateIn).
 *
 * The flow lives as long as [scope]: cancel the scope to stop refreshing.
 *
 * Careful: stateIn runs the upstream flow as a child of [scope]. If the
 * flow ever THROWS, that exception cancels [scope] itself (and everything
 * else running in it). Here that can't happen: fetchOne turns every
 * source problem into a Failure value, so a round of fetching never
 * throws. If you write your own refreshing(), keep that promise, or add
 * `.catch { }` before stateIn.
 */
fun WeatherAggregator.dashboard(
    scope: CoroutineScope,
    source: WeatherSource,
    cities: List<City>,
    config: AggregatorConfig,
    periodMillis: Long,
): StateFlow<WeatherReport?> =
    refreshing(source, cities, config, periodMillis)
        .stateIn(scope, SharingStarted.Eagerly, initialValue = null)
