package l10_flow.lesson07_errors_completion

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.runBlocking
import java.io.IOException

/**
 * =====================================================================
 *  LESSON 10.7 — Errors and completion
 * =====================================================================
 *  GOAL   : Handle failures in a flow the declarative way: catch
 *           (upstream only), retry/retryWhen, onCompletion and onEmpty.
 *           Learn the one rule you must never break: exception
 *           transparency.
 *  RUN    : ./gradlew -q run -Plesson=l10_flow.lesson07_errors_completion.ErrorsAndCompletionKt
 *  OUTPUT :
 *           got 1
 *           got 2
 *           try/catch around collect: sensor broke
 *           value 1
 *           value 2
 *           catch saw: sensor broke
 *           value -1
 *           collected 1
 *           downstream error escapes catch: collector rejected 2
 *           onEach 1
 *           catch now sees it: collector rejected 2
 *           catch around emit breaks the rules: true
 *           retrying after: attempt 1 failed
 *           retrying after: attempt 2 failed
 *           data on attempt 3
 *           retryWhen attempt 0: always down
 *           retryWhen attempt 1: always down
 *           gave up: always down
 *           completed normally, cause = null
 *           completed with error: sensor broke
 *           onEmpty gave 0
 *  NEXT   : lesson08_stateflow/StateFlowBasics.kt
 * =====================================================================
 */

private const val RETRY_LIMIT = 5L
private const val FIRST_SUCCESSFUL_ATTEMPT = 3
private const val RETRY_WHEN_MAX_ATTEMPTS = 2L
private const val FALLBACK = -1
private const val COLLECTOR_LIMIT = 2

/** Emits two readings, then fails - like a sensor that breaks mid-stream. */
fun readingsThenBoom(): Flow<Int> = flow {
    emit(1)
    emit(2)
    throw IllegalStateException("sensor broke")
}

fun main() =
    runBlocking {
        // ── 1. CONCEPT: an exception travels to the collector ────────────
        // A failing flow throws out of collect(), like any suspend function.
        // You CAN wrap collect in try/catch (lesson l09_coroutines/lesson11_exceptions)...
        try {
            readingsThenBoom().collect { println("got $it") }
        } catch (e: IllegalStateException) {
            println("try/catch around collect: ${e.message}")
        }

        // ── 2. CONCEPT: the catch operator ───────────────────────────────
        // ...but the flow way is the catch operator. It sees errors from
        // UPSTREAM (everything above it) and may emit a fallback value.
        readingsThenBoom()
            .catch { e ->
                println("catch saw: ${e.message}")
                emit(FALLBACK)
            }
            .collect { println("value $it") }

        // ── 3. PITFALL: catch does not see DOWNSTREAM errors ─────────────
        // An exception thrown inside collect { } is BELOW catch, so catch
        // never sees it. It escapes to the caller.
        try {
            flowOf(1, 2, 3)
                .catch { println("never printed") }
                .collect { value ->
                    check(value < COLLECTOR_LIMIT) { "collector rejected $value" }
                    println("collected $value")
                }
        } catch (e: IllegalStateException) {
            println("downstream error escapes catch: ${e.message}")
        }

        // The fix: move the work into onEach (upstream of catch) and call
        // collect() with no lambda.
        flowOf(1, 2, 3)
            .onEach { value ->
                check(value < COLLECTOR_LIMIT) { "collector rejected $value" }
                println("onEach $value")
            }
            .catch { e -> println("catch now sees it: ${e.message}") }
            .collect()

        // ── 4. PITFALL: exception transparency ───────────────────────────
        // RULE: never wrap emit() in try/catch inside flow { }. emit() runs
        // the DOWNSTREAM code; catching its exception hides the collector's
        // failure from the collector. Kotlin detects emitting after that and
        // throws IllegalStateException("Flow exception transparency is violated...").
        val rudeFlow = flow {
            try {
                emit(1)
            } catch (e: IllegalStateException) {
                emit(FALLBACK) // BUG: emitting after the collector failed
            }
        }
        try {
            rudeFlow.collect { check(false) { "collector failed" } }
        } catch (e: IllegalStateException) {
            val violated = e.message.orEmpty().startsWith("Flow exception transparency is violated")
            println("catch around emit breaks the rules: $violated")
        }

        // ── 5. CONCEPT: retry and retryWhen ──────────────────────────────
        // retry(n) re-COLLECTS the upstream (a cold flow starts again from the
        // top) at most n more times; plain retry(3) retries any error. The
        // optional predicate decides which errors are worth retrying.
        // retry(n) requires n > 0: retry(0) throws IllegalArgumentException.
        // For "maybe zero retries" use retryWhen (below).
        var attempts = 0
        val flaky = flow {
            attempts++
            if (attempts < FIRST_SUCCESSFUL_ATTEMPT) throw IOException("attempt $attempts failed")
            emit("data on attempt $attempts")
        }
        flaky
            .retry(RETRY_LIMIT) { e ->
                println("retrying after: ${e.message}")
                e is IOException // only retry I/O problems
            }
            .collect { println(it) }

        // retryWhen also gets the attempt number (0, 1, 2...). Add delay()
        // inside it for backoff (l09_coroutines/lesson16_testing shows how to test that).
        flow<String> { throw IOException("always down") }
            .retryWhen { cause, attempt ->
                val again = attempt < RETRY_WHEN_MAX_ATTEMPTS
                if (again) println("retryWhen attempt $attempt: ${cause.message}")
                again
            }
            .catch { e -> println("gave up: ${e.message}") }
            .collect()

        // ── 6. CONCEPT: onCompletion and onEmpty ─────────────────────────
        // onCompletion runs at the end, like finally. Its cause is null on
        // success and the exception on failure. It does NOT handle the error:
        // that is still catch's job.
        flowOf(1, 2)
            .onCompletion { cause -> println("completed normally, cause = $cause") }
            .collect()
        readingsThenBoom()
            .onCompletion { cause -> println("completed with error: ${cause?.message}") }
            .catch { } // handled (silently) so main can go on
            .collect()

        // onEmpty runs when a flow completes without emitting anything.
        emptyFlow<Int>()
            .onEmpty { emit(0) }
            .collect { println("onEmpty gave $it") }

        // ── 7. WHY declarative? ──────────────────────────────────────────
        // catch / retry / onCompletion keep error handling IN the pipeline,
        // next to the code that fails. The collector stays simple, and the
        // same flow can be reused by collectors that handle errors differently.
        // Like try/catch in coroutines: never swallow CancellationException.
        // catch never handles the cancellation of the collecting coroutine, so
        // cancelling a collector still stops it.

        // ── 8. TRY IT ────────────────────────────────────────────────────
        // a) In section 2, remove emit(FALLBACK). What does the collector print?
        // b) Change the retry predicate in section 5 to `false`. What happens?
        // c) Put onCompletion BELOW catch in section 6. Is cause still the error?
    }
