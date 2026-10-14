package l09_coroutines.lesson02_suspend

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

/**
 * =====================================================================
 *  LESSON 09.2 — Suspend functions
 * =====================================================================
 *  GOAL   : Write `suspend` functions, know where they may be called,
 *           and see the difference between delay (suspends) and
 *           Thread.sleep (blocks).
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.lesson02_suspend.SuspendFunctionsKt
 *  OUTPUT :
 *           Loading profile...
 *           Profile: Ada (3 orders)
 *           ticker 0
 *           main: working (with delay)
 *           ticker 1
 *           ticker 2
 *           main: done
 *           Ticks during Thread.sleep: 0
 *           Ticks after a suspension point: 1
 *  NEXT   : lesson03_builders/CoroutineBuilders.kt
 * =====================================================================
 */

private const val NETWORK_MILLIS = 100L
private const val TICK_MILLIS = 100L
private const val TICK_COUNT = 3
// Main waits long enough that ticks 1 and 2 (at ~100 ms and ~200 ms)
// finish well before it wakes, even on a slow machine.
private const val WORK_MILLIS = 450L
private const val BLOCK_MILLIS = 200L

// ── A suspend function ────────────────────────────────────────────────
// `suspend` means: "this function may PAUSE in the middle (at delay,
// await, ...), and continue later". It looks like normal code.
suspend fun loadUserName(): String {
    delay(NETWORK_MILLIS) // pretend this is a network call
    return "Ada"
}

suspend fun loadOrderCount(user: String): Int {
    delay(NETWORK_MILLIS)
    return user.length // fake data: 3 orders for "Ada"
}

// A suspend function can call other suspend functions, top to bottom,
// with no callbacks. Each line waits for the previous one.
suspend fun loadProfile(): String {
    val name = loadUserName()
    val orders = loadOrderCount(name)
    return "$name ($orders orders)"
}

fun main() {
    // ── 1. CONCEPT: calling suspend functions ────────────────────────
    // A suspend function can only run inside a coroutine (or another
    // suspend function). runBlocking starts a coroutine for us (09.3).
    runBlocking {
        println("Loading profile...")
        println("Profile: ${loadProfile()}")
    }

    // ── 2. PITFALL: calling from a normal function ───────────────────
    //   fun notSuspending() {
    //       val user = loadUserName()
    //   }
    //   ❌ Suspend function 'suspend fun loadUserName(): String' can only be called from a coroutine or
    //      another suspend function.

    // ── 3. CONCEPT: delay suspends, the thread stays free ────────────
    // The ticker and main share one thread. While main is in delay, the
    // ticker gets to run.
    runBlocking {
        launch {
            repeat(TICK_COUNT) { tick ->
                println("ticker $tick")
                delay(TICK_MILLIS)
            }
        }
        yield() // let the ticker print "ticker 0" first (more on yield in lesson09_cancellation)
        println("main: working (with delay)")
        delay(WORK_MILLIS) // ticks 1 and 2 happen while main waits
        println("main: done")
    }

    // ── 4. PITFALL: Thread.sleep BLOCKS the thread ───────────────────
    // Thread.sleep keeps the thread busy doing nothing, so no other
    // coroutine on that thread can run. Never use it in coroutines.
    runBlocking {
        var ticks = 0
        launch { ticks++ } // scheduled, waiting for the thread to be free
        Thread.sleep(BLOCK_MILLIS) // blocks: the launched coroutine can't start
        println("Ticks during Thread.sleep: $ticks")
        yield() // a suspension point: now the waiting coroutine runs
        println("Ticks after a suspension point: $ticks")
    }

    // ── 5. WHY: how can a function pause? ────────────────────────────
    // The compiler rewrites every suspend function into a small state
    // machine and passes it a hidden extra parameter, a "Continuation":
    // an object that says "when you're done waiting, continue at step 2
    // with these local variables". delay registers that continuation with
    // a timer and RETURNS, freeing the thread. When the timer fires, the
    // continuation is resumed. That's all suspension is.

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Add `suspend fun loadAddress(user: String)` and use it in loadProfile.
    // b) Replace delay(WORK_MILLIS) in section 3 with Thread.sleep(WORK_MILLIS).
    //    How many ticker lines appear before "main: done" now?
    // c) Remove the `suspend` keyword from loadUserName and read the error.
}
