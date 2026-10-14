package l09_coroutines.lesson13_shared_state

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

/**
 * =====================================================================
 *  LESSON 09.13 — Shared mutable state
 * =====================================================================
 *  GOAL   : See a race condition happen, then fix it three ways:
 *           a Mutex, an atomic variable, and thread confinement.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.lesson13_shared_state.SharedStateKt
 *  OUTPUT : (example output; the unsafe count changes on every run)
 *           unsafe counter: 40331 (expected 100000)
 *           mutex counter : 100000
 *           atomic counter: 100000
 *           confined counter: 100000
 *  NEXT   : lesson14_channels/Channels.kt
 * =====================================================================
 */

private const val COROUTINES = 100
private const val INCREMENTS_EACH = 1_000
private const val EXPECTED = COROUTINES * INCREMENTS_EACH

/** Starts [COROUTINES] coroutines on several threads, each running [action] [INCREMENTS_EACH] times. */
suspend fun massiveRun(action: suspend () -> Unit) {
    withContext(Dispatchers.Default) { // several threads at once
        repeat(COROUTINES) {
            launch { repeat(INCREMENTS_EACH) { action() } }
        }
    } // withContext waits for all its children
}

fun main() = runBlocking {
    // ── 1. PITFALL: a race condition ─────────────────────────────────
    // counter++ is really three steps: READ, ADD 1, WRITE. Two threads
    // can read the same old value, and one of the updates is lost.
    var unsafeCounter = 0
    massiveRun { unsafeCounter++ }
    println("unsafe counter: $unsafeCounter (expected $EXPECTED)")

    // ── 2. FIX 1: a Mutex (mutual exclusion lock) ────────────────────
    // Only one coroutine at a time may run the code inside withLock.
    // Unlike a Java lock, waiting for a Mutex SUSPENDS instead of
    // blocking the thread.
    val mutex = Mutex()
    var lockedCounter = 0
    massiveRun { mutex.withLock { lockedCounter++ } }
    println("mutex counter : $lockedCounter")
    // PITFALL: a Mutex is NOT reentrant. Locking it again while you
    // already hold it waits for yourself forever (a deadlock):
    //   mutex.withLock {
    //       mutex.withLock { lockedCounter++ } // hangs: nobody will unlock
    //   }
    // Keep locked sections small and never call code that might lock
    // the same mutex from inside them.

    // ── 3. FIX 2: an atomic variable ─────────────────────────────────
    // AtomicInteger (from Java) does read-add-write as ONE indivisible
    // step. Fastest choice for a single number or reference.
    val atomicCounter = AtomicInteger(0)
    massiveRun { atomicCounter.incrementAndGet() }
    println("atomic counter: ${atomicCounter.get()}")

    // ── 4. FIX 3: confinement - one task at a time ───────────────────
    // limitedParallelism(1) gives a view of the dispatcher that runs at
    // most ONE task at a time. All updates happen one after another, and
    // each task sees the previous one's writes. The tasks may still run
    // on DIFFERENT threads, just never at the same moment. (For one fixed
    // thread you would use newSingleThreadContext, which you must close.)
    val singleLane = Dispatchers.Default.limitedParallelism(1)
    var confinedCounter = 0
    massiveRun { withContext(singleLane) { confinedCounter++ } }
    println("confined counter: $confinedCounter")

    // ── 5. WHY so many options? ──────────────────────────────────────
    // - Atomic: one simple value (counter, flag). Fastest.
    // - Mutex: several values that must change together.
    // - Confinement: a whole object owned by one "lane"; good for
    //   coarse-grained work (confine the WHOLE task, not each ++).
    // - Best of all: no shared mutable state. Let each coroutine return
    //   its result (async/await) and combine the results at the end.

    // ── 6. PITFALL: @Volatile is not enough ──────────────────────────
    // @Volatile makes every thread SEE the latest value, but counter++
    // is still three steps, so updates are still lost.

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Run the lesson several times. Does the unsafe count change?
    // b) Rewrite massiveRun so each coroutine counts locally and returns
    //    its count with async; sum the results. Is a lock still needed?
}
