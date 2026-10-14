package l09_coroutines.lesson12_context

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * =====================================================================
 *  LESSON 09.12 — The CoroutineContext
 * =====================================================================
 *  GOAL   : See what a coroutine context is made of (Job, dispatcher,
 *           name, handler), combine elements with `+`, and understand
 *           what a child inherits from its parent.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.lesson12_context.CoroutineContextBasicsKt
 *  OUTPUT :
 *           I am: main
 *           empty context: name=-, dispatcher=-, hasJob=false
 *           combined: name=worker, dispatcher=Dispatchers.Default, hasJob=false
 *           after + CoroutineName: name=renamed, dispatcher=Dispatchers.Default, hasJob=false
 *           minusKey(CoroutineName): name=-, dispatcher=Dispatchers.Default, hasJob=false
 *           child inherits the name: main
 *           child with its own name: downloader
 *           parent has an active child: true
 *           after join, active children: 0
 *           inside withContext: io-step
 *           back outside: main
 *  NEXT   : lesson13_shared_state/SharedState.kt
 * =====================================================================
 */

private const val WORK_MS = 50L

/** Reads the name element of the CURRENT coroutine's context. */
suspend fun myName(): String = currentCoroutineContext()[CoroutineName]?.name ?: "(no name)"

fun describe(context: CoroutineContext): String {
    val name = context[CoroutineName]?.name ?: "-"
    val dispatcher = context[ContinuationInterceptor] ?: "-"
    val hasJob = context[Job] != null
    return "name=$name, dispatcher=$dispatcher, hasJob=$hasJob"
}

fun main() = runBlocking(CoroutineName("main")) {
    // ── 1. CONCEPT: a context is a small map of ELEMENTS ─────────────
    // Each element type has a key: context[Job], context[CoroutineName],
    // context[ContinuationInterceptor] (the dispatcher), and so on.
    println("I am: ${myName()}")
    println("empty context: ${describe(EmptyCoroutineContext)}")

    // ── 2. CONCEPT: combine with + ───────────────────────────────────
    val workerContext = CoroutineName("worker") + Dispatchers.Default
    println("combined: ${describe(workerContext)}")

    // An element with the same key REPLACES the old one (right side wins):
    val renamed = workerContext + CoroutineName("renamed")
    println("after + CoroutineName: ${describe(renamed)}")
    println("minusKey(CoroutineName): ${describe(renamed.minusKey(CoroutineName))}")

    // ── 3. CONCEPT: children inherit, and may override ───────────────
    val parentJob = coroutineContext[Job]
    launch {
        println("child inherits the name: ${myName()}")
    }.join()
    launch(CoroutineName("downloader")) {
        println("child with its own name: ${myName()}")
        delay(WORK_MS)
    }.join()

    // The Job is never inherited as-is: every coroutine gets a NEW Job
    // whose parent is the Job of the scope that launched it.
    val child = launch { delay(WORK_MS) }
    println("parent has an active child: ${parentJob?.children?.any { it.isActive }}")
    child.join()
    println("after join, active children: ${parentJob?.children?.count { it.isActive }}")

    // ── 4. CONCEPT: withContext switches elements for a block ────────
    withContext(CoroutineName("io-step") + Dispatchers.IO) {
        println("inside withContext: ${myName()}")
    }
    println("back outside: ${myName()}")

    // ── 5. PITFALL: passing a Job breaks the family tree ─────────────
    // launch(Job()) { ... } makes the new coroutine a child of THAT fresh
    // Job, not of this scope. The scope no longer waits for it or
    // cancels it: structured concurrency (lesson08) is lost. Pass names
    // and dispatchers freely, but avoid passing a Job. The most common
    // version of this mistake is launch(SupervisorJob()) or
    // withContext(SupervisorJob()), hoping for supervisor behaviour: use
    // supervisorScope { } instead (lesson11_exceptions).

    // ── 6. WHY a context? ────────────────────────────────────────────
    // It carries "how and where" a coroutine runs without adding
    // parameters to every suspend function: the dispatcher picks the
    // thread, the Job handles cancellation, the name helps debugging.

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Print describe(coroutineContext) inside a launch(Dispatchers.Default).
    // b) Run with the JVM option -Dkotlinx.coroutines.debug and print
    //    Thread.currentThread().name inside a named coroutine.
}
