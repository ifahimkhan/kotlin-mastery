# Capstone 12.B — Weather aggregator

> Ask a real web API for the weather in many cities at once. Survive slow
> answers, flaky networks and broken data, and keep the results fresh.

## What you build

An aggregator that gets current weather for a list of cities, robustly.
The contract is in [WeatherAggregator.kt](WeatherAggregator.kt), and you
implement it in [starter/WeatherAggregatorStarter.kt](starter/WeatherAggregatorStarter.kt):

```
Lisbon     21.5 C  wind 12.0 km/h
Oslo       4.0 C  wind 20.5 km/h
Tokyo      unavailable: timeout
Nairobi    25.3 C  wind 9.0 km/h
3 of 4 cities updated
```

The aggregator never touches the network itself. It's handed a
`WeatherSource`:
- **[OpenMeteoSource](OpenMeteoSource.kt):** the real thing. It calls
  Ktor's HTTP client against the free
  [Open-Meteo API](https://open-meteo.com/), which needs no API key, and
  parses the JSON with kotlinx.serialization.
- **[FakeWeatherSource](FakeWeatherSource.kt):** scripted answers ("fails
  once, then works", "takes 10 seconds"...). The tests use it on virtual
  time, so they're instant and never flaky.

## Milestones

| # | Build | Uses | Check |
|---|-------|------|-------|
| 1 | `fetchOne`: timeout per attempt, retry with exponential backoff | l09 `withTimeoutOrNull`, `delay`, l05 sealed results | `--tests "*WeatherStarterTest.m1*"` |
| 2 | `fetchAll`: concurrent, at most N at once, results in request order | l09 `async`/`awaitAll`, `Semaphore`, `coroutineScope` | `m2*` |
| 3 | `formatReport` | l01 strings, `padEnd`, `String.format(Locale.ROOT, ...)` | `m3*` |
| 4 | `refreshing`: fetch now, then every period | l10 `flow { }`, `emit`; `stateIn` in [Dashboard.kt](Dashboard.kt) | `m4*` |

```bash
./gradlew exercises --tests "*WeatherStarterTest.m1*"
./gradlew exercises --tests "l12_capstones.weather*"   # everything
```

## Run it

```bash
# Offline: fake data, always works
./gradlew -q run -Plesson=l12_capstones.weather.MainKt

# Live: real weather from Open-Meteo (needs internet)
./gradlew -q run -Plesson=l12_capstones.weather.MainKt --args="--live"
```

In live mode, Ktor prints a one-time `SLF4J(W): No SLF4J providers were
found` warning. It's harmless: it only means no logging library is
installed.

Once your tests pass, switch `WeatherAggregatorReference` to
`WeatherAggregatorStarter` in [Main.kt](Main.kt).

## Hints

- A timeout is not an exception you need to catch: `withTimeoutOrNull`
  returns `null`.
- Retry only `retryable` failures. `BadData` won't fix itself.
- `awaitAll()` returns results in the order of the list, not the order
  they finished.
- Limit **suspending** calls with a `Semaphore`. `limitedParallelism` only
  limits threads (l09 lesson 06).
- Don't trust the source blindly. If `source.current()` throws anything
  except `CancellationException`, turn it into a `Network` failure.
  Otherwise one broken city cancels all the others inside `coroutineScope`.
- Rounding: `String.format(Locale.ROOT, "%.1f", x)` rounds half away from
  zero (-3.25 becomes "-3.3"). `roundToInt() / 10.0` would give -3.2, and
  the test expects -3.3.
- HTTP errors (in `OpenMeteoSource`): 4xx means "your request is wrong", so
  retrying won't help (`BadData`). The exceptions are 408 and 429, which,
  like 5xx, are worth retrying (`Network`).

## Stretch goals

- Add a `forecast(city, days)` with the `daily=temperature_2m_max` field.
- Cache answers for 10 minutes. The type-safe cache from l08 is a good start.
- Look up coordinates by city name with Open-Meteo's geocoding API.
