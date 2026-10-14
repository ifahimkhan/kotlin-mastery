package l11_advanced.lesson02_result

/**
 * =====================================================================
 *  LESSON 11.2 — Result<T> and runCatching
 * =====================================================================
 *  GOAL   : Turn "may throw" into a value you can pass around: build a
 *           Result with runCatching, transform it with map/mapCatching,
 *           recover from failures, and unwrap it with fold/getOrElse.
 *           Know when a sealed error type is the better choice.
 *  RUN    : ./gradlew -q run -Plesson=l11_advanced.lesson02_result.ResultBasicsKt
 *  OUTPUT :
 *           ok: Success(42)
 *           bad: Failure(java.lang.NumberFormatException: For input string: "4x2")
 *           ok.isSuccess=true bad.isFailure=true
 *           ok.map { it * 2 } = Success(84)
 *           bad.map { it * 2 } = Failure(java.lang.NumberFormatException: For input string: "4x2")
 *           mapCatching catches the new error: ArithmeticException
 *           recover: 0
 *           fold: got 42 / fold: failed with NumberFormatException
 *           getOrElse: -1, getOrNull: null
 *           onSuccess saw 42
 *           onFailure saw NumberFormatException
 *           sealed: Parsed(value=7) | TooBig(limit=100) | NotANumber(text=abc)
 *  NEXT   : lesson03_class_delegation/ClassDelegation.kt
 * =====================================================================
 */

// ── Helpers for section 5: a sealed error type ──────────────────────
// Result only says "some Throwable happened". A sealed type names every
// expected outcome, and `when` forces callers to handle each one.
sealed interface ParseOutcome {
    data class Parsed(val value: Int) : ParseOutcome

    data class TooBig(val limit: Int) : ParseOutcome

    data class NotANumber(val text: String) : ParseOutcome
}

private const val LIMIT = 100
private const val FALLBACK = -1

private fun divide(
    value: Int,
    by: Int,
): Int = value / by // throws ArithmeticException when by == 0

fun parseSmall(text: String): ParseOutcome {
    val number = text.toIntOrNull() ?: return ParseOutcome.NotANumber(text)
    return if (number > LIMIT) ParseOutcome.TooBig(LIMIT) else ParseOutcome.Parsed(number)
}

fun describe(outcome: ParseOutcome): String =
    when (outcome) { // no else: the compiler checks every case is handled
        is ParseOutcome.Parsed -> "Parsed(value=${outcome.value})"
        is ParseOutcome.TooBig -> "TooBig(limit=${outcome.limit})"
        is ParseOutcome.NotANumber -> "NotANumber(text=${outcome.text})"
    }

fun main() {
    // ── 1. CONCEPT: runCatching turns an exception into a value ──────
    val ok: Result<Int> = runCatching { "42".toInt() }
    val bad: Result<Int> = runCatching { "4x2".toInt() }
    println("ok: $ok")
    println("bad: $bad")
    println("ok.isSuccess=${ok.isSuccess} bad.isFailure=${bad.isFailure}")

    // ── 2. CONCEPT: transform without unwrapping ─────────────────────
    // map runs only on success; a failure passes through untouched.
    println("ok.map { it * 2 } = ${ok.map { it * 2 }}")
    println("bad.map { it * 2 } = ${bad.map { it * 2 }}")
    // map does NOT catch exceptions thrown by its lambda; mapCatching does.
    val divided = ok.mapCatching { divide(it, by = 0) }
    println("mapCatching catches the new error: ${divided.exceptionOrNull()?.let { it::class.simpleName }}")

    // ── 3. CONCEPT: recover and unwrap ───────────────────────────────
    println("recover: ${bad.recover { 0 }.getOrThrow()}")
    // Same asymmetry as map/mapCatching: an exception thrown INSIDE
    // recover's lambda escapes; recoverCatching wraps it in a Failure.
    // (Result as a function's return type is legal since Kotlin 1.5, but
    // many style guides prefer a sealed result type in public APIs.)
    val okText = ok.fold(onSuccess = { "got $it" }, onFailure = { "failed with ${it::class.simpleName}" })
    val badText = bad.fold(onSuccess = { "got $it" }, onFailure = { "failed with ${it::class.simpleName}" })
    println("fold: $okText / fold: $badText")
    println("getOrElse: ${bad.getOrElse { FALLBACK }}, getOrNull: ${bad.getOrNull()}")

    // ── 4. CONCEPT: side effects that keep the Result ────────────────
    ok.onSuccess { println("onSuccess saw $it") }.onFailure { println("never printed") }
    bad.onSuccess { println("never printed") }.onFailure { println("onFailure saw ${it::class.simpleName}") }

    // ── 5. WHY: Result vs a sealed error type ────────────────────────
    // Result<T>: wrap code that MAY throw (parsing, I/O) at a boundary.
    // Sealed type: your own expected outcomes with names and data
    // (like l08_generics/practice/result Outcome<T, E>).
    val outcomes = listOf("7", "250", "abc").map { describe(parseSmall(it)) }
    println("sealed: ${outcomes.joinToString(" | ")}")

    // ── 6. PITFALL: runCatching catches EVERYTHING ───────────────────
    // It catches every Throwable: OutOfMemoryError, programming bugs, and
    // CancellationException. Inside a coroutine that silently breaks
    // cancellation (l09_coroutines/lesson09_cancellation). In suspend
    // code, catch the specific exception you expect instead, or rethrow:
    //   runCatching { work() }.onFailure { if (it is CancellationException) throw it }

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Chain: runCatching { "8".toInt() }.map { it * it }.getOrElse { 0 }
    // b) Make parseSmall reject negative numbers with a new sealed case.
    //    What does the compiler say about describe()?
    // c) Write safeDiv(a, b): Result<Int> with runCatching. Try b = 0.
}
