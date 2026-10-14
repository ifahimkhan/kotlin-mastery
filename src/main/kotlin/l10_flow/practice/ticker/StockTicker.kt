package l10_flow.practice.ticker

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlin.math.abs

/**
 * =====================================================================
 *  PRACTICE 10.A — A live stock ticker in the console
 * =====================================================================
 *  GOAL   : Turn a stream of prices into a ticker: a moving average per
 *           symbol, and an alert whenever a price crosses a threshold.
 *  RUN    : ./gradlew -q run -Plesson=l10_flow.practice.ticker.StockTickerKt
 *  OUTPUT :
 *           KOT  $100.00   avg(3)        -
 *           JVM   $50.00   avg(3)        -
 *           KOT  $102.00   avg(3)        -
 *           >> ALERT KOT crossed above $101.00 (now $102.00)
 *           KOT  $104.50   avg(3)  $102.16
 *           JVM   $49.00   avg(3)        -
 *           KOT   $99.00   avg(3)  $101.83
 *           >> ALERT KOT crossed below $101.00 (now $99.00)
 *           JVM   $52.00   avg(3)   $50.33
 *           >> ALERT JVM crossed above $51.00 (now $52.00)
 *           KOT   $98.00   avg(3)  $100.50
 *           5 updates for KOT, 3 for JVM, 3 alerts
 *  DESIGN : The "exchange" is a scripted list sent as ONE flow, so the
 *           order is the same on every run (two real-time feeds merged
 *           would race). Each step is its own small operator built with
 *           flow { collect { ... } } (lesson03_terminal_operators):
 *           quotes -> movingAverages -> withAlerts -> print.
 *           State (recent prices, above/below) lives INSIDE each flow { },
 *           so every collector gets fresh state: the flow stays cold and
 *           reusable. Money is a Long of cents, never a Double.
 *  EXTEND : Add a "biggest mover" line at the end; or debounce alerts so
 *           a price jumping around the threshold doesn't spam
 *           (lesson05_buffering_timing).
 * =====================================================================
 */

private const val TICK_MILLIS = 100L
private const val AVERAGE_WINDOW = 3
private const val CENTS_PER_DOLLAR = 100
private const val PRICE_WIDTH = 8

data class Quote(
    val symbol: String,
    val priceCents: Long,
)

/** A quote plus the average of the last N prices of its symbol (null until N prices exist). */
data class PricePoint(
    val quote: Quote,
    val averageCents: Long?,
)

sealed interface TickerEvent {
    data class Update(
        val point: PricePoint,
    ) : TickerEvent

    data class Alert(
        val symbol: String,
        val priceCents: Long,
        val thresholdCents: Long,
        val crossedAbove: Boolean,
    ) : TickerEvent
}

/** Sends the scripted quotes one by one, [gapMillis] apart. */
fun scriptedFeed(
    quotes: List<Quote>,
    gapMillis: Long,
): Flow<Quote> =
    flow {
        quotes.forEachIndexed { index, quote ->
            if (index > 0) delay(gapMillis)
            emit(quote)
        }
    }

/**
 * Adds the moving average of the last [window] prices PER SYMBOL. The
 * average is rounded down to whole cents. [window] < 1 is a caller bug.
 */
fun movingAverages(
    quotes: Flow<Quote>,
    window: Int,
): Flow<PricePoint> {
    require(window >= 1) { "window must be at least 1, was $window" }
    return flow {
        var recentBySymbol = emptyMap<String, List<Long>>()
        quotes.collect { quote ->
            val recent = ((recentBySymbol[quote.symbol] ?: emptyList()) + quote.priceCents).takeLast(window)
            recentBySymbol = recentBySymbol + (quote.symbol to recent)
            val average = if (recent.size == window) recent.sum() / window else null
            emit(PricePoint(quote, average))
        }
    }
}

/**
 * Emits an Update for every point, followed by an Alert when the price of
 * a watched symbol moves from one side of its threshold to the other
 * ("at or above" counts as above). The first price only sets the side.
 */
fun withAlerts(
    points: Flow<PricePoint>,
    thresholdsCents: Map<String, Long>,
): Flow<TickerEvent> =
    flow {
        var aboveBySymbol = emptyMap<String, Boolean>()
        points.collect { point ->
            emit(TickerEvent.Update(point))
            val symbol = point.quote.symbol
            val threshold = thresholdsCents[symbol] ?: return@collect // not watched
            val isAbove = point.quote.priceCents >= threshold
            val wasAbove = aboveBySymbol[symbol]
            aboveBySymbol = aboveBySymbol + (symbol to isAbove)
            if (wasAbove != null && wasAbove != isAbove) {
                emit(TickerEvent.Alert(symbol, point.quote.priceCents, threshold, isAbove))
            }
        }
    }

/** 10450 -> "$104.50", -5 -> "-$0.05". */
fun formatCents(cents: Long): String {
    val sign = if (cents < 0) "-" else ""
    val absolute = abs(cents)
    val dollars = absolute / CENTS_PER_DOLLAR
    val rest = (absolute % CENTS_PER_DOLLAR).toString().padStart(2, '0')
    return "$sign$$dollars.$rest"
}

fun describe(event: TickerEvent): String =
    when (event) {
        is TickerEvent.Update -> {
            val quote = event.point.quote
            val price = formatCents(quote.priceCents).padStart(PRICE_WIDTH)
            val average = (event.point.averageCents?.let(::formatCents) ?: "-").padStart(PRICE_WIDTH)
            "${quote.symbol} $price   avg($AVERAGE_WINDOW) $average"
        }
        is TickerEvent.Alert -> {
            val direction = if (event.crossedAbove) "above" else "below"
            ">> ALERT ${event.symbol} crossed $direction ${formatCents(event.thresholdCents)} " +
                "(now ${formatCents(event.priceCents)})"
        }
    }

val demoQuotes =
    listOf(
        Quote("KOT", 10_000),
        Quote("JVM", 5_000),
        Quote("KOT", 10_200),
        Quote("KOT", 10_450),
        Quote("JVM", 4_900),
        Quote("KOT", 9_900),
        Quote("JVM", 5_200),
        Quote("KOT", 9_800),
    )

val demoThresholds = mapOf("KOT" to 10_100L, "JVM" to 5_100L)

fun main() =
    runBlocking {
        var updates = emptyMap<String, Int>()
        var alerts = 0
        val events = withAlerts(movingAverages(scriptedFeed(demoQuotes, TICK_MILLIS), AVERAGE_WINDOW), demoThresholds)
        events.collect { event ->
            println(describe(event))
            when (event) {
                is TickerEvent.Update -> {
                    val symbol = event.point.quote.symbol
                    updates = updates + (symbol to (updates[symbol] ?: 0) + 1)
                }
                is TickerEvent.Alert -> alerts++
            }
        }
        println("${updates["KOT"]} updates for KOT, ${updates["JVM"]} for JVM, $alerts alerts")
    }
