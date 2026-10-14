package l11_advanced.lesson08_state_machines

/**
 * =====================================================================
 *  LESSON 11.8 — State machines with sealed types
 * =====================================================================
 *  GOAL   : Model "what can happen next" with a sealed State, a sealed
 *           Event and ONE pure function next(state, event). The compiler
 *           checks every case, illegal moves become values (not crashes),
 *           and a history is just a fold over events.
 *  RUN    : ./gradlew -q run -Plesson=l11_advanced.lesson08_state_machines.StateMachinesKt
 *  OUTPUT :
 *           Idle + Insert(100) -> Ready(credit=100)
 *           Ready(credit=100) + Insert(50) -> Ready(credit=150)
 *           Ready(credit=150) + Choose(tea) -> Serving(drink=tea, change=0)
 *           Serving(drink=tea, change=0) + Take -> Idle
 *           Idle + Choose(tea) -> rejected: insert money first
 *           Ready(credit=50) + Choose(tea) -> rejected: tea costs 150, credit is 50
 *           fold over 4 events ends in: Idle
 *           visited: Idle -> Ready -> Ready -> Serving -> Idle
 *  NEXT   : lesson09_java_interop/JavaInterop.kt
 * =====================================================================
 */

// ── 1. CONCEPT: states and events as sealed hierarchies ─────────────
sealed interface VendingState {
    data object Idle : VendingState

    data class Ready(val credit: Int) : VendingState

    data class Serving(val drink: String, val change: Int) : VendingState
}

sealed interface VendingEvent {
    data class Insert(val amount: Int) : VendingEvent

    data class Choose(val drink: String) : VendingEvent

    data object Take : VendingEvent
}

// ── 2. CONCEPT: an illegal move is a VALUE, not an exception ────────
// Asking for tea with no money is a normal thing users do (a business
// refusal), so it's a result, not a crash. Invalid ARGUMENTS (a negative
// coin) are caller bugs: require.
sealed interface Transition {
    data class Moved(val to: VendingState) : Transition

    data class Rejected(val reason: String) : Transition
}

private const val TEA_PRICE = 150
private val PRICES = mapOf("tea" to TEA_PRICE)

// ── 3. CONCEPT: one pure transition function ────────────────────────
// No else branches: add a new state or event and the compiler shows
// every place that must handle it.
fun next(
    state: VendingState,
    event: VendingEvent,
): Transition =
    when (state) {
        VendingState.Idle ->
            when (event) {
                is VendingEvent.Insert -> Transition.Moved(VendingState.Ready(credit = positive(event.amount)))
                is VendingEvent.Choose -> Transition.Rejected("insert money first")
                VendingEvent.Take -> Transition.Rejected("nothing to take")
            }
        is VendingState.Ready ->
            when (event) {
                is VendingEvent.Insert -> Transition.Moved(state.copy(credit = state.credit + positive(event.amount)))
                is VendingEvent.Choose -> choose(state, event.drink)
                VendingEvent.Take -> Transition.Rejected("choose a drink first")
            }
        is VendingState.Serving ->
            when (event) {
                VendingEvent.Take -> Transition.Moved(VendingState.Idle)
                is VendingEvent.Insert, is VendingEvent.Choose -> Transition.Rejected("take your ${state.drink} first")
            }
    }

private fun positive(amount: Int): Int {
    require(amount > 0) { "amount must be > 0, was $amount" }
    return amount
}

private fun choose(
    state: VendingState.Ready,
    drink: String,
): Transition {
    val price = PRICES[drink] ?: return Transition.Rejected("unknown drink $drink")
    if (state.credit < price) return Transition.Rejected("$drink costs $price, credit is ${state.credit}")
    return Transition.Moved(VendingState.Serving(drink, change = state.credit - price))
}

// Pretty names for printing.
fun show(state: VendingState): String =
    when (state) {
        VendingState.Idle -> "Idle"
        is VendingState.Ready -> "Ready(credit=${state.credit})"
        is VendingState.Serving -> "Serving(drink=${state.drink}, change=${state.change})"
    }

fun show(event: VendingEvent): String =
    when (event) {
        is VendingEvent.Insert -> "Insert(${event.amount})"
        is VendingEvent.Choose -> "Choose(${event.drink})"
        VendingEvent.Take -> "Take"
    }

fun showMove(
    state: VendingState,
    event: VendingEvent,
): String =
    when (val result = next(state, event)) {
        is Transition.Moved -> "${show(state)} + ${show(event)} -> ${show(result.to)}"
        is Transition.Rejected -> "${show(state)} + ${show(event)} -> rejected: ${result.reason}"
    }

private const val COIN = 100
private const val HALF_COIN = 50

fun main() {
    val events =
        listOf(
            VendingEvent.Insert(COIN),
            VendingEvent.Insert(HALF_COIN),
            VendingEvent.Choose("tea"),
            VendingEvent.Take,
        )
    // Walk the events one by one, printing each move.
    var state: VendingState = VendingState.Idle
    for (event in events) {
        println(showMove(state, event))
        state = (next(state, event) as? Transition.Moved)?.to ?: state
    }
    println(showMove(VendingState.Idle, VendingEvent.Choose("tea")))
    println(showMove(VendingState.Ready(HALF_COIN), VendingEvent.Choose("tea")))

    // ── 4. CONCEPT: history is a fold ────────────────────────────────
    // Rejected events keep the current state.
    fun step(
        current: VendingState,
        event: VendingEvent,
    ) = (next(current, event) as? Transition.Moved)?.to ?: current
    val start: VendingState = VendingState.Idle // the type is the sealed interface, not just Idle
    val end = events.fold(start, ::step)
    println("fold over ${events.size} events ends in: ${show(end)}")
    val visited = events.runningFold(start, ::step)
    println("visited: ${visited.joinToString(" -> ") { show(it).substringBefore("(") }}")

    // ── 5. WHY: pure transitions are easy to test ────────────────────
    // next() has no I/O and no hidden state: same input, same output.
    // Tests just call it (see practice/orders and its tests).

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Add a Refund event that returns the credit from Ready to Idle.
    //    Follow the compiler errors to every `when` that needs updating.
    // b) Add "coffee" to PRICES and buy one.
    // c) What happens with Insert(0)? Why is that an exception, not Rejected?
}
