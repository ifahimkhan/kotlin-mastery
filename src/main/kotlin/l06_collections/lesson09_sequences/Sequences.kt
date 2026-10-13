package l06_collections.lesson09_sequences

/**
 * =====================================================================
 *  LESSON 06.9 — Sequences: lazy collections
 * =====================================================================
 *  GOAL   : SEE the difference between eager lists and lazy sequences,
 *           and know when a sequence is worth it.
 *  RUN    : ./gradlew -q run -Plesson=l06_collections.lesson09_sequences.SequencesKt
 *  OUTPUT :
 *           --- List (eager): each step runs on ALL items ---
 *           map 1
 *           map 2
 *           map 3
 *           map 4
 *           filter 10
 *           filter 20
 *           filter 30
 *           filter 40
 *           result: 30
 *           --- Sequence (lazy): one item travels through all steps ---
 *           map 1
 *           filter 10
 *           map 2
 *           filter 20
 *           map 3
 *           filter 30
 *           result: 30
 *           built the sequence, nothing ran yet
 *           first 10 powers of 2: [1, 2, 4, 8, 16, 32, 64, 128, 256, 512]
 *           countdown: [5, 4, 3, 2, 1]
 *  NEXT   : lesson10_pitfalls/CollectionPitfalls.kt
 * =====================================================================
 */
fun main() {
    val numbers = listOf(1, 2, 3, 4)

    // ── 1. CONCEPT: lists are EAGER ──────────────────────────────────
    // map runs on every item and builds a whole new list, THEN filter
    // runs on every item of that list, THEN first picks one.
    println("--- List (eager): each step runs on ALL items ---")
    val eager =
        numbers
            .map { println("map $it"); it * 10 }
            .filter { println("filter $it"); it > 20 }
            .first()
    println("result: $eager")

    // ── 2. CONCEPT: sequences are LAZY ───────────────────────────────
    // asSequence(): each item goes through map AND filter before the
    // next item starts, and work stops as soon as first() is happy.
    // Item 4 is never touched!
    println("--- Sequence (lazy): one item travels through all steps ---")
    val lazy =
        numbers
            .asSequence()
            .map { println("map $it"); it * 10 }
            .filter { println("filter $it"); it > 20 }
            .first()
    println("result: $lazy")

    // ── 3. CONCEPT: nothing happens without a terminal operation ─────
    // map/filter on a sequence only DESCRIBE the work. A terminal
    // operation (first, toList, sum, count, ...) actually runs it.
    numbers.asSequence().map { println("never printed $it"); it }
    println("built the sequence, nothing ran yet")

    // ── 4. CONCEPT: infinite sequences ───────────────────────────────
    // generateSequence(seed) { next } can go on forever; take() limits it.
    val powersOfTwo = generateSequence(1) { it * 2 }
    println("first 10 powers of 2: ${powersOfTwo.take(10).toList()}")
    // Returning null ends the sequence.
    val countdown = generateSequence(5) { if (it > 1) it - 1 else null }
    println("countdown: ${countdown.toList()}")

    // ── 5. WHY / WHEN ────────────────────────────────────────────────
    // Use a Sequence when: the data is large, there are several steps,
    // you stop early (first, take), or the source is infinite.
    // Use a List when: the data is small. Lists are simpler and often
    // FASTER for small data, because a sequence adds some overhead.

    // ── 6. PITFALL ───────────────────────────────────────────────────
    // generateSequence(1) { it + 1 }.toList() never ends: an infinite
    // sequence needs take() or another limit BEFORE toList().

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Remove .first() from section 2 and add .toList(). Count the prints.
    // b) Find the first square number above 500 with generateSequence.
    // c) Build the sequence of even numbers and print the first 7 of them.
}
