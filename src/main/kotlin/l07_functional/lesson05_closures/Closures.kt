package l07_functional.lesson05_closures

/**
 * =====================================================================
 *  LESSON 07.5 — Closures
 * =====================================================================
 *  GOAL   : Understand that a lambda REMEMBERS the variables around it,
 *           build a counter from a closure, and avoid the shared-var trap.
 *  RUN    : ./gradlew -q run -Plesson=l07_functional.lesson05_closures.ClosuresKt
 *  OUTPUT :
 *           greet("Ada") = Welcome to Kotlin, Ada
 *           a: 1 2 3
 *           b: 1
 *           a again: 4
 *           clicks counted by a lambda: 3
 *           captured var changed later -> report says 99
 *           snapshot taken early -> report says 1 (limit is now 99)
 *  NEXT   : lesson06_scope_functions/ScopeFunctions.kt
 * =====================================================================
 */

// Every call to makeCounter creates a NEW `count` variable.
// The returned lambda keeps that variable alive and private.
fun makeCounter(): () -> Int {
    var count = 0
    return {
        count += 1
        count
    }
}

fun main() {
    // ── 1. CONCEPT: capturing a val ──────────────────────────────────
    // The lambda uses `course`, which lives OUTSIDE the lambda.
    val course = "Kotlin"
    val greet = { name: String -> "Welcome to $course, $name" }
    println("greet(\"Ada\") = ${greet("Ada")}")

    // ── 2. CONCEPT: a closure keeps private state ────────────────────
    // a and b each have their OWN count. Nobody else can touch it.
    val a = makeCounter()
    val b = makeCounter()
    println("a: ${a()} ${a()} ${a()}")
    println("b: ${b()}")
    println("a again: ${a()}")

    // ── 3. CONCEPT: capturing a var (and changing it) ────────────────
    // Unlike Java, a Kotlin lambda may modify a captured var.
    var clicks = 0
    val onClick = { clicks += 1 }
    repeat(3) { onClick() }
    println("clicks counted by a lambda: $clicks")

    // ── 4. PITFALL: the lambda sees the var's CURRENT value ──────────
    // A closure captures the VARIABLE, not a copy of its value.
    var level = 1
    val report = { "report says $level" }
    level = 99 // changed AFTER the lambda was created...
    println("captured var changed later -> ${report()}") // ...but the lambda sees 99

    // Fix: copy the value into a val first. The val can't change.
    var limit = 1
    val snapshot = limit
    val fixedReport = { "report says $snapshot" }
    limit = 99
    println("snapshot taken early -> ${fixedReport()} (limit is now $limit)")

    // ── 5. WHY ───────────────────────────────────────────────────────
    // Closures give you private state without writing a class. But
    // shared mutable state is risky: when several coroutines change the
    // same captured var at once, updates get lost (level 09). Prefer
    // capturing vals.

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Write makeCounter(step: Int) that adds `step` each time.
    // b) Write fun makeGreeter(greeting: String): (String) -> String.
    // c) In section 4, change `var level` to `val level`. What does the compiler say
    //    about `level = 99`? (Hint: you saw this message in lesson 01.2.)
}
