package l06_collections.lesson04_maps

/**
 * =====================================================================
 *  LESSON 06.4 — Maps
 * =====================================================================
 *  GOAL   : Store key -> value pairs, look values up safely, loop over
 *           entries, combine/copy/filter maps and count with them.
 *  RUN    : ./gradlew -q run -Plesson=l06_collections.lesson04_maps.MapsKt
 *  OUTPUT :
 *           prices = {apple=120, bread=250, milk=99}
 *           "apple" to 120 is a Pair: first=apple, second=120
 *           prices["bread"] = 250, prices["caviar"] = null
 *           getOrDefault("caviar", 0) = 0
 *           getValue("caviar") -> crashed with NoSuchElementException
 *           apple costs 120
 *           bread costs 250
 *           milk costs 99
 *           keys = [apple, bread, milk], values = [120, 250, 99]
 *           stock = {pens=12, paper=3}
 *           prices + extra = {apple=120, bread=250, milk=105, tea=80}
 *           copy = {apple=0, bread=250, milk=99}, prices = {apple=120, bread=250, milk=99}
 *           price > 100 = {apple=120, bread=250}
 *           filterValues < 100 = {milk=99}
 *           letter counts = {b=1, a=3, n=2}
 *           groups = {fruit=[apple, kiwi], veg=[leek]}
 *  NEXT   : lesson05_transformations/Transformations.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: mapOf and `to` ───────────────────────────────────
    // Each entry is key to value. Keys are unique; values may repeat.
    val prices = mapOf("apple" to 120, "bread" to 250, "milk" to 99)
    println("prices = $prices")

    // `to` simply builds a Pair(first, second).
    val pair = "apple" to 120
    println("\"apple\" to 120 is a Pair: first=${pair.first}, second=${pair.second}")

    // ── 2. CONCEPT: reading values ───────────────────────────────────
    // map[key] returns a NULLABLE value: the key might be missing.
    println("prices[\"bread\"] = ${prices["bread"]}, prices[\"caviar\"] = ${prices["caviar"]}")
    println("getOrDefault(\"caviar\", 0) = ${prices.getOrDefault("caviar", 0)}")
    try { // (try/catch: later level) — catches the crash so the lesson continues
        prices.getValue("caviar") // throws when the key is missing
    } catch (e: NoSuchElementException) {
        println("getValue(\"caviar\") -> crashed with ${e::class.simpleName}")
    }
    //   val p: Int = prices["apple"] // ❌ Initializer type mismatch: expected 'Int', actual 'Int?'.
    // Fix: prices["apple"] ?: 0   (level 04)

    // ── 3. CONCEPT: looping with destructuring ───────────────────────
    // (name, price) unpacks each entry into two variables.
    for ((name, price) in prices) {
        println("$name costs $price")
    }
    println("keys = ${prices.keys}, values = ${prices.values}")

    // ── 4. CONCEPT: MutableMap ───────────────────────────────────────
    val stock = mutableMapOf("pens" to 10)
    stock["paper"] = 3 // add a new key
    stock["pens"] = 12 // replace the value of an existing key
    println("stock = $stock")

    // ── 5. CONCEPT: combining, copying and filtering ─────────────────
    // map + map builds a NEW map. On a duplicate key the RIGHT side wins.
    val extra = mapOf("milk" to 105, "tea" to 80)
    println("prices + extra = ${prices + extra}")
    // toMutableMap() makes an editable COPY; the original stays unchanged.
    val copy = prices.toMutableMap()
    copy["apple"] = 0
    println("copy = $copy, prices = $prices")
    // filter sees each entry (it.key, it.value); filterValues sees only values.
    println("price > 100 = ${prices.filter { it.value > 100 }}")
    println("filterValues < 100 = ${prices.filterValues { it < 100 }}")

    // ── 6. CONCEPT: counting and grouping by hand ────────────────────
    // Counting: read the old count (null -> 0), add 1, store it back.
    val counts = mutableMapOf<Char, Int>()
    for (letter in "banana") {
        counts[letter] = (counts[letter] ?: 0) + 1
    }
    println("letter counts = $counts")

    // getOrPut: "give me the value, and if it's missing, put this first".
    // Perfect for grouping: create the list once, then keep adding to it.
    val groups = mutableMapOf<String, MutableList<String>>()
    val produce = listOf("fruit" to "apple", "veg" to "leek", "fruit" to "kiwi")
    for ((kind, item) in produce) {
        groups.getOrPut(kind) { mutableListOf() }.add(item)
    }
    println("groups = $groups")
    // Lesson 06.7 shows groupingBy().eachCount() and groupBy, which do this in one line.

    // ── 7. PITFALL: order and read-only maps ─────────────────────────
    // mapOf / mutableMapOf (LinkedHashMap) keep insertion order.
    // HashMap() does NOT: never rely on how a HashMap prints.
    //   prices["tea"] = 80 // ❌ No 'set' operator method providing array access.

    // ── 8. TRY IT ────────────────────────────────────────────────────
    // a) Build a map from country to capital for 3 countries and print one capital.
    // b) Count the words in "to be or not to be" (split(" ") gives the words).
    // c) Merge two price lists with + and print which value won for a shared key.
}
