// currentTime (virtual time) is still marked experimental in kotlinx-coroutines-test.
@file:OptIn(ExperimentalCoroutinesApi::class)

package l10_flow.practice

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import l10_flow.practice.ticker.PricePoint
import l10_flow.practice.ticker.Quote
import l10_flow.practice.ticker.TickerEvent
import l10_flow.practice.ticker.formatCents
import l10_flow.practice.ticker.movingAverages
import l10_flow.practice.ticker.scriptedFeed
import l10_flow.practice.ticker.withAlerts
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StockTickerTest {
    @Test
    fun `scripted feed waits between quotes`() =
        runTest {
            val quotes = listOf(Quote("A", 1), Quote("A", 2), Quote("A", 3))
            assertEquals(quotes, scriptedFeed(quotes, 100).toList())
            assertEquals(200, currentTime) // two gaps, no wait before the first quote
        }

    @Test
    fun `moving average is per symbol and null until the window is full`() =
        runTest {
            val quotes = flowOf(Quote("A", 100), Quote("B", 900), Quote("A", 200), Quote("A", 600))
            val averages = movingAverages(quotes, 2).map { it.quote.symbol to it.averageCents }.toList()
            assertEquals(listOf("A" to null, "B" to null, "A" to 150L, "A" to 400L), averages)
        }

    @Test
    fun `moving average rejects a window below 1 right away`() {
        assertFailsWith<IllegalArgumentException> { movingAverages(flowOf(), 0) }
    }

    @Test
    fun `alerts fire only when the side of the threshold changes`() =
        runTest {
            val points =
                flowOf(100L, 150L, 160L, 90L)
                    .map { PricePoint(Quote("A", it), null) }
            withAlerts(points, mapOf("A" to 150L)).test {
                assertEquals(100L, (awaitItem() as TickerEvent.Update).point.quote.priceCents)
                awaitItem() // update 150
                assertEquals(TickerEvent.Alert("A", 150, 150, crossedAbove = true), awaitItem())
                awaitItem() // update 160: still above, no alert
                awaitItem() // update 90
                assertEquals(TickerEvent.Alert("A", 90, 150, crossedAbove = false), awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun `unwatched symbols never alert`() =
        runTest {
            val points = flowOf(1L, 1_000L).map { PricePoint(Quote("X", it), null) }
            val events = withAlerts(points, mapOf("A" to 500L)).toList()
            assertEquals(2, events.size)
        }

    @Test
    fun `cents are formatted as dollars`() {
        assertEquals("$104.50", formatCents(10_450))
        assertEquals("$0.05", formatCents(5))
        assertEquals("-$1.20", formatCents(-120))
    }
}
