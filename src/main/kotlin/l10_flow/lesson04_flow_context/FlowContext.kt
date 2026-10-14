package l10_flow.lesson04_flow_context

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * =====================================================================
 *  LESSON 10.4 — Flow context: where does a flow run?
 * =====================================================================
 *  GOAL   : Know that a flow runs in the COLLECTOR's coroutine, move
 *           the upstream work to another dispatcher with flowOn, and
 *           avoid the "Flow invariant is violated" error.
 *  RUN    : ./gradlew -q run -Plesson=l10_flow.lesson04_flow_context.FlowContextKt
 *  OUTPUT :
 *           no flowOn: flow body ran on the collector's thread: true
 *           before flowOn, emit ran on the main thread: false
 *           before flowOn, map ran on the main thread: false
 *           after flowOn, map ran on the main thread: true
 *           collector ran on the main thread: true
 *           emit inside withContext -> IllegalStateException
 *           message starts with: Flow invariant is violated:
 *  NEXT   : lesson05_buffering_timing/BufferingAndTiming.kt
 * =====================================================================
 */

fun main() =
    runBlocking {
        // runBlocking runs on the main thread. We compare Thread OBJECTS
        // (===), not names: names change in debug mode.
        val mainThread = Thread.currentThread()

        fun onMain(): Boolean = Thread.currentThread() === mainThread

        // ── 1. CONCEPT: context preservation ─────────────────────────────
        // A flow has no thread of its own. Its code runs in the coroutine
        // that collects it — here, runBlocking on the main thread.
        flow { emit(onMain()) }
            .collect { println("no flowOn: flow body ran on the collector's thread: $it") }

        // ── 2. CONCEPT: flowOn moves the UPSTREAM ────────────────────────
        // flowOn(dispatcher) changes where everything ABOVE it runs. What is
        // below it (later operators and the collector) stays where it was.
        // Each stage records "was I on the main thread?" in the value itself.
        flow { emit(onMain()) } // runs on Default (above flowOn)
            .map { emitOnMain -> emitOnMain to onMain() } // also above: Default
            .flowOn(Dispatchers.Default)
            .map { (emitOnMain, firstMapOnMain) -> Triple(emitOnMain, firstMapOnMain, onMain()) } // below: main
            .collect { (emitOnMain, firstMapOnMain, secondMapOnMain) ->
                println("before flowOn, emit ran on the main thread: $emitOnMain")
                println("before flowOn, map ran on the main thread: $firstMapOnMain")
                println("after flowOn, map ran on the main thread: $secondMapOnMain")
                println("collector ran on the main thread: ${onMain()}")
            }

        // ── 3. WHY ───────────────────────────────────────────────────────
        // The code that BUILDS a flow should not care about threads. The
        // code that USES it decides: "do this heavy part on Default (or IO),
        // give me the results here". Same idea as withContext for one value
        // (l09_coroutines/lesson07_with_context), but for a whole stream.

        // ── 4. PITFALL: emit from another context ────────────────────────
        // Switching threads INSIDE flow { } with withContext and emitting
        // there breaks the rule above, so Flow throws at runtime.
        try {
            flow {
                withContext(Dispatchers.Default) {
                    emit(1) // emitting from the wrong coroutine
                }
            }.collect { }
        } catch (e: IllegalStateException) {
            println("emit inside withContext -> ${e::class.simpleName}")
            println("message starts with: ${e.message?.lineSequence()?.first()}")
        }
        // Fix: emit normally and add .flowOn(Dispatchers.Default) after the
        // flow. If you really need to emit from several coroutines, use
        // channelFlow (lesson11_callbackflow).

        // ── 5. TRY IT ────────────────────────────────────────────────────
        // a) Move .flowOn(Dispatchers.Default) to the very end, after the
        //    second map. Which lines change to false?
        // b) Add two flowOn calls with different dispatchers. Which one
        //    applies to which part of the chain?
        // c) Print e.message in full in section 4 and read the hint it gives.
    }
