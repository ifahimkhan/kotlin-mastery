package l10_flow.lesson09_sharedflow

import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * =====================================================================
 *  LESSON 10.9 — SharedFlow: broadcasting events
 * =====================================================================
 *  GOAL   : Broadcast values to many collectors with MutableSharedFlow,
 *           choose replay and buffer settings, use emit vs tryEmit, and
 *           pick SharedFlow, StateFlow or Channel for the job.
 *  RUN    : ./gradlew -q run -Plesson=l10_flow.lesson09_sharedflow.SharedFlowBasicsKt
 *  OUTPUT :
 *           subscribers before: 0 (that emit was lost)
 *           subscriber A got: [hello, world]
 *           subscriber B got: [hello, world]
 *           replay cache: [b, c]
 *           late subscriber got: [b, c]
 *           tryEmit with a waiting subscriber and no buffer: false
 *           tryEmit with room in the buffer: true
 *           DROP_OLDEST kept: [3, 4]
 *           channel kept the event for a late collector: navigate
 *  NEXT   : lesson10_statein_sharein/StateInShareIn.kt
 * =====================================================================
 */

private const val REPLAY = 2
private const val BUFFER = 2
private const val SUBSCRIBERS = 2

fun main() =
    runBlocking {
        // ── 1. CONCEPT: a SharedFlow is hot ──────────────────────────────
        // A plain flow { } runs once PER collector (cold). A SharedFlow is
        // one stream that exists on its own; collectors just listen in.
        // With the default settings (replay = 0), nothing is stored: emit
        // with no subscribers simply drops the value.
        val events = MutableSharedFlow<String>()
        events.emit("lost: nobody is listening")
        println("subscribers before: ${events.subscriptionCount.value} (that emit was lost)")

        // ── 2. CONCEPT: every subscriber gets every value ────────────────
        // Start two subscribers, then WAIT until both are really subscribed
        // (a handshake via subscriptionCount, itself a StateFlow) before
        // emitting. Without the handshake, early values could be missed.
        val a = async { events.take(2).toList() }
        val b = async { events.take(2).toList() }
        events.subscriptionCount.first { it == SUBSCRIBERS }
        events.emit("hello")
        events.emit("world")
        println("subscriber A got: ${a.await()}")
        println("subscriber B got: ${b.await()}")

        // ── 3. CONCEPT: replay for late subscribers ──────────────────────
        // replay = N keeps the last N values and hands them to every NEW
        // subscriber first.
        val history = MutableSharedFlow<String>(replay = REPLAY)
        history.emit("a")
        history.emit("b")
        history.emit("c")
        println("replay cache: ${history.replayCache}")
        println("late subscriber got: ${history.take(REPLAY).toList()}")

        // ── 4. CONCEPT: emit vs tryEmit and the buffer ───────────────────
        // emit() SUSPENDS when subscribers are slow and the buffer is full.
        // tryEmit() never suspends: it returns false instead. With the
        // default settings there is no buffer at all, so tryEmit fails as
        // soon as anyone is subscribed.
        val noBuffer = MutableSharedFlow<Int>()
        val waiting = launch { noBuffer.collect { } }
        noBuffer.subscriptionCount.first { it == 1 }
        println("tryEmit with a waiting subscriber and no buffer: ${noBuffer.tryEmit(1)}")
        waiting.cancel()

        // extraBufferCapacity gives emitters room, so tryEmit can succeed
        // even when subscribers haven't caught up yet.
        val buffered = MutableSharedFlow<Int>(extraBufferCapacity = BUFFER)
        val slow = launch { buffered.collect { } }
        buffered.subscriptionCount.first { it == 1 }
        println("tryEmit with room in the buffer: ${buffered.tryEmit(1)}")
        slow.cancel()

        // onBufferOverflow decides what happens when the buffer is full:
        // SUSPEND (default), DROP_OLDEST or DROP_LATEST. Here replay doubles
        // as the buffer, and the oldest values are thrown away.
        val latestTwo = MutableSharedFlow<Int>(replay = REPLAY, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        for (n in 1..4) latestTwo.tryEmit(n) // never fails, never suspends
        println("DROP_OLDEST kept: ${latestTwo.replayCache}")

        // ── 5. PITFALL: one-shot events and late collectors ──────────────
        // "Show this message once" or "navigate to the next screen" must not
        // be lost and must not be repeated. SharedFlow(replay = 0) LOSES an
        // event if the collector isn't there yet (section 1). replay = 1
        // REPEATS it to every new collector. A Channel keeps the event until
        // exactly one collector takes it (l09_coroutines/lesson14_channels).
        val oneShot = Channel<String>(Channel.BUFFERED)
        oneShot.send("navigate") // nobody listening yet - it waits in the buffer
        println("channel kept the event for a late collector: ${oneShot.receive()}")

        // ── 6. WHY: which hot stream? ────────────────────────────────────
        // StateFlow  - the CURRENT state; new collectors get it at once (lesson08).
        // SharedFlow - broadcast to every current collector; replay is optional.
        // Channel    - each value goes to ONE receiver; good for one-shot events.

        // ── 7. TRY IT ────────────────────────────────────────────────────
        // a) In section 2, remove the subscriptionCount.first { } line.
        //    Do both subscribers still get both values? (It will hang: stop the program yourself.)
        // b) Change REPLAY to 3 in section 3. What does the late subscriber get?
        // c) Use DROP_LATEST in section 4. Which values are kept now?
    }
