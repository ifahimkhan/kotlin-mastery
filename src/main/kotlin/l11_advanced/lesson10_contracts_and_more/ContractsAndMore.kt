@file:OptIn(ExperimentalContracts::class)

package l11_advanced.lesson10_contracts_and_more

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * =====================================================================
 *  LESSON 11.10 — Contracts, typealiases and context parameters
 * =====================================================================
 *  GOAL   : Teach the compiler facts it can't see: "if this returns true,
 *           x is not null" and "this lambda runs exactly once" (contracts).
 *           Name long function types (typealias), and pass shared
 *           helpers implicitly (context parameters).
 *  RUN    : ./gradlew -q run -Plesson=l11_advanced.lesson10_contracts_and_more.ContractsAndMoreKt
 *  OUTPUT :
 *           name length: 3
 *           no name given
 *           computed once: 42
 *           handlers: [upper:HELLO, count:5]
 *           [audit] saving invoice-7
 *           [audit] sending invoice-7
 *           saved and sent: invoice-7
 *  NEXT   : practice/html/HtmlDsl.kt
 * =====================================================================
 */

// ── 1. CONCEPT: a "returns ... implies" contract ────────────────────
// Without the contract, the caller can't smart-cast `name` after the
// check, because the compiler doesn't look inside hasText().
fun hasText(value: String?): Boolean {
    contract { returns(true) implies (value != null) }
    return !value.isNullOrBlank()
}

// ── 2. CONCEPT: a "callsInPlace" contract ───────────────────────────
// Promises the lambda runs EXACTLY ONCE, right here. That lets callers
// initialise a `val` inside it. (The stdlib's run, let, apply... all
// have this contract.)
inline fun <T> once(block: () -> T): T {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block()
}

// ── 3. CONCEPT: typealias for function types ────────────────────────
// A new NAME, not a new type: a Handler is any (String) -> String.
typealias Handler = (String) -> String
typealias NamedHandlers = Map<String, Handler>

// ── 4. CONCEPT: context parameters ──────────────────────────────────
// Some helpers (a logger, a clock, a transaction) are needed by many
// functions. A context parameter is passed IMPLICITLY from the caller's
// scope instead of being added to every argument list.
// Fact check for this repo: this code compiles with Kotlin 2.4.20 and NO
// extra compiler flags or warnings. (Older Kotlin versions needed the
// -Xcontext-parameters flag.) They replace an older experiment called
// "context receivers" (`context(AuditLog)` without a name), which was
// removed: you may still see it in old blog posts.
class AuditLog {
    fun record(message: String) = println("[audit] $message")
}

context(audit: AuditLog)
fun save(id: String) = audit.record("saving $id")

context(audit: AuditLog)
fun send(id: String) = audit.record("sending $id")

// This function also has the context, so it passes it on implicitly.
context(audit: AuditLog)
fun saveAndSend(id: String): String {
    save(id)
    send(id)
    return id
}

private const val ANSWER = 42

fun main() {
    for (name in listOf<String?>("Ada", null)) {
        if (hasText(name)) {
            println("name length: ${name.length}") // smart cast thanks to the contract
        } else {
            println("no name given")
        }
    }
    // Remove the contract and `name.length` fails:
    //   ❌ Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver
    //      of type 'String?'.

    val answer: Int
    once { answer = ANSWER } // allowed: the contract says "exactly once"
    println("computed once: $answer")
    // With a plain inline function (no contract) the same code fails:
    //   ❌ 'val' cannot be reassigned.

    val handlers: NamedHandlers =
        mapOf(
            "upper" to { text -> text.uppercase() },
            "count" to { text -> text.length.toString() },
        )
    println("handlers: ${handlers.map { (name, handler) -> "$name:${handler("hello")}" }}")

    // context(value) { ... } provides the context argument for the block.
    // (with(AuditLog()) { ... } also works, because a receiver in scope
    // can serve as a context argument, but it also exposes AuditLog's
    // members unqualified; context(...) is the purpose-made form.)
    val sent = context(AuditLog()) { saveAndSend("invoice-7") }
    println("saved and sent: $sent")
    // Outside a scope that provides an AuditLog:
    //   saveAndSend("x")
    //   ❌ No context argument for 'audit: AuditLog' found.

    // ── 5. WHY: use these sparingly ──────────────────────────────────
    // WRITING your own contract needs @OptIn(ExperimentalContracts::class)
    // (see the top of this file). The stdlib's own contracts (let, run,
    // isNullOrBlank, ...) work everywhere without any opt-in.
    // The compiler does NOT verify a contract, it TRUSTS it: a wrong
    // contract causes wrong smart casts. Contracts are allowed only in
    // final top-level or member functions with a block body.
    // Context parameters: an explicit parameter is often clearer. Reach
    // for them when the same helper threads through many calls.

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Write `fun isPositive(n: Int?): Boolean` with a contract and use it.
    // b) Change once's InvocationKind to AT_MOST_ONCE. What breaks?
    // c) Add a context parameter `clock: () -> Long` to save().
}
