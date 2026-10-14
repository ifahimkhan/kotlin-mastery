package l08_generics.lesson08_type_system

/**
 * =====================================================================
 *  LESSON 08.8 — A tour of the type system
 * =====================================================================
 *  GOAL   : Place Any, Any?, Unit and Nothing in one picture, see how
 *           Nothing makes generic "failure" types work, name long types
 *           with typealias, and meet `T & Any`.
 *  RUN    : ./gradlew -q run -Plesson=l08_generics.lesson08_type_system.TypeSystemKt
 *  OUTPUT :
 *           Everything is an Any: Int, String, Double, Char, Boolean
 *           printing...
 *           println returned: kotlin.Unit
 *           name = Ada
 *           emptyList fits List<String>: size = 0
 *           user 1 -> Loaded: Ada
 *           user 2 -> Failed: no user 2
 *           Ada's best score = 91
 *           firstNonNullOr: x / none
 *  NEXT   : practice/result/ResultType.kt
 * =====================================================================
 */

/** Never returns normally: its return type is Nothing (level 03). */
fun fail(message: String): Nothing = throw IllegalStateException(message)

// `out T` + Nothing: Failed has no value of type T, so it uses Nothing,
// the subtype of EVERY type. That makes Failed fit LoadResult<String>,
// LoadResult<Int>... (sealed types: level 05)
sealed interface LoadResult<out T> {
    data class Loaded<out T>(
        val value: T,
    ) : LoadResult<T>

    data class Failed(
        val reason: String,
    ) : LoadResult<Nothing>
}

fun loadUser(id: Int): LoadResult<String> =
    if (id == KNOWN_USER) LoadResult.Loaded("Ada") else LoadResult.Failed("no user $id")

private const val KNOWN_USER = 1

// typealias gives a long type a short, meaningful name. It's NOT a new
// type: Scores and Map<String, List<Int>> are fully interchangeable.
typealias Scores = Map<String, List<Int>>

// `T & Any` = "T, but definitely not null", even if T itself is nullable.
fun <T> firstNonNullOr(
    items: List<T>,
    default: T & Any,
): T & Any {
    for (item in items) {
        if (item != null) return item // smart cast: item is T & Any here
    }
    return default
}

fun main() {
    // ── 1. CONCEPT: Any — the root of all non-null types ─────────────
    val things: List<Any> = listOf(1, "two", 3.0, 'c', true)
    println("Everything is an Any: ${things.joinToString { it::class.simpleName.orEmpty() }}")
    // Any? is the root of EVERYTHING, null included:
    //   val a: Any = null // ❌ Null cannot be a value of a non-null type 'Any'.

    // ── 2. CONCEPT: Unit — "no useful result" ────────────────────────
    // Unit has exactly ONE value, also called Unit. Functions without a
    // declared return type return it.
    val result: Unit = println("printing...")
    println("println returned: $result")

    // ── 3. CONCEPT: Nothing — "never returns" ────────────────────────
    // Nothing has NO values. Because it's a subtype of every type, a
    // call to fail() fits anywhere, e.g. on the right of ?: .
    val maybeName: String? = "Ada"
    val name: String = maybeName ?: fail("name is required")
    println("name = $name")
    //   val n: Nothing = 5 // ❌ Initializer type mismatch: expected 'Nothing', actual 'Int'.

    // ── 4. CONCEPT: Nothing inside generics ──────────────────────────
    // emptyList<Nothing>() is a List<Nothing>. List is `out`, and Nothing
    // is a subtype of String, so it is also a List<String>.
    val noWords: List<String> = emptyList<Nothing>()
    println("emptyList fits List<String>: size = ${noWords.size}")

    for (id in listOf(1, 2)) {
        val text =
            when (val loaded = loadUser(id)) { // exhaustive, no else (level 05)
                is LoadResult.Loaded -> "Loaded: ${loaded.value}"
                is LoadResult.Failed -> "Failed: ${loaded.reason}"
            }
        println("user $id -> $text")
    }

    // ── 5. CONCEPT: typealias ────────────────────────────────────────
    val scores: Scores = mapOf("Ada" to listOf(72, 91, 85))
    println("Ada's best score = ${scores["Ada"]?.maxOrNull()}")

    // ── 6. CONCEPT: definitely non-nullable T & Any ──────────────────
    val found = firstNonNullOr(listOf(null, "x"), "none")
    val fallback = firstNonNullOr(listOf<String?>(null), "none")
    println("firstNonNullOr: $found / $fallback")

    // ── 7. THE PICTURE ───────────────────────────────────────────────
    //                 Any?              <- top: every value, and null
    //               /      \
    //            Any       Int?, String?, ...
    //        /  /   |   \        |
    //   Int String Unit ...      |    <- every class is an Any
    //        \  \   |   /     Nothing?  <- type of `null`: its only value
    //         \  \  |  /        /
    //              Nothing              <- bottom: no values at all
    // (Every non-null type T sits below its nullable twin T?.)

    // ── 8. TRY IT ────────────────────────────────────────────────────
    // a) Write `fun notImplementedYet(): Nothing = TODO()` and use it as
    //    the body of a function that returns Int.
    // b) Add `data object Loading : LoadResult<Nothing>`. Why Nothing?
    //    Then fix the `when` the compiler points at.
    // c) Write `typealias UserId = String`. Can you still pass any
    //    String where a UserId is expected? (Compare: value classes, level 05.)
}
