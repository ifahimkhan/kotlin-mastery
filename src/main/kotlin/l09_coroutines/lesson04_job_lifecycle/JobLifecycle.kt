package l09_coroutines.lesson04_job_lifecycle

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * =====================================================================
 *  LESSON 09.4 — The Job and its lifecycle
 * =====================================================================
 *  GOAL   : Follow a Job through its states (New, Active, Completing,
 *           Completed, Cancelling, Cancelled), start work lazily, and
 *           see that a Job has children.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.lesson04_job_lifecycle.JobLifecycleKt
 *  OUTPUT :
 *           created LAZY  -> active=false completed=false cancelled=false
 *           after start() -> active=true completed=false cancelled=false
 *           after join()  -> active=false completed=true cancelled=false
 *           while waiting -> active=true completed=false cancelled=false
 *           after cancel() -> active=false completed=false cancelled=true
 *           after join()  -> active=false completed=true cancelled=true
 *           parent has 2 running children
 *           child B finished
 *           child A finished
 *           parent finished, children left: 0
 *  NEXT   : lesson05_async_await/AsyncAwait.kt
 * =====================================================================
 */

private const val WORK_MILLIS = 100L
private const val LONG_WORK_MILLIS = 10_000L
private const val SETTLE_MILLIS = 10L
private const val CHILD_A_MILLIS = 80L
private const val CHILD_B_MILLIS = 40L

private fun describe(label: String, job: Job) {
    println("$label -> active=${job.isActive} completed=${job.isCompleted} cancelled=${job.isCancelled}")
}

fun main() = runBlocking {
    // ── 1. CONCEPT: the states of a Job ──────────────────────────────
    //
    //   New --start--> Active --body done--> Completing --children done--> Completed
    //                    |
    //                  cancel / failure
    //                    v
    //               Cancelling --children done--> Cancelled (also "completed")
    //
    // You can't read the state directly, only these three flags.

    // ── 2. CONCEPT: start lazily ─────────────────────────────────────
    // CoroutineStart.LAZY creates the Job in the "New" state: nothing runs
    // until start() or join() is called.
    val lazyJob = launch(start = CoroutineStart.LAZY) { delay(WORK_MILLIS) }
    describe("created LAZY ", lazyJob)
    lazyJob.start()
    describe("after start()", lazyJob)
    lazyJob.join()
    describe("after join() ", lazyJob)

    // ── 3. CONCEPT: cancelling ───────────────────────────────────────
    val slowJob = launch { delay(LONG_WORK_MILLIS) }
    delay(SETTLE_MILLIS) // let slowJob start and reach its delay
    describe("while waiting", slowJob)
    slowJob.cancel()
    // "Cancelling": it was asked to stop but hasn't finished stopping yet.
    describe("after cancel()", slowJob)
    slowJob.join()
    // A cancelled Job counts as completed too (it's done, just not successfully).
    describe("after join() ", slowJob)

    // ── 4. CONCEPT: a Job has children ───────────────────────────────
    // Every launch inside another coroutine becomes its CHILD. A parent
    // is only completed when all its children are completed.
    val parent =
        launch {
            launch {
                delay(CHILD_A_MILLIS)
                println("child A finished")
            }
            launch {
                delay(CHILD_B_MILLIS)
                println("child B finished")
            }
        }
    delay(SETTLE_MILLIS) // the parent's body has run; both children are waiting
    println("parent has ${parent.children.count()} running children")
    parent.join() // waits for the children too
    println("parent finished, children left: ${parent.children.count()}")

    // ── 5. PITFALL: isActive after cancel is false immediately ───────
    // "Not active" doesn't mean "finished". Use join() (or the
    // cancelAndJoin() shortcut) when you must wait for the cleanup.

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Call lazyJob.join() without start(). Does it run?
    // b) Cancel `parent` right after the children start. Do they print?
    // c) Print slowJob.isCompleted right after cancel() without the join.
}
