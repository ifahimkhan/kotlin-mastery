package l07_functional.practice

import l07_functional.practice.eventbus.Event
import l07_functional.practice.eventbus.EventBus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EventBusTest {
    @Test
    fun `publish reaches only handlers of the same topic`() {
        val bus = EventBus()
        val chat = mutableListOf<String>()
        val news = mutableListOf<String>()
        bus.subscribe("chat") { chat.add(it.text) }
        bus.subscribe("news") { news.add(it.text) }

        assertEquals(1, bus.publish(Event("chat", "Ada", "hi")))
        assertEquals(listOf("hi"), chat)
        assertEquals(emptyList(), news)
        assertEquals(0, bus.publish(Event("sports", "bot", "goal")))
    }

    @Test
    fun `unsubscribe removes exactly that subscription and is safe to call twice`() {
        val bus = EventBus()
        val received = mutableListOf<String>()
        val handler: (Event) -> Unit = { received.add(it.text) }
        val stopFirst = bus.subscribe("chat", handler)
        bus.subscribe("chat", handler) // same lambda, second subscription

        stopFirst()
        stopFirst()
        assertEquals(1, bus.publish(Event("chat", "Ada", "x")), "The second subscription must survive")
        assertEquals(listOf("x"), received)
    }

    @Test
    fun `a handler may unsubscribe itself while being called`() {
        val bus = EventBus()
        var calls = 0
        var stop: () -> Unit = {}
        stop =
            bus.subscribe("tick") {
                calls++
                stop()
            }
        bus.subscribe("tick") { }

        assertEquals(2, bus.publish(Event("tick", "clock", "1")))
        assertEquals(1, bus.publish(Event("tick", "clock", "2")))
        assertEquals(1, calls)
    }

    @Test
    fun `topics lists topics with subscribers and blank topics are rejected`() {
        val bus = EventBus()
        val stop = bus.subscribe("chat") { }
        bus.subscribe("news") { }
        assertEquals(setOf("chat", "news"), bus.topics())
        stop()
        assertEquals(setOf("news"), bus.topics())
        assertFailsWith<IllegalArgumentException> { bus.subscribe(" ") { } }
    }
}
