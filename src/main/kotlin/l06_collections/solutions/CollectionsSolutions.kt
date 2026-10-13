package l06_collections.solutions

import l06_collections.CollectionsTasks

/**
 * Reference answers for Level 06. Try the exercises first!
 * Each answer explains WHY it is written this way.
 */
object CollectionsSolutions : CollectionsTasks {
    // filter first, so we only square the items we keep.
    override fun evensSquared(numbers: List<Int>): List<Int> = numbers.filter { it % 2 == 0 }.map { it * it }

    // associateWith: the words become keys, the lambda computes each value.
    override fun wordLengths(words: List<String>): Map<String, Int> = words.associateWith { it.length }

    // eachCount() returns a LinkedHashMap, so keys stay in first-seen order.
    // maxByOrNull returns the FIRST entry with the max value: that's the tie rule.
    override fun mostFrequentChar(text: String): Char? =
        text
            .filterNot { it.isWhitespace() }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

    // Filter out empty words before calling first(), which would crash on "".
    override fun groupByFirstLetter(words: List<String>): Map<Char, List<String>> =
        words
            .filter { it.isNotEmpty() }
            .groupBy { it.first().lowercaseChar() }

    // distinct() removes the duplicate 9 in [4, 9, 9, 2]; getOrNull handles short lists.
    override fun secondLargestDistinct(numbers: List<Int>): Int? =
        numbers
            .distinct()
            .sortedDescending()
            .getOrNull(1)

    // runningReduce starts from the first item, so the output has the same size.
    // (runningFold(0) would add an extra leading 0.)
    override fun runningTotals(numbers: List<Int>): List<Int> = numbers.runningReduce { total, n -> total + n }

    override fun mergeInventories(first: Map<String, Int>, second: Map<String, Int>): Map<String, Int> =
        // keys + keys is a Set: first's keys in order, then new keys of second.
        // associateWith turns each key into key -> summed count; a missing
        // key reads as null, and `?: 0` treats it as zero.
        (first.keys + second.keys).associateWith { (first[it] ?: 0) + (second[it] ?: 0) }

    // compareByDescending on the count, thenBy the word for alphabetical ties.
    override fun topNWords(text: String, n: Int): List<String> {
        if (n <= 0) return emptyList()
        val counts =
            text
                .lowercase()
                .split(NON_LETTERS)
                .filter { it.isNotEmpty() }
                .groupingBy { it }
                .eachCount()
        return counts.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(n)
            .map { it.key }
    }

    // Two texts are anagrams when their sorted letters are equal.
    override fun isAnagram(first: String, second: String): Boolean =
        normalizedLetters(first) == normalizedLetters(second)

    // distinct() keeps the first occurrence of every value, in order.
    override fun flattenAndDedupe(lists: List<List<Int>>): List<Int> = lists.flatten().distinct()

    private fun normalizedLetters(text: String): List<Char> =
        text
            .lowercase()
            .filterNot { it.isWhitespace() }
            .toList()
            .sorted()

    private val NON_LETTERS = Regex("[^a-z]+")
}
