package l06_collections.lesson03_sets

/**
 * =====================================================================
 *  LESSON 06.3 — Sets
 * =====================================================================
 *  GOAL   : Store unique items, combine sets, and know which sets keep
 *           their order.
 *  RUN    : ./gradlew -q run -Plesson=l06_collections.lesson03_sets.SetsKt
 *  OUTPUT :
 *           tags = [kotlin, java, swift], size = 3
 *           "java" in tags = true
 *           add("kotlin") returned false, add("go") returned true -> [kotlin, java, swift, go]
 *           distinct visitors: [ann, bob, cid]
 *           backend  union    frontend = [kotlin, sql, go, ts, css]
 *           backend  intersect frontend = [kotlin]
 *           backend  subtract frontend = [sql, go]
 *           setOf keeps insertion order : [pear, apple, mango]
 *           sortedSetOf keeps sort order: [apple, mango, pear]
 *  NEXT   : lesson04_maps/Maps.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: a Set holds each item at most once ───────────────
    val tags = setOf("kotlin", "java", "kotlin", "swift") // duplicate dropped
    println("tags = $tags, size = ${tags.size}")
    println("\"java\" in tags = ${"java" in tags}") // very fast lookup

    // ── 2. CONCEPT: MutableSet.add tells you if it was new ───────────
    val languages = mutableSetOf("kotlin", "java", "swift")
    val addedAgain = languages.add("kotlin")
    val addedNew = languages.add("go")
    println("add(\"kotlin\") returned $addedAgain, add(\"go\") returned $addedNew -> $languages")

    // Removing duplicates from a list: toSet() or distinct().
    val visits = listOf("ann", "bob", "ann", "cid", "bob")
    println("distinct visitors: ${visits.distinct()}")

    // ── 3. CONCEPT: set math ─────────────────────────────────────────
    val backend = setOf("kotlin", "sql", "go")
    val frontend = setOf("ts", "css", "kotlin")
    println("backend  union    frontend = ${backend union frontend}")
    println("backend  intersect frontend = ${backend intersect frontend}")
    println("backend  subtract frontend = ${backend subtract frontend}")

    // ── 4. CONCEPT: order ────────────────────────────────────────────
    // setOf / mutableSetOf are LinkedHashSets: they remember insertion order.
    // sortedSetOf keeps items sorted. A plain HashSet() has NO reliable
    // order: never write code that depends on how a HashSet prints.
    println("setOf keeps insertion order : ${setOf("pear", "apple", "mango")}")
    println("sortedSetOf keeps sort order: ${sortedSetOf("pear", "apple", "mango")}")

    // ── 5. PITFALL: sets have no index ───────────────────────────────
    //   tags[0]   // ❌ No 'get' operator method providing array access.
    // Use tags.first() or convert with toList() if you need positions.
    //   tags.add("go") // ❌ Unresolved reference 'add' on receiver of type 'Set<String>'.

    // ── 6. WHY ───────────────────────────────────────────────────────
    // "Have I seen this before?" is a Set question. `x in set` is fast
    // even with millions of items, while `x in list` checks one by one.

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Find the letters that appear in both "kotlin" and "java" (hint: toSet()).
    // b) Count unique words in "the cat and the hat".
    // c) Print sortedSetOf(5, 1, 3) and setOf(5, 1, 3). Explain the difference.
}
