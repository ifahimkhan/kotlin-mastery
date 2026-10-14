package l10_flow.lesson10_statein_sharein

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

/**
 * =====================================================================
 *  LESSON 10.10 — stateIn and shareIn: making a cold flow hot
 * =====================================================================
 *  GOAL   : Share ONE upstream between many collectors with shareIn and
 *           stateIn, choose a SharingStarted strategy, and own (and
 *           cancel) the scope that keeps the sharing alive.
 *  RUN    : ./gradlew -q run -Plesson=l10_flow.lesson10_statein_sharein.StateInShareInKt
 *  OUTPUT :
 *           cold: 2 collectors -> upstream started 2 times
 *           stateIn: latest = 22, read by 2 callers -> upstream started 1 time
 *           shareIn: both got [20, 21, 22] -> upstream started 1 time
 *           Eagerly started before any collector: true
 *           Lazily started before any collector: false
 *           WhileSubscribed: start #1
 *           WhileSubscribed: stop (no collectors left)
 *           WhileSubscribed: start #2
 *           WhileSubscribed: stop (no collectors left)
 *  NEXT   : lesson11_callbackflow/CallbackFlowBasics.kt
 * =====================================================================
 */

private const val LAST_READING = 22
private const val READINGS = 3
private const val REPLAY_ALL = 3
private const val TICK_MS = 50L

/** A cold flow that counts how often it is started. */
class Thermometer {
    var starts = 0
        private set

    fun readings(): Flow<Int> = flow {
        starts++
        emit(20)
        emit(21)
        emit(LAST_READING)
    }
}

fun main() =
    runBlocking {
        // The scope that owns the sharing. It lives on runBlocking's single
        // thread, but with its own Job, so we decide when it ends.
        val sharingScope = CoroutineScope(coroutineContext + Job())

        // ── 1. CONCEPT: cold flows start once PER collector ──────────────
        val cold = Thermometer()
        cold.readings().first()
        cold.readings().first()
        println("cold: 2 collectors -> upstream started ${cold.starts} times")

        // ── 2. CONCEPT: stateIn - share as a StateFlow ───────────────────
        // stateIn(scope) starts the upstream once, keeps the LATEST value and
        // returns a StateFlow (lesson08_stateflow) that anyone can read.
        val forState = Thermometer()
        val temperature = forState.readings().stateIn(sharingScope)
        temperature.first { it == LAST_READING } // wait until the upstream is done
        val readBy = listOf(temperature.value, temperature.value)
        println(
            "stateIn: latest = ${readBy.first()}, read by ${readBy.size} callers -> " +
                "upstream started ${forState.starts} time",
        )

        // ── 3. CONCEPT: shareIn - share as a SharedFlow ──────────────────
        // shareIn keeps `replay` values for late collectors (lesson09_sharedflow).
        val forShare = Thermometer()
        val shared = forShare.readings().shareIn(sharingScope, SharingStarted.Lazily, replay = REPLAY_ALL)
        val first = shared.take(READINGS).toList()
        val second = shared.take(READINGS).toList()
        val same = if (first == second) "both got $first" else "they differ: $first vs $second"
        println("shareIn: $same -> upstream started ${forShare.starts} time")

        // ── 4. CONCEPT: WHEN does sharing start? ─────────────────────────
        // Eagerly          - right away, even with no collectors
        // Lazily           - at the first collector, then never stops
        // WhileSubscribed  - while at least one collector is there
        val eager = Thermometer()
        eager.readings().shareIn(sharingScope, SharingStarted.Eagerly)
        val lazy = Thermometer()
        lazy.readings().shareIn(sharingScope, SharingStarted.Lazily)
        yield() // give the sharing coroutines a chance to run
        println("Eagerly started before any collector: ${eager.starts > 0}")
        println("Lazily started before any collector: ${lazy.starts > 0}")

        // WhileSubscribed(stopTimeoutMillis = 0) stops the upstream as soon as
        // the last collector leaves, and starts it again for the next one.
        // (Apps often use WhileSubscribed(5_000) to survive short gaps.)
        val stopped = Channel<Unit>(Channel.UNLIMITED)
        var startCount = 0
        val ticker = flow {
            var n = 0
            while (true) {
                emit(n++)
                delay(TICK_MS)
            }
        }
            .onStart { println("WhileSubscribed: start #${++startCount}") }
            .onCompletion {
                println("WhileSubscribed: stop (no collectors left)")
                stopped.trySend(Unit) // trySend: we are being cancelled, so don't suspend
            }
            .shareIn(sharingScope, SharingStarted.WhileSubscribed())
        ticker.first()   // collector 1 comes and goes
        stopped.receive() // handshake: wait until the upstream really stopped
        ticker.first()   // collector 2 restarts it
        stopped.receive()

        // ── 5. PITFALL: who owns the sharing? ────────────────────────────
        // The upstream runs in sharingScope, not in the collectors. If you
        // never cancel that scope, the upstream can run forever (a leak).
        // In an app, use a scope that ends with the screen or service.
        sharingScope.cancel()

        // ── 6. WHY share? ────────────────────────────────────────────────
        // One database query, one network socket or one sensor, many
        // listeners. Without sharing, every collector would start its own.

        // ── 7. TRY IT ────────────────────────────────────────────────────
        // a) In section 3, use SharingStarted.Eagerly with replay = 1. What
        //    does each collector get now?
        // b) Create the scope with CoroutineScope(coroutineContext) (no Job())
        //    and remove sharingScope.cancel(). Why does main never end now?
    }
