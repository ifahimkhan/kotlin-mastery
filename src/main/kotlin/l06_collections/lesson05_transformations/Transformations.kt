package l06_collections.lesson05_transformations

/**
 * =====================================================================
 *  LESSON 06.5 — Transformations: map, flatMap, zip, associate
 * =====================================================================
 *  GOAL   : Turn one collection into another without writing loops.
 *  RUN    : ./gradlew -q run -Plesson=l06_collections.lesson05_transformations.TransformationsKt
 *  OUTPUT :
 *           map         -> [3, 5, 5]
 *           mapIndexed  -> [1. Ada, 2. Grace, 3. Linus]
 *           mapNotNull  -> [10, 30]
 *           flatten     -> [1, 2, 3, 4, 5]
 *           flatMap     -> [k, o, t, j, a, v, a]
 *           map (no flat) -> [[k, o, t], [j, a, v, a]]
 *           zip         -> [(Ada, 36), (Grace, 85), (Linus, 54)]
 *           zip + map   -> [Ada is 36, Grace is 85, Linus is 54]
 *           associateWith -> {Ada=3, Grace=5, Linus=5}
 *           associateBy   -> {A=Ada, G=Grace, L=Linus}
 *           associate     -> {ADA=ada, GRACE=grace, LINUS=linus}
 *           original names unchanged: [Ada, Grace, Linus]
 *  NEXT   : lesson06_filtering/Filtering.kt
 * =====================================================================
 */
fun main() {
    val names = listOf("Ada", "Grace", "Linus")

    // ── 1. CONCEPT: map — one in, one out ────────────────────────────
    println("map         -> ${names.map { it.length }}")
    // mapIndexed also gives you the position.
    println("mapIndexed  -> ${names.mapIndexed { index, name -> "${index + 1}. $name" }}")
    // mapNotNull drops every null result (level 04 preview).
    println("mapNotNull  -> ${listOf("10", "x", "30").mapNotNull { it.toIntOrNull() }}")

    // ── 2. CONCEPT: flatten and flatMap — many in, one flat list ─────
    val nested = listOf(listOf(1, 2), listOf(3), listOf(4, 5))
    println("flatten     -> ${nested.flatten()}")
    val words = listOf("kot", "java")
    println("flatMap     -> ${words.flatMap { it.toList() }}")
    // Compare: map keeps the inner lists, flatMap merges them.
    println("map (no flat) -> ${words.map { it.toList() }}")

    // ── 3. CONCEPT: zip — pair up two lists ──────────────────────────
    val ages = listOf(36, 85, 54)
    println("zip         -> ${names.zip(ages)}")
    println("zip + map   -> ${names.zip(ages).map { (name, age) -> "$name is $age" }}")
    // If the lists differ in length, zip stops at the shorter one.

    // ── 4. CONCEPT: associate — build a Map from a list ──────────────
    // associateWith: items become KEYS, the lambda gives the value.
    // associateBy  : the lambda gives the KEY, items become values.
    // associate    : the lambda returns a whole Pair.
    println("associateWith -> ${names.associateWith { it.length }}")
    println("associateBy   -> ${names.associateBy { it.first() }}")
    println("associate     -> ${names.associate { it.uppercase() to it.lowercase() }}")
    // PITFALL: with associateBy, duplicate keys keep only the LAST item.
    // Use groupBy (lesson 06.7) when one key can have many items.

    // ── 5. WHY: new collection, old one untouched ────────────────────
    // Every function here RETURNS a new collection. Nothing is mutated,
    // so the original list is still safe to use.
    println("original names unchanged: $names")

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Turn listOf(1, 2, 3) into listOf("1!", "2!", "3!").
    // b) Split listOf("a b", "c d e") into single words with flatMap { it.split(" ") }.
    // c) Zip listOf("x", "y") with listOf(1, 2, 3). Where did 3 go?
}
