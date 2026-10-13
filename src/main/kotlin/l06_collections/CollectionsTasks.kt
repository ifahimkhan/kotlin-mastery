package l06_collections

/**
 * The list of Level 06 exercises.
 *
 * Both [l06_collections.exercises.CollectionsExercises] (your answers) and
 * [l06_collections.solutions.CollectionsSolutions] (reference answers)
 * implement this interface, so the SAME tests can check both.
 *
 * Rule for this level: try to solve each one WITHOUT a for loop, using
 * map, filter, groupBy, fold and friends. Most answers fit in 1-3 lines.
 */
interface CollectionsTasks {
    /** Ex 1. Keep the even numbers and square them, in order.  [1, 2, 3, 4] -> [4, 16] */
    fun evensSquared(numbers: List<Int>): List<Int>

    /** Ex 2. Map each word to its length.  ["hi", "kotlin"] -> {hi=2, kotlin=6} */
    fun wordLengths(words: List<String>): Map<String, Int>

    /**
     * Ex 3. The character that appears most often, ignoring whitespace.
     * On a tie, the one that appears FIRST in the text wins.
     * Returns null when there is no non-whitespace character.
     * mostFrequentChar("hello world") == 'l',  mostFrequentChar("abab") == 'a'
     */
    fun mostFrequentChar(text: String): Char?

    /**
     * Ex 4. Group words by their lower-case first letter. Skip empty words.
     * Keep the original order inside each group.
     * ["Apple", "bob", "avocado"] -> {a=[Apple, avocado], b=[bob]}
     */
    fun groupByFirstLetter(words: List<String>): Map<Char, List<String>>

    /**
     * Ex 5. The second largest DISTINCT number, or null if there isn't one.
     * [4, 9, 9, 2] -> 4,  [7, 7] -> null,  [] -> null
     */
    fun secondLargestDistinct(numbers: List<Int>): Int?

    /** Ex 6. Running totals.  [1, 2, 3, 4] -> [1, 3, 6, 10],  [] -> [] */
    fun runningTotals(numbers: List<Int>): List<Int>

    /**
     * Ex 7. Merge two stock maps, adding the counts of shared items.
     * Keys of [first] come first (in their order), then new keys of [second].
     * {pen=2, ink=1} + {ink=4, cap=3} -> {pen=2, ink=5, cap=3}
     */
    fun mergeInventories(first: Map<String, Int>, second: Map<String, Int>): Map<String, Int>

    /**
     * Ex 8. The [n] most frequent words in [text] (lower case, letters only).
     * Sort by count (highest first), then alphabetically for ties.
     * topNWords("b a b c a b", 2) == [b, a].  n <= 0 returns an empty list.
     */
    fun topNWords(text: String, n: Int): List<String>

    /**
     * Ex 9. True when both texts use exactly the same letters the same
     * number of times, ignoring case and spaces.
     * isAnagram("Dormitory", "dirty room") == true
     */
    fun isAnagram(first: String, second: String): Boolean

    /**
     * Ex 10. Flatten the lists and remove duplicates, keeping the FIRST
     * occurrence order.  [[3, 1], [1, 2], [3]] -> [3, 1, 2]
     */
    fun flattenAndDedupe(lists: List<List<Int>>): List<Int>
}
