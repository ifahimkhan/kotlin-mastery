package l11_advanced.practice.orders

/**
 * =====================================================================
 *  PRACTICE 11.C — An order lifecycle as a state machine
 * =====================================================================
 *  GOAL   : Model an online order: Created -> Paid -> Shipped ->
 *           Delivered, with cancelling and refunds, as sealed states and
 *           events, one pure reducer, rejected moves as values, and an
 *           audit trail.
 *  RUN    : ./gradlew -q run -Plesson=l11_advanced.practice.orders.OrderStateMachineKt
 *  OUTPUT :
 *           Created --Pay(2500)--> Paid(amount=2500)
 *           Paid(amount=2500) --Ship(TRK-1)--> Shipped(tracking=TRK-1)
 *           Shipped(tracking=TRK-1) --Cancel--> REJECTED: cannot cancel a shipped order
 *           Shipped(tracking=TRK-1) --Deliver--> Delivered
 *           Delivered --Refund--> Refunded(amount=2500)
 *           final: Refunded(amount=2500), accepted 4 of 5 events
 *           cancel early: Created --Cancel--> Cancelled
 *           cancel after paying: Paid(amount=2500) --Cancel--> Refunded(amount=2500)
 *  DESIGN : reduce(order, event) is pure: no I/O, no clock, no mutation.
 *           An Order carries its state, the amount paid and an immutable
 *           history, so "Refunded" knows how much to return. Rejected
 *           events are recorded too, which is what an audit log needs.
 *           A non-positive payment is a caller bug: require. Cancelling a
 *           paid order refunds it, so money is never stranded. Every
 *           decision is a `when (state)` with no else branch.
 *  EXTEND : Add a `Return` event allowed only from Delivered, leading to a
 *           new Returned state. Let the compiler find every `when`.
 * =====================================================================
 */

sealed interface OrderState {
    data object Created : OrderState

    data class Paid(val amountCents: Long) : OrderState

    data class Shipped(val tracking: String) : OrderState

    data object Delivered : OrderState

    data object Cancelled : OrderState

    data class Refunded(val amountCents: Long) : OrderState
}

sealed interface OrderEvent {
    data class Pay(val amountCents: Long) : OrderEvent

    data class Ship(val tracking: String) : OrderEvent

    data object Deliver : OrderEvent

    data object Cancel : OrderEvent

    data object Refund : OrderEvent
}

/** One line of the audit trail. [rejection] is null for accepted events. */
data class AuditEntry(
    val from: OrderState,
    val event: OrderEvent,
    val to: OrderState,
    val rejection: String?,
)

data class Order(
    val state: OrderState = OrderState.Created,
    val paidCents: Long = 0,
    val history: List<AuditEntry> = emptyList(),
)

/** The outcome of one event: the next state, or why it was refused. */
sealed interface Decision {
    data class Accept(val to: OrderState, val paidCents: Long) : Decision

    data class Reject(val reason: String) : Decision
}

fun decide(
    order: Order,
    event: OrderEvent,
): Decision {
    // Every branch below is a `when (state)` with NO else: add a new
    // OrderState and the compiler points at every place that must decide
    // what that state does with each event.
    val state = order.state
    val paid = order.paidCents
    return when (event) {
        is OrderEvent.Pay -> {
            require(event.amountCents > 0) { "payment must be > 0, was ${event.amountCents}" }
            when (state) {
                OrderState.Created -> Decision.Accept(OrderState.Paid(event.amountCents), event.amountCents)
                is OrderState.Paid, is OrderState.Shipped, OrderState.Delivered,
                OrderState.Cancelled, is OrderState.Refunded,
                -> Decision.Reject("already paid or closed")
            }
        }
        is OrderEvent.Ship ->
            when (state) {
                is OrderState.Paid -> Decision.Accept(OrderState.Shipped(event.tracking), paid)
                OrderState.Created, is OrderState.Shipped, OrderState.Delivered,
                OrderState.Cancelled, is OrderState.Refunded,
                -> Decision.Reject("only a paid order can ship")
            }
        OrderEvent.Deliver ->
            when (state) {
                is OrderState.Shipped -> Decision.Accept(OrderState.Delivered, paid)
                OrderState.Created, is OrderState.Paid, OrderState.Delivered,
                OrderState.Cancelled, is OrderState.Refunded,
                -> Decision.Reject("only a shipped order can be delivered")
            }
        OrderEvent.Cancel ->
            when (state) {
                OrderState.Created -> Decision.Accept(OrderState.Cancelled, paid)
                // Cancelling a PAID order must give the money back, never strand it.
                is OrderState.Paid -> Decision.Accept(OrderState.Refunded(paid), paid)
                is OrderState.Shipped -> Decision.Reject("cannot cancel a shipped order")
                OrderState.Delivered, OrderState.Cancelled, is OrderState.Refunded ->
                    Decision.Reject("order is already closed")
            }
        OrderEvent.Refund ->
            when (state) {
                is OrderState.Paid, OrderState.Delivered -> Decision.Accept(OrderState.Refunded(paid), paid)
                OrderState.Created, is OrderState.Shipped, OrderState.Cancelled, is OrderState.Refunded ->
                    Decision.Reject("nothing to refund in this state")
            }
    }
}

/** Applies [event] and records it. Never throws for a refused move. */
fun reduce(
    order: Order,
    event: OrderEvent,
): Order =
    when (val decision = decide(order, event)) {
        is Decision.Accept ->
            order.copy(
                state = decision.to,
                paidCents = decision.paidCents,
                history = order.history + AuditEntry(order.state, event, decision.to, rejection = null),
            )
        is Decision.Reject ->
            order.copy(history = order.history + AuditEntry(order.state, event, order.state, decision.reason))
    }

fun show(state: OrderState): String =
    when (state) {
        OrderState.Created -> "Created"
        is OrderState.Paid -> "Paid(amount=${state.amountCents})"
        is OrderState.Shipped -> "Shipped(tracking=${state.tracking})"
        OrderState.Delivered -> "Delivered"
        OrderState.Cancelled -> "Cancelled"
        is OrderState.Refunded -> "Refunded(amount=${state.amountCents})"
    }

fun show(event: OrderEvent): String =
    when (event) {
        is OrderEvent.Pay -> "Pay(${event.amountCents})"
        is OrderEvent.Ship -> "Ship(${event.tracking})"
        OrderEvent.Deliver -> "Deliver"
        OrderEvent.Cancel -> "Cancel"
        OrderEvent.Refund -> "Refund"
    }

fun show(entry: AuditEntry): String {
    val target = entry.rejection?.let { "REJECTED: $it" } ?: show(entry.to)
    return "${show(entry.from)} --${show(entry.event)}--> $target"
}

private const val PRICE_CENTS = 2500L

fun main() {
    val events =
        listOf(
            OrderEvent.Pay(PRICE_CENTS),
            OrderEvent.Ship("TRK-1"),
            OrderEvent.Cancel,
            OrderEvent.Deliver,
            OrderEvent.Refund,
        )
    val finished = events.fold(Order(), ::reduce)
    finished.history.forEach { println(show(it)) }
    val accepted = finished.history.count { it.rejection == null }
    println("final: ${show(finished.state)}, accepted $accepted of ${events.size} events")

    val cancelled = reduce(Order(), OrderEvent.Cancel)
    println("cancel early: ${show(cancelled.history.single())}")

    val paidThenCancelled = listOf(OrderEvent.Pay(PRICE_CENTS), OrderEvent.Cancel).fold(Order(), ::reduce)
    println("cancel after paying: ${show(paidThenCancelled.history.last())}")
}
