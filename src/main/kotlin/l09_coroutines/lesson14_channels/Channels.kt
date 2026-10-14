package l09_coroutines.lesson14_channels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * =====================================================================
 *  LESSON 09.14 — Channels: coroutines talking to each other
 * =====================================================================
 *  GOAL   : Send values from one coroutine to another with a Channel,
 *           pick the right capacity, close channels, and build
 *           pipelines, fan-out and fan-in.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.lesson14_channels.ChannelsKt
 *  OUTPUT :
 *           send    hello
 *           send    from
 *           receive hello
 *           receive from
 *           send    a coroutine
 *           receive a coroutine
 *           buffered: 1, 2
 *           conflated keeps only: 22
 *           pipeline numbers -> squares: [1, 4, 9, 16, 25]
 *           fan-out: 9 jobs, each once: true
 *           fan-in arrival order: [A0, B0, A1, B1, A2, B2]
 *           early exit: read 1, 2 then cancelled the rest
 *  NEXT   : lesson15_select/Select.kt
 * =====================================================================
 */

private const val WORKERS = 3
private const val JOBS = 9
private const val TICK_MS = 300L
private const val OFFSET_MS = 150L // B ticks land 150 ms between A ticks: a safe gap

// ── produce { } builds a coroutine that sends into a channel it owns,
// and closes the channel automatically when the block ends.
// produce is marked @ExperimentalCoroutinesApi: its signature may still
// change in a future version. @OptIn says "I know, I accept that".
@OptIn(ExperimentalCoroutinesApi::class)
fun CoroutineScope.numbers(count: Int): ReceiveChannel<Int> = produce {
    for (n in 1..count) send(n)
}

@OptIn(ExperimentalCoroutinesApi::class)
fun CoroutineScope.squares(input: ReceiveChannel<Int>): ReceiveChannel<Int> = produce {
    for (n in input) send(n * n)
}

@OptIn(ExperimentalCoroutinesApi::class)
fun CoroutineScope.ticker(name: String, startMs: Long, count: Int): ReceiveChannel<String> = produce {
    delay(startMs)
    repeat(count) { i ->
        send("$name$i")
        delay(TICK_MS)
    }
}

fun main() = runBlocking {
    // ── 1. CONCEPT: send / receive / close ───────────────────────────
    // A Channel is like a queue between coroutines, but send() and
    // receive() SUSPEND instead of blocking. The default channel has no
    // buffer ("rendezvous"): a send waits until someone receives.
    val channel = Channel<String>()
    launch {
        for (word in listOf("hello", "from", "a coroutine")) {
            println("send    $word")
            channel.send(word)
        }
        channel.close() // "no more values": ends the receiver's for-loop
    }
    for (word in channel) println("receive $word")

    // ── 2. CONCEPT: capacity ─────────────────────────────────────────
    // Channel(2)        buffered: up to 2 sends don't wait
    // Channel(UNLIMITED) never waits (but can eat all your memory)
    // Channel(CONFLATED) keeps only the LATEST value
    val buffered = Channel<Int>(2)
    buffered.send(1)
    buffered.send(2) // both fit in the buffer, no receiver needed yet
    println("buffered: ${buffered.receive()}, ${buffered.receive()}")

    val latestOnly = Channel<Int>(Channel.CONFLATED)
    for (temperature in 18..22) latestOnly.send(temperature) // never waits
    latestOnly.close()
    println("conflated keeps only: ${latestOnly.receive()}")

    // ── 3. CONCEPT: pipelines with produce ───────────────────────────
    val results = mutableListOf<Int>()
    for (square in squares(numbers(5))) results.add(square)
    println("pipeline numbers -> squares: $results")

    // ── 4. CONCEPT: fan-out - many workers, one channel ──────────────
    // Each value is received by exactly ONE worker.
    val jobs = numbers(JOBS)
    val handled = mutableListOf<Int>() // only touched by runBlocking's single thread
    val workers = List(WORKERS) { id ->
        launch {
            for (job in jobs) {
                handled.add(job)
                delay(TICK_MS / WORKERS + id) // workers have different speeds
            }
        }
    }
    workers.forEach { it.join() }
    println("fan-out: ${handled.size} jobs, each once: ${handled.sorted() == (1..JOBS).toList()}")

    // ── 5. CONCEPT: fan-in - many senders, one channel ───────────────
    val merged = Channel<String>()
    val senders = listOf(
        launch { for (tick in ticker("A", startMs = 0, count = 3)) merged.send(tick) },
        launch { for (tick in ticker("B", startMs = OFFSET_MS, count = 3)) merged.send(tick) },
    )
    launch {
        senders.forEach { it.join() }
        merged.close()
    }
    val arrivals = mutableListOf<String>()
    for (tick in merged) arrivals.add(tick)
    println("fan-in arrival order: $arrivals")

    // ── 6. PITFALL: forgetting to close ──────────────────────────────
    // A for-loop over a channel ends only when the channel is CLOSED.
    // Forget close() and the receiver waits forever: the program hangs.
    // produce { } closes for you, which is why it is preferred.
    //
    // The reverse hangs too: stop reading a produce { } early and its
    // coroutine stays suspended in send(), keeping the enclosing scope
    // (here runBlocking) alive forever. Cancel what you no longer need:
    val endless = numbers(Int.MAX_VALUE)
    val first = endless.receive()
    val second = endless.receive()
    endless.cancel() // without this line, main() would never return
    println("early exit: read $first, $second then cancelled the rest")

    // ── 7. WHY channels? ─────────────────────────────────────────────
    // "Don't communicate by sharing memory; share memory by
    // communicating." Each value has one owner at a time, so there is
    // no shared mutable state to protect (compare lesson13_shared_state).
    // For streams of values with operators like map/filter, level 10
    // (Flow) is usually the better tool.

    // ── 8. TRY IT ────────────────────────────────────────────────────
    // a) In section 1, use Channel<String>(Channel.UNLIMITED). How does
    //    the order of "send" and "receive" lines change?
    // b) Add a third stage to the pipeline that keeps only even squares.
}
