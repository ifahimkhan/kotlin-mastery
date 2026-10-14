package l12_capstones.weather

import l12_capstones.weather.reference.WeatherAggregatorReference

/** Proves the reference passes every acceptance test. Runs with `./gradlew test`. */
class WeatherReferenceTest : WeatherAggregatorTest(WeatherAggregatorReference)
