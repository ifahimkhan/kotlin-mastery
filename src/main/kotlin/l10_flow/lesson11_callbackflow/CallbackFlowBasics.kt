package l10_flow.lesson11_callbackflow

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * =====================================================================
 *  LESSON 10.11 — callbackFlow and channelFlow
 * =====================================================================
 *  GOAL   : Turn a callback-based API into a Flow with callbackFlow,
 *           clean up with awaitClose, and emit from several coroutines
 *           at once with channelFlow.
 *  RUN    : ./gradlew -q run -Plesson=l10_flow.lesson11_callbackflow.CallbackFlowBasicsKt
 *  OUTPUT :
 *           api: listener registered
 *           api: listener unregistered
 *           first 3 readings: [10, 11, 12]
 *           listeners left: 0
 *           api: listener registered
 *           api: listener unregistered
 *           sensor failed: sensor unplugged
 *           channelFlow arrival order: [fast, slow]
 *  NEXT   : lesson12_testing_flows/TestingFlows.kt
 * =====================================================================
 */

private const val SLOW_SOURCE_MS = 200L
private const val FIRST_READING = 10
private const val READINGS_WANTED = 3

/** The kind of callback interface many older Java/Android libraries use. */
interface SensorListener {
    fun onReading(value: Int)
    fun onError(error: Exception)
}

/** A fake sensor library: you register a listener and it calls you back. */
class FakeSensorApi {
    private val listeners = mutableListOf<SensorListener>()
    private val _listenerCount = MutableStateFlow(0)
    val listenerCount: StateFlow<Int> = _listenerCount.asStateFlow()

    fun register(listener: SensorListener) {
        listeners += listener
        _listenerCount.update { it + 1 }
        println("api: listener registered")
    }

    fun unregister(listener: SensorListener) {
        listeners -= listener
        _listenerCount.update { it - 1 }
        println("api: listener unregistered")
    }

    fun push(value: Int) = listeners.toList().forEach { it.onReading(value) }

    fun fail(message: String) = listeners.toList().forEach { it.onError(IllegalStateException(message)) }
}

/**
 * callbackFlow builds a flow backed by a channel: the callbacks call
 * trySend (they are not suspend functions, so they can't call send).
 */
fun FakeSensorApi.readings(): Flow<Int> = callbackFlow {
    val listener = object : SensorListener { // object expression: l05_oop/lesson08_objects
        override fun onReading(value: Int) {
            trySend(value) // returns a result instead of suspending; fails if the flow is closed
        }

        override fun onError(error: Exception) {
            close(error) // ends the flow with this exception
        }
    }
    register(listener)
    // awaitClose suspends until the flow is closed or the collector stops,
    // then runs the cleanup. Forget it and callbackFlow throws
    // IllegalStateException: "'awaitClose { yourCallbackOrListener.cancel() }'
    // should be used in the end of callbackFlow block."
    awaitClose { unregister(listener) }
}

fun main() =
    runBlocking {
        // ── 1. CONCEPT: wrap a callback API ──────────────────────────────
        val api = FakeSensorApi()
        launch {
            api.listenerCount.first { it > 0 } // handshake: wait for the listener
            for (value in FIRST_READING until FIRST_READING + READINGS_WANTED + 2) api.push(value)
        }
        // take(3) stops collecting early -> the flow is cancelled -> awaitClose
        // runs the cleanup BEFORE toList() returns.
        val readings = api.readings().take(READINGS_WANTED).toList()
        println("first $READINGS_WANTED readings: $readings")
        println("listeners left: ${api.listenerCount.value}")

        // ── 2. CONCEPT: errors from callbacks ────────────────────────────
        // close(error) ends the flow with that exception; the collector sees
        // it like any flow error (lesson07_errors_completion).
        launch {
            api.listenerCount.first { it > 0 }
            api.fail("sensor unplugged")
        }
        try {
            api.readings().collect { }
        } catch (e: IllegalStateException) {
            println("sensor failed: ${e.message}")
        }

        // ── 3. CONCEPT: channelFlow - emit from many coroutines ──────────
        // A plain flow { } may only emit from its own coroutine. Calling
        // emit inside a launch { } there fails at run time with
        // IllegalStateException ("Flow invariant is violated").
        // channelFlow allows it: send() from any child coroutine.
        val merged = channelFlow {
            launch {
                delay(SLOW_SOURCE_MS)
                send("slow")
            }
            launch { send("fast") }
        }
        println("channelFlow arrival order: ${merged.toList()}")

        // ── 4. WHY: choosing the buffer ──────────────────────────────────
        // callbackFlow/channelFlow use a buffer of 64 by default. If the
        // callback fires faster than you collect, trySend starts to FAIL
        // (values are dropped) once the buffer is full. Pick on purpose:
        //   .buffer(Channel.UNLIMITED)   keep everything (memory risk)
        //   .buffer(Channel.CONFLATED)   keep only the latest (sensors, prices)
        // See latestReadings() below main for the conflated version.

        // ── 5. TRY IT ────────────────────────────────────────────────────
        // a) Remove the awaitClose { } line in readings(). Run it and read the
        //    exception message.
        // b) In section 1, remove the first { } handshake so the values are
        //    pushed before anyone listens. Why does toList() now wait forever?
        // c) Collect api.latestReadings() while pushing 1..100 with no delay.
        //    Which values arrive?
    }

/** A sensor stream that keeps only the newest unread value. */
fun FakeSensorApi.latestReadings(): Flow<Int> = readings().buffer(Channel.CONFLATED)
