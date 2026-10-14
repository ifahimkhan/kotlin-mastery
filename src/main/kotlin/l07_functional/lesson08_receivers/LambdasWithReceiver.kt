package l07_functional.lesson08_receivers

/**
 * =====================================================================
 *  LESSON 07.8 — Lambdas with receiver: T.() -> R
 * =====================================================================
 *  GOAL   : Understand the "this inside a lambda" trick behind apply,
 *           buildString and Kotlin DSLs, and write your own.
 *  RUN    : ./gradlew -q run -Plesson=l07_functional.lesson08_receivers.LambdasWithReceiverKt
 *  OUTPUT :
 *           plain lambda    : 5
 *           with receiver   : 5
 *           shout("hey")    : HEY!
 *           myBuildString   : a-b-c
 *           buildString     : a-b-c
 *           <ul>
 *             <li>Kotlin</li>
 *             <li>Lambdas</li>
 *           </ul>
 *  NEXT   : lesson09_patterns/FunctionalPatterns.kt
 * =====================================================================
 */

// ── Our own buildString ───────────────────────────────────────────────
// `StringBuilder.() -> Unit` = "a lambda that runs WITH a StringBuilder
// as `this`". Inside it you can call append(...) directly.
fun myBuildString(actions: StringBuilder.() -> Unit): String {
    val builder = StringBuilder()
    builder.actions() // run the lambda with builder as `this`
    return builder.toString()
}

// ── A tiny HTML-list builder (full DSLs: level 11) ────────────────────
class ListBuilder {
    private val items = mutableListOf<String>()

    fun item(text: String) {
        items.add(text)
    }

    fun render(): String =
        buildString {
            appendLine("<ul>")
            items.forEach { appendLine("  <li>$it</li>") }
            append("</ul>")
        }
}

fun ul(content: ListBuilder.() -> Unit): String = ListBuilder().apply(content).render()

fun main() {
    // ── 1. CONCEPT: the difference in one picture ────────────────────
    // (String) -> Int   : the string is a PARAMETER   -> use it
    // String.() -> Int  : the string is the RECEIVER  -> use this (or nothing)
    val plain: (String) -> Int = { it.length }
    val withReceiver: String.() -> Int = { length } // this.length
    println("plain lambda    : ${plain("hello")}")
    println("with receiver   : ${"hello".withReceiver()}")

    // A receiver lambda can be called both ways:
    val shout: String.() -> String = { uppercase() + "!" }
    println("shout(\"hey\")    : ${shout("hey")}") // same as "hey".shout()

    // ── 2. CONCEPT: this is how buildString and apply work ───────────
    val ours =
        myBuildString {
            append("a")
            append("-b")
            append("-c")
        }
    println("myBuildString   : $ours")

    val stdlib =
        buildString {
            append("a")
            append("-b")
            append("-c")
        }
    println("buildString     : $stdlib")
    // apply is the same idea: inline fun <T> T.apply(block: T.() -> Unit): T
    // (the <T> part is generics, level 08)

    // ── 3. CONCEPT: a mini DSL ───────────────────────────────────────
    // Inside ul { }, `this` is a ListBuilder, so item(...) just works.
    val html =
        ul {
            item("Kotlin")
            item("Lambdas")
        }
    println(html)

    // ── 4. PITFALL: receiver functions only exist inside the lambda ──
    //   val sb = StringBuilder()
    //   append("x")
    //   // ❌ Unresolved reference 'append'.
    // append needs a StringBuilder receiver: sb.append("x"), or call it
    // inside a lambda that has one.

    // ── 5. WHY ───────────────────────────────────────────────────────
    // Receiver lambdas let an API read like a little language: Gradle's
    // build.gradle.kts, Ktor routing and Compose UI are all built this way.

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Add fun ListBuilder.items(vararg texts: String) and use it inside ul { }.
    // b) Write val isLong: String.() -> Boolean = { length > 5 } and call "kotlin".isLong().
    // c) Rewrite myBuildString using apply on the StringBuilder.
}
