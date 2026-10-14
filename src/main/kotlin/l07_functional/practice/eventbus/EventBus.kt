package l07_functional.practice.eventbus

/**
 * =====================================================================
 *  PRACTICE 07.B — A tiny event bus
 * =====================================================================
 *  GOAL   : Use lambdas as callbacks: subscribe handlers to topics,
 *           publish events, and unsubscribe with a returned function.
 *  RUN    : ./gradlew -q run -Plesson=l07_functional.practice.eventbus.EventBusKt
 *  OUTPUT :
 *           [chat] Ada: hi all
 *           [audit] chat event #1
 *           notified 2 handler(s)
 *           [chat] Alan: hello
 *           [audit] chat event #2
 *           unsubscribed the audit handler
 *           [chat] Ada: bye
 *           notified 1 handler(s)
 *           news events nobody listens to -> notified 0 handler(s)
 *           topics with subscribers: [chat]
 *  DESIGN : The handler list is private; callers only get a read-only
 *           view (topics) and an "unsubscribe" FUNCTION, so nobody can
 *           remove someone else's handler. publish() loops over a COPY,
 *           so a handler may unsubscribe itself safely (lesson 06.10).
 *           Two consequences of that copy: a handler removed DURING a
 *           publish still receives that one event, and if one handler
 *           throws, the handlers after it get nothing (no try/catch yet).
 *           Single-threaded on purpose: thread safety comes in level 09.
 *  EXTEND : Add subscribeOnce(topic, handler) that unsubscribes itself
 *           after the first event. Later (try/catch: later level), make
 *           one failing handler unable to stop the others.
 * =====================================================================
 */

/** Something that happened. */
data class Event(val topic: String, val sender: String, val text: String)

/** What subscribers provide: a function that reacts to an event. */
typealias Handler = (Event) -> Unit

class EventBus {
    // A plain (non-data) class: two subscriptions are different objects
    // even when topic and handler are equal, so each unsubscribe removes
    // exactly ITS subscription.
    private class Subscription(val topic: String, val handler: Handler)

    private val subscriptions = mutableListOf<Subscription>()

    /** Registers [handler] for [topic]. Returns a function that undoes it. */
    fun subscribe(topic: String, handler: Handler): () -> Unit {
        require(topic.isNotBlank()) { "topic must not be blank" }
        val subscription = Subscription(topic, handler)
        subscriptions.add(subscription)
        return { subscriptions.remove(subscription) } // calling it twice is harmless
    }

    /** Delivers [event] to every handler of its topic. Returns how many were notified. */
    fun publish(event: Event): Int {
        val receivers = subscriptions.filter { it.topic == event.topic } // a copy
        receivers.forEach { it.handler(event) }
        return receivers.size
    }

    /** Read-only view: which topics currently have subscribers. */
    fun topics(): Set<String> = subscriptions.map { it.topic }.toSet()
}

fun main() {
    val bus = EventBus()

    bus.subscribe("chat") { println("[chat] ${it.sender}: ${it.text}") }

    var chatEvents = 0 // captured and updated by the audit handler (a closure)
    val stopAudit =
        bus.subscribe("chat") {
            chatEvents++
            println("[audit] chat event #$chatEvents")
        }

    println("notified ${bus.publish(Event("chat", "Ada", "hi all"))} handler(s)")
    bus.publish(Event("chat", "Alan", "hello"))

    stopAudit()
    println("unsubscribed the audit handler")
    println("notified ${bus.publish(Event("chat", "Ada", "bye"))} handler(s)")

    println("news events nobody listens to -> notified ${bus.publish(Event("news", "bot", "x"))} handler(s)")
    println("topics with subscribers: ${bus.topics()}")
}
