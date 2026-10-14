package l07_functional.lesson06_scope_functions

/**
 * =====================================================================
 *  LESSON 07.6 — Scope functions: let, run, with, apply, also
 * =====================================================================
 *  GOAL   : Pick the right scope function by asking two questions:
 *           "this or it?" and "result or the object itself?"
 *  RUN    : ./gradlew -q run -Plesson=l07_functional.lesson06_scope_functions.ScopeFunctionsKt
 *  OUTPUT :
 *           let   -> name has 3 letters
 *           let   -> no nickname, nothing printed above this line
 *           run   -> Report(title=Q3, pages=12, draft=false) is 12 pages
 *           with  -> Q3: 12 pages
 *           apply -> Report(title=Q4, pages=20, draft=true)
 *           also  -> [log] created Report(title=Q4, pages=20, draft=true)
 *           also returned the same object: true
 *           also sees: KOTLIN
 *           chain -> KOTLIN!
 *  NEXT   : lesson07_inline/InlineFunctions.kt
 * =====================================================================
 */

// A small mutable class, so apply has something to configure.
// (Real code would usually prefer a data class with val + copy, level 05.)
class Report {
    var title: String = ""
    var pages: Int = 0
    var draft: Boolean = false

    override fun toString(): String = "Report(title=$title, pages=$pages, draft=$draft)"
}

private const val Q3_PAGES = 12
private const val Q4_PAGES = 20

fun main() {
    // ── THE TABLE ────────────────────────────────────────────────────
    //  function | object is called | returns          | typical use
    //  ---------+------------------+------------------+---------------------------
    //  let      | it               | lambda result    | null check + transform
    //  run      | this             | lambda result    | compute something from an object
    //  with     | this (argument)  | lambda result    | many calls on one object
    //  apply    | this             | the object       | configure a new object
    //  also     | it               | the object       | side effects: log, validate
    //
    // "this" lets you write title instead of report.title.
    // "it" keeps the outer `this` visible and names the object clearly.

    // ── 1. let: run code only when the value is not null ─────────────
    val name: String? = lookUpName()
    name?.let { println("let   -> name has ${it.length} letters") }

    val nickname: String? = lookUpNickname()
    nickname?.let { println("let   -> nickname is $it") } // skipped: nickname is null
    println("let   -> no nickname, nothing printed above this line")

    // ── 2. run: compute a result FROM an object (this) ───────────────
    val q3 = Report()
    q3.title = "Q3"
    q3.pages = Q3_PAGES
    val summary = q3.run { "$this is $pages pages" }
    println("run   -> $summary")

    // ── 3. with: several calls on one object, no chaining ────────────
    // with is not an extension: the object goes in the parentheses.
    val line = with(q3) { "$title: $pages pages" }
    println("with  -> $line")

    // ── 4. apply: configure, then get the SAME object back ───────────
    val q4 =
        Report().apply {
            title = "Q4" // this.title
            pages = Q4_PAGES
            draft = true
        }
    println("apply -> $q4")

    // ── 5. also: do something extra, keep the object ─────────────────
    val logged = q4.also { println("also  -> [log] created $it") }
    println("also returned the same object: ${logged === q4}")

    // ── 6. Chaining: each step hands its result to the next ──────────
    val shout =
        "kotlin"
            .let { it.uppercase() } // let returns the lambda result: "KOTLIN"
            .also { println("also sees: $it") } // a side effect; also returns "KOTLIN" unchanged
            .plus("!")
    println("chain -> $shout")

    // ── 7. PITFALL: `it` does not exist inside apply / run / with ────
    //   q4.apply { println(it) }
    //   // ❌ Unresolved reference 'it'.
    // Inside apply, the object is `this`.

    // ── 8. PITFALL: nesting hides which object you mean ──────────────
    //   q3.run { q4.apply { title = title } }   // which title is which?
    // Nested this/it quickly gets confusing. Max one or two levels deep;
    // otherwise use plain variables with clear names.

    // ── 9. PITFALL: ?.let { } ?: as an if/else ───────────────────────
    // x?.let { A } ?: B runs B ALSO when A itself returns null. For side
    // effects, a plain if/else is clearer (lesson 04.4).

    // ── 10. TRY IT ───────────────────────────────────────────────────
    // a) Build a StringBuilder with apply { append("a"); append("b") } and print it.
    // b) Use also to print a list before you sort it in the same chain.
    // c) Rewrite section 3 with run instead of with. What changes?
}

// Helpers return nullable values, so the compiler can't see the answer
// in advance (a literal "Ada" would be smart-cast to String).
private fun lookUpName(): String? = "Ada"

private fun lookUpNickname(): String? = null
