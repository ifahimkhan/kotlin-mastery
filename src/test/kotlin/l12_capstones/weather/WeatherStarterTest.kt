package l12_capstones.weather

import l12_capstones.weather.starter.WeatherAggregatorStarter
import org.junit.jupiter.api.Tag

/**
 * Checks YOUR aggregator in starter/WeatherAggregatorStarter.kt.
 *
 * Run: ./gradlew exercises --tests "l12_capstones.weather*"
 * One milestone only: ./gradlew exercises --tests "*WeatherStarterTest.m1*"
 */
@Tag("exercise")
class WeatherStarterTest : WeatherAggregatorTest(WeatherAggregatorStarter)
