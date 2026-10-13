package l04_null_safety.lesson07_safe_casts

/**
 * =====================================================================
 *  LESSON 04.7 — Safe casts (as?) and nulls in collections
 * =====================================================================
 *  GOAL   : Cast without crashing, and remove or skip nulls in lists.
 *  RUN    : ./gradlew -q run -Plesson=l04_null_safety.lesson07_safe_casts.SafeCastsAndCollectionsKt
 *  OUTPUT :
 *           value as String  -> crashed with ClassCastException
 *           value as? String -> null
 *           text as? String  -> hello
 *           (value as? String)?.length ?: -1 = -1
 *           listOfNotNull -> [Ann, Cid]
 *           scores        -> [90, null, 75, null]
 *           filterNotNull -> [90, 75]
 *           mapNotNull    -> [12, 7]
 *           List<String?> vs List<String>?: [null] vs null
 *  NEXT   : practice/safeinput/SafeInputParser.kt
 * =====================================================================
 */
fun main() {
    val value: Any = 42
    val text: Any = "hello"

    // ── 1. CONCEPT: as vs as? ────────────────────────────────────────
    // `as`  casts, and throws ClassCastException if the type is wrong.
    // `as?` casts, and returns null if the type is wrong. No crash.
    try {
        val forced = value as String
        println(forced)
    } catch (e: ClassCastException) {
        println("value as String  -> crashed with ${e::class.simpleName}")
    }
    println("value as? String -> ${value as? String}")
    println("text as? String  -> ${text as? String}")

    // as? combines nicely with ?. and ?:
    println("(value as? String)?.length ?: -1 = ${(value as? String)?.length ?: -1}")

    // ── 2. CONCEPT: building lists without nulls ─────────────────────
    // listOfNotNull keeps only the non-null arguments.
    val nickname: String? = null
    println("listOfNotNull -> ${listOfNotNull("Ann", nickname, "Cid")}")

    // ── 3. CONCEPT: cleaning lists that contain nulls ────────────────
    // (Collections get their own level 06, and `{ it ... }` blocks are
    // lambdas from level 07. This is a preview: read them like English.)
    val scores: List<Int?> = listOf(90, null, 75, null)
    println("scores        -> $scores")
    // filterNotNull turns List<Int?> into List<Int>: nulls are gone,
    // and so is the `?` in the type.
    val realScores: List<Int> = scores.filterNotNull()
    println("filterNotNull -> $realScores")

    // mapNotNull transforms each item and drops every null result.
    // Perfect with toIntOrNull: invalid entries simply disappear.
    val rawInput = listOf("12", "abc", "7", "")
    println("mapNotNull    -> ${rawInput.mapNotNull { it.toIntOrNull() }}")

    // ── 4. PITFALL: where does the `?` go? ───────────────────────────
    //   List<String?>  = the list exists, ITEMS may be null
    //   List<String>?  = the LIST itself may be null, items never are
    val itemsMayBeNull: List<String?> = listOf(null)
    val listMayBeNull: List<String>? = null
    println("List<String?> vs List<String>?: $itemsMayBeNull vs $listMayBeNull")

    // ── 5. WHY ───────────────────────────────────────────────────────
    // Data from the outside world (forms, files, JSON) is messy. as?,
    // filterNotNull and mapNotNull let you clean it in one line instead
    // of writing loops full of if-checks.

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) From listOf("1", "2", "x", "4") compute the sum of the valid numbers.
    // b) Given val things: List<Any> = listOf(1, "a", 2.5, "b"), keep only
    //    the Strings using mapNotNull { it as? String }.
    // c) Declare a List<String>? that is null and print its size safely.
}
