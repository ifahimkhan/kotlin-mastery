package l09_coroutines.lesson09_cancellation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/**
 * =====================================================================
 *  LESSON 09.9 — Cancellation is cooperative
 * =====================================================================
 *  GOAL   : Stop coroutines you no longer need, make your own code
 *           respond to cancellation, clean up in `finally`, and never
 *           swallow a CancellationException.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.lesson09_cancellation.CancellationKt
 *  OUTPUT :
 *           downloading part 0
 *           downloading part 1
 *           downloading part 2
 *           main: cancelling the download
 *           main: isCancelled=true, isActive=false
 *           stubborn step 0
 *           stubborn step 1
 *           main: cancelling the stubborn job
 *           stubborn step 2
 *           stubborn step 3
 *           main: stubborn job finally ended
 *           polite step 0
 *           polite step 1
 *           main: cancelling the polite job
 *           polite step 2
 *           main: polite job stopped early
 *           cruncher: chunk 0 done
 *           main: cancelling the cruncher
 *           main: cruncher stopped at its next yield()
 *           editing line 0
 *           editing line 1
 *           editing line 2
 *           cleanup: closing the file
 *           cleanup: unsaved work written to disk
 *           main: saver is done, isActive=false
 *           swallower step 0 (isActive=true)
 *           main: cancelling the swallower
 *           swallower step 1 (isActive=false)
 *           swallower step 2 (isActive=false)
 *           swallower step 3 (isActive=false)
 *           main: swallower ran to the end anyway
 *           fixed step 0
 *           main: cancelling the fixed job
 *           main: fixed job stopped
 *  NEXT   : lesson10_timeouts/Timeouts.kt
 * =====================================================================
 */

private const val STEP_MS = 100L
private const val WORK_MS = 200L

// Lesson plumbing, not the topic: each worker reports "I reached step N"
// into a channel (lesson14_channels), and main waits for that report
// before cancelling. Guessing a time with delay() would make the printed
// order depend on how busy the computer is.
private fun progressChannel() = Channel<Int>(Channel.UNLIMITED)

private suspend fun Channel<Int>.awaitStep(step: Int) {
    do {
        val reached = receive()
    } while (reached != step)
}

fun main() = runBlocking {
    // ── 1. CONCEPT: cancel() + join() ────────────────────────────────
    // delay() checks for cancellation, so this job stops right away.
    val downloadProgress = progressChannel()
    val job = launch {
        repeat(10) { i ->
            println("downloading part $i")
            downloadProgress.trySend(i)
            delay(STEP_MS)
        }
    }
    downloadProgress.awaitStep(2) // part 2 printed; the job now sits in delay()
    println("main: cancelling the download")
    job.cancelAndJoin() // = cancel() + join(): ask it to stop, then wait until it has
    println("main: isCancelled=${job.isCancelled}, isActive=${job.isActive}")

    // ── 2. PITFALL: code that never checks ───────────────────────────
    // Cancellation only sets a flag. Code that never suspends and never
    // looks at the flag keeps running. Thread.sleep stands in for heavy
    // CPU work here: it blocks the thread and ignores coroutines.
    val stubbornProgress = progressChannel()
    val stubborn = launch(Dispatchers.Default) {
        repeat(4) { i ->
            stubbornProgress.trySend(i) // "starting step i"
            Thread.sleep(WORK_MS)
            println("stubborn step $i")
        }
    }
    stubbornProgress.awaitStep(2) // steps 0 and 1 printed; step 2 is running
    println("main: cancelling the stubborn job")
    stubborn.cancelAndJoin() // waits for ALL 4 steps: cancel was ignored
    println("main: stubborn job finally ended")

    // ── 3. FIX: check the flag yourself ──────────────────────────────
    // ensureActive() throws CancellationException if the job was cancelled.
    // Alternatives: `if (!isActive) return@launch`, or yield() (section 4).
    val politeProgress = progressChannel()
    val polite = launch(Dispatchers.Default) {
        repeat(4) { i ->
            ensureActive()
            politeProgress.trySend(i) // passed the check, starting step i
            Thread.sleep(WORK_MS)
            println("polite step $i")
        }
    }
    politeProgress.awaitStep(2) // step 2 already passed its check...
    println("main: cancelling the polite job") // ...so it finishes, then step 3's check stops it
    polite.cancelAndJoin()
    println("main: polite job stopped early")

    // ── 4. FIX on ONE thread: yield() ────────────────────────────────
    // runBlocking runs everything on a single thread. A loop that never
    // suspends would keep that thread forever, so main could not even
    // call cancel(). yield() is a suspension point: it lets other
    // coroutines run AND checks for cancellation (it throws if cancelled).
    val cruncher = launch {
        repeat(4) { chunk ->
            println("cruncher: chunk $chunk done")
            yield() // let others run; stop here if cancelled
        }
    }
    yield() // main steps aside so the cruncher can do one chunk
    println("main: cancelling the cruncher")
    cruncher.cancelAndJoin()
    println("main: cruncher stopped at its next yield()")

    // ── 5. CONCEPT: cleanup with finally ─────────────────────────────
    // Cancellation is delivered as a CancellationException, so `finally`
    // runs. (try/catch/finally are explained in lesson11_exceptions.)
    // Inside finally the job is already cancelled: calling a suspending
    // function like delay() there would throw at once. Wrap such cleanup
    // in withContext(NonCancellable).
    val saverProgress = progressChannel()
    val saver = launch {
        try {
            repeat(10) { i ->
                println("editing line $i")
                saverProgress.trySend(i)
                delay(STEP_MS)
            }
        } finally {
            println("cleanup: closing the file")
            withContext(NonCancellable) {
                delay(STEP_MS / 2) // a suspending save, allowed here
                println("cleanup: unsaved work written to disk")
            }
        }
    }
    saverProgress.awaitStep(2)
    saver.cancelAndJoin()
    println("main: saver is done, isActive=${saver.isActive}")

    // ── 6. PITFALL: swallowing CancellationException ─────────────────
    // runCatching catches EVERY exception, including the one that
    // cancellation uses to stop you. The loop below keeps going.
    val swallowerProgress = progressChannel()
    val swallower = launch {
        repeat(4) { i ->
            if (i > 0) runCatching { delay(STEP_MS) } // BUG: hides cancellation
            println("swallower step $i (isActive=$isActive)")
            swallowerProgress.trySend(i)
        }
    }
    swallowerProgress.awaitStep(0)
    println("main: cancelling the swallower")
    swallower.cancelAndJoin()
    println("main: swallower ran to the end anyway")

    // FIX: rethrow it (or catch only the exceptions you expect).
    val fixedProgress = progressChannel()
    val fixed = launch {
        repeat(4) { i ->
            if (i > 0) {
                runCatching { delay(STEP_MS) }
                    .onFailure { error -> if (error is CancellationException) throw error }
            }
            println("fixed step $i")
            fixedProgress.trySend(i)
        }
    }
    fixedProgress.awaitStep(0)
    println("main: cancelling the fixed job")
    fixed.cancelAndJoin()
    println("main: fixed job stopped")

    // ── 7. WHY cooperative? ──────────────────────────────────────────
    // Stopping a thread at a random instruction could leave data half
    // written. Coroutines stop only at safe points: suspension points and
    // explicit checks. That is why YOUR long loops must check too.

    // ── 8. TRY IT ────────────────────────────────────────────────────
    // a) In section 2, replace Thread.sleep with delay. What changes?
    // b) In section 5, remove withContext(NonCancellable). What prints?
    // c) Replace ensureActive() with `if (!isActive) return@launch`.
}
