package l10_flow.lesson05_buffering_timing

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

/**
 * =====================================================================
 *  LESSON 10.5 — Buffering and timing: slow collectors, fast producers
 * =====================================================================
 *  GOAL   : Decide what happens when values arrive faster than the
 *           collector can handle them: wait (default), buffer, keep
 *           only the latest (conflate), restart (collectLatest), or
 *           wait for a pause (debounce).
 *  RUN    : ./gradlew -q run -Plesson=l10_flow.lesson05_buffering_timing.BufferingAndTimingKt
 *  OUTPUT :
 *           plain:  produce 1, process 1, produce 2, process 2, produce 3, process 3
 *           buffer: produce 1, produce 2, produce 3, process 1, process 2, process 3
 *           conflate processed: [1, 5]
 *           collectLatest started 1
 *           collectLatest started 2
 *           collectLatest started 3
 *           collectLatest finished 3
 *           debounce: [kot, kotlin]
 *  NEXT   : lesson06_combining/Combining.kt
 * =====================================================================
 */

private const val PROCESS_MILLIS = 100L // a "slow" collector
private const val EMIT_GAP_MILLIS = 200L // time between values in section 4
private const val WORK_MILLIS = 600L // work per value in section 4
private const val TYPING_GAP_MILLIS = 100L // fast typing in section 5
private const val PAUSE_MILLIS = 600L // the user stops typing
private const val DEBOUNCE_MILLIS = 300L

// Emits 1..count with NO delay and logs each emit. The collector decides
// how fast things really go.
fun fastProducer(
    count: Int,
    log: MutableList<String>,
): Flow<Int> =
    flow {
        for (i in 1..count) {
            log += "produce $i"
            emit(i)
        }
    }

// Someone typing "kot", pausing, then finishing "kotlin".
fun typing(): Flow<String> =
    flow {
        emit("k")
        delay(TYPING_GAP_MILLIS)
        emit("ko")
        delay(TYPING_GAP_MILLIS)
        emit("kot")
        delay(PAUSE_MILLIS)
        emit("kotlin")
    }

@OptIn(FlowPreview::class) // debounce is still marked as a preview API
fun main() =
    runBlocking {
        // ── 1. CONCEPT: by default, producer and collector take turns ────
        // emit() suspends until the collector has finished with the value.
        // So a slow collector slows the producer down: they alternate.
        val plainLog = mutableListOf<String>()
        fastProducer(3, plainLog).collect { value ->
            plainLog += "process $value"
            delay(PROCESS_MILLIS)
        }
        println("plain:  ${plainLog.joinToString()}")

        // ── 2. CONCEPT: buffer = producer runs ahead ─────────────────────
        // buffer() runs the producer in its own coroutine and puts values in
        // a queue (a channel, l09_coroutines/lesson14_channels). The
        // producer no longer waits: it emits everything, then the collector
        // works through the queue. (runBlocking has ONE thread, so the
        // producer runs until it has to wait; on a multi-threaded
        // dispatcher the log could interleave differently - and conflate
        // below might even give just [5].)
        val bufferLog = mutableListOf<String>()
        fastProducer(3, bufferLog).buffer().collect { value ->
            bufferLog += "process $value"
            delay(PROCESS_MILLIS)
        }
        println("buffer: ${bufferLog.joinToString()}")

        // ── 3. CONCEPT: conflate = only the LATEST value matters ─────────
        // While the collector is busy, new values overwrite each other; it
        // gets the newest one when it is ready. Values in between are
        // dropped. Good for "current price", "current position".
        val processed = mutableListOf<Int>()
        fastProducer(5, mutableListOf()).conflate().collect { value ->
            processed += value
            delay(PROCESS_MILLIS)
        }
        println("conflate processed: $processed")

        // ── 4. CONCEPT: collectLatest = cancel the old work ──────────────
        // A new value CANCELS the work still running for the previous one
        // and starts again. Only the last value's work finishes.
        // Values come every 200 ms, the work takes 600 ms, so each new
        // value arrives long before the old work could finish.
        flow {
            emit(1)
            delay(EMIT_GAP_MILLIS)
            emit(2)
            delay(EMIT_GAP_MILLIS)
            emit(3)
        }.collectLatest { value ->
            println("collectLatest started $value")
            delay(WORK_MILLIS) // cancelled here when the next value arrives
            println("collectLatest finished $value")
        }

        // ── 5. CONCEPT: debounce = wait for a pause ──────────────────────
        // debounce(300) only lets a value through if no newer value comes
        // within 300 ms. "k", "ko" are replaced quickly; "kot" survives the
        // pause; the last value always gets through when the flow ends.
        // This is how "search as you type" avoids a request per key press.
        println("debounce: ${typing().debounce(DEBOUNCE_MILLIS).toList()}")

        // sample(ms) is a cousin: every ms milliseconds it emits the latest
        // value seen so far (good for "update the screen 10 times a second").

        // ── 6. WHY: pick by what a value MEANS ───────────────────────────
        // every value matters, producer is bursty  -> buffer
        // only the newest value matters             -> conflate
        // new value makes old work useless          -> collectLatest
        // react only after the user stops typing    -> debounce
        // Default (no operator): simplest, and nothing is ever dropped.

        // ── 7. PITFALL: conflate and collectLatest DROP data ─────────────
        // Never use them for things that must all be handled, like payments
        // or chat messages. Use buffer (or no operator) for those.

        // ── 8. TRY IT ────────────────────────────────────────────────────
        // a) In section 2 use buffer(1). How does the log change?
        // b) In section 4 make WORK_MILLIS 100. Which values finish now?
        // c) In section 5 change DEBOUNCE_MILLIS to 50. What gets through?
    }
