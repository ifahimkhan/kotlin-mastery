package l11_advanced.practice

import l11_advanced.practice.orders.Order
import l11_advanced.practice.orders.OrderEvent
import l11_advanced.practice.orders.OrderState
import l11_advanced.practice.orders.reduce
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OrderStateMachineTest {
    private fun play(vararg events: OrderEvent): Order = events.fold(Order(), ::reduce)

    @Test
    fun `happy path ends delivered`() {
        val order = play(OrderEvent.Pay(1000), OrderEvent.Ship("T1"), OrderEvent.Deliver)
        assertEquals(OrderState.Delivered, order.state)
        assertEquals(1000, order.paidCents)
        assertEquals(3, order.history.size)
        assertNull(order.history.firstOrNull { it.rejection != null })
    }

    @Test
    fun `cancel is allowed before shipping only`() {
        assertEquals(OrderState.Cancelled, play(OrderEvent.Cancel).state)
        assertEquals(
            OrderState.Refunded(500),
            play(OrderEvent.Pay(500), OrderEvent.Cancel).state,
            "Cancelling a paid order must refund it",
        )
        val shipped = play(OrderEvent.Pay(500), OrderEvent.Ship("T"), OrderEvent.Cancel)
        assertEquals(OrderState.Shipped("T"), shipped.state, "A rejected event keeps the state")
        assertNotNull(shipped.history.last().rejection)
    }

    @Test
    fun `refund returns what was paid`() {
        assertEquals(OrderState.Refunded(700), play(OrderEvent.Pay(700), OrderEvent.Refund).state)
        val delivered = play(OrderEvent.Pay(900), OrderEvent.Ship("T"), OrderEvent.Deliver, OrderEvent.Refund)
        assertEquals(OrderState.Refunded(900), delivered.state)
        assertEquals(OrderState.Created, play(OrderEvent.Refund).state, "Nothing to refund before paying")
    }

    @Test
    fun `out-of-order events are recorded as rejected, not thrown`() {
        val order = play(OrderEvent.Deliver, OrderEvent.Ship("T"), OrderEvent.Pay(100), OrderEvent.Pay(100))
        assertEquals(OrderState.Paid(100), order.state)
        assertEquals(listOf(true, true, false, true), order.history.map { it.rejection != null })
    }

    @Test
    fun `a non-positive payment is a caller bug`() {
        assertFailsWith<IllegalArgumentException> { play(OrderEvent.Pay(0)) }
    }
}
