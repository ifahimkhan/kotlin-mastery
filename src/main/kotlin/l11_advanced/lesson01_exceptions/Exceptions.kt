package l11_advanced.lesson01_exceptions

/**
 * =====================================================================
 *  LESSON 11.1 — Exceptions in depth: try, catch, finally, use
 * =====================================================================
 *  GOAL   : Use try/catch/finally with confidence: try as an expression,
 *           finally that always runs, throw as Nothing, your own
 *           exception classes, and `use {}` to close resources safely.
 *           (Earlier lessons said "try/catch: later level". This is it.)
 *  RUN    : ./gradlew -q run -Plesson=l11_advanced.lesson01_exceptions.ExceptionsKt
 *  OUTPUT :
 *           parse("42") = 42, parse("x") = -1
 *           try block
 *           finally runs (no error)
 *           try block
 *           caught: IllegalStateException
 *           finally runs (after an error)
 *           finally even runs on return: result=in-try, steps=[try, finally]
 *           parsePositive("7") = 7
 *           parsePositive("x") -> not a number: x
 *           requireAge(-3) -> IllegalArgumentException: age must be >= 0, was -3
 *           withdraw(500) -> InsufficientFundsException: need 500, have 100
 *           caught by the base class: InsufficientFundsException
 *           opened db
 *           opened file
 *           using both
 *           closed file
 *           closed db
 *           opened reader
 *           closed reader
 *           use{} closed the reader, then the error escaped: IllegalStateException
 *           body error wins: work failed, suppressed = [close failed]
 *           only close() failed: close failed
 *  NEXT   : lesson02_result/ResultBasics.kt
 * =====================================================================
 */

// ── Helpers for section 4: your own exception hierarchy ─────────────
// Extend Exception (or RuntimeException). A common base class lets
// callers catch "anything from the bank" in one place.
open class BankException(message: String) : Exception(message)

class InsufficientFundsException(
    val needed: Long,
    val available: Long,
) : BankException("need $needed, have $available")

// ── Helpers for section 5: a resource that must be closed ───────────
// AutoCloseable is the Java interface behind `use {}` (like Java's
// try-with-resources). close() must run even when work fails.
class Resource(
    private val name: String,
) : AutoCloseable {
    init {
        println("opened $name")
    }

    override fun close() = println("closed $name")
}

// A resource whose close() itself fails (a network socket, say).
class FailingResource : AutoCloseable {
    override fun close() = throw IllegalStateException("close failed")
}

private const val NOT_A_NUMBER = -1
private const val STARTING_BALANCE = 100L
private const val BIG_WITHDRAWAL = 500L
private const val NEGATIVE_AGE = -3

fun main() {
    // ── 1. CONCEPT: try is an EXPRESSION ─────────────────────────────
    // The value of try/catch is the last expression of whichever block
    // ran. No `var result` + assignment dance needed.
    fun parse(text: String): Int =
        try {
            text.toInt()
        } catch (e: NumberFormatException) {
            NOT_A_NUMBER
        }
    println("parse(\"42\") = ${parse("42")}, parse(\"x\") = ${parse("x")}")

    // PITFALL: both branches must fit the declared type.
    //   val n: Int = try { "1".toInt() } catch (e: NumberFormatException) { "oops" }
    //   ❌ Initializer type mismatch: expected 'Int', actual 'Comparable<*> & Serializable'.

    // ── 2. CONCEPT: finally ALWAYS runs ──────────────────────────────
    // Use it for cleanup that must happen whether or not work failed.
    // ("Always" = unless the JVM itself stops: System.exit, a crash, or
    // an endless loop in try. And if finally throws, its exception
    // REPLACES the original one, so keep finally blocks simple.)
    try {
        println("try block")
    } finally {
        println("finally runs (no error)")
    }
    try {
        println("try block")
        error("boom") // error(msg) throws IllegalStateException(msg)
    } catch (e: IllegalStateException) {
        println("caught: ${e::class.simpleName}")
    } finally {
        println("finally runs (after an error)")
    }
    val steps = mutableListOf<String>()
    val result = returnsFromTry(steps)
    println("finally even runs on return: result=$result, steps=$steps")

    // ── 3. CONCEPT: throw is an expression of type Nothing ───────────
    // Nothing is the "bottom" type: it fits wherever ANY type is expected,
    // and the compiler knows nothing after it runs
    // (see l03_functions/lesson04_local_unit_nothing). That is why
    // `text.toIntOrNull() ?: throw ...` still has the type Int.
    println("parsePositive(\"7\") = ${parsePositive("7")}")
    try {
        parsePositive("x")
    } catch (e: IllegalArgumentException) {
        println("parsePositive(\"x\") -> ${e.message}")
    }
    // Careful: require(...) is NOT of type Nothing. It returns Unit when
    // the check passes and throws IllegalArgumentException when it fails.
    // error(...) and TODO(...) are the helpers whose type is Nothing.
    try {
        requireAge(NEGATIVE_AGE)
    } catch (e: IllegalArgumentException) {
        println("requireAge($NEGATIVE_AGE) -> ${e::class.simpleName}: ${e.message}")
    }

    // ── 4. CONCEPT: your own exception types ─────────────────────────
    // Catch the most specific type first; a base class catches the rest.
    try {
        withdraw(balance = STARTING_BALANCE, amount = BIG_WITHDRAWAL)
    } catch (e: InsufficientFundsException) {
        println("withdraw($BIG_WITHDRAWAL) -> ${e::class.simpleName}: ${e.message}")
    }
    try {
        withdraw(balance = STARTING_BALANCE, amount = BIG_WITHDRAWAL)
    } catch (e: BankException) { // the base class catches every subclass
        println("caught by the base class: ${e::class.simpleName}")
    }
    // WHY not exceptions everywhere? Expected outcomes (not enough money)
    // are often better as a result value: see l05_oop/lesson04_visibility
    // (the require vs result rule) and lesson02_result next.

    // ── 5. CONCEPT: use {} closes resources, in reverse order ────────
    Resource("db").use {
        Resource("file").use {
            println("using both")
        } // file closes first...
    } // ...then db: last opened, first closed
    try {
        Resource("reader").use { error("read failed") }
    } catch (e: IllegalStateException) {
        println("use{} closed the reader, then the error escaped: ${e::class.simpleName}")
    }
    // What if close() fails too? The body's exception wins, and the close
    // error is attached to it as a "suppressed" exception, so nothing is
    // lost. If ONLY close() fails, that exception is the one you get.
    try {
        FailingResource().use { error("work failed") }
    } catch (e: IllegalStateException) {
        println("body error wins: ${e.message}, suppressed = ${e.suppressed.map { it.message }}")
    }
    try {
        FailingResource().use { /* the work itself succeeds */ }
    } catch (e: IllegalStateException) {
        println("only close() failed: ${e.message}")
    }
    // (use is an inline function, so `return` inside its lambda returns
    // from the enclosing function, like in forEach: l07_functional/lesson07_inline.)

    // ── 6. WHY: Kotlin has no checked exceptions ─────────────────────
    // Java forces callers to catch or declare IOException ("checked").
    // Kotlin does not: every exception is "unchecked". You decide where
    // to handle it. When Java code calls your Kotlin function and should
    // know about an exception, add @Throws(IOException::class) (see
    // lesson09_java_interop).

    // ── 7. PITFALL: catching too much ────────────────────────────────
    //   catch (e: Exception) { }      // swallows EVERYTHING, even bugs
    // In coroutines this also swallows CancellationException, which
    // breaks cancellation: always rethrow it
    // (l09_coroutines/lesson11_exceptions).

    // ── 8. TRY IT ────────────────────────────────────────────────────
    // a) Make parse() return null instead of -1. What type does it get?
    // b) Add a `finally` to section 4 that prints "audit logged".
    // c) Add a third Resource inside section 5. Predict the close order.
}

private fun returnsFromTry(steps: MutableList<String>): String {
    try {
        steps += "try"
        return "in-try"
    } finally {
        // Runs before the function actually returns. Never `return` from
        // finally: it would silently replace the try's result.
        steps += "finally"
    }
}

private fun parsePositive(text: String): Int {
    // `?: throw` has type Nothing on the right, so `number` is a plain Int.
    val number: Int = text.toIntOrNull() ?: throw IllegalArgumentException("not a number: $text")
    require(number > 0) { "must be positive, was $number" }
    return number
}

private fun requireAge(age: Int): Int {
    require(age >= 0) { "age must be >= 0, was $age" }
    return age
}

private fun withdraw(
    balance: Long,
    amount: Long,
): Long {
    if (amount > balance) throw InsufficientFundsException(needed = amount, available = balance)
    return balance - amount
}
