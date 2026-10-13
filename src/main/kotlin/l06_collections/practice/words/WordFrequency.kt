package l06_collections.practice.words

/**
 * =====================================================================
 *  PRACTICE 06.B — Word frequency counter
 * =====================================================================
 *  GOAL   : Read a text file, clean the words, count them and print a
 *           top-10 table. A classic data-processing pipeline.
 *  RUN    : ./gradlew -q run -Plesson=l06_collections.practice.words.WordFrequencyKt
 *  OUTPUT :
 *           Words: 43 total, 24 distinct
 *           Top 10:
 *            1. the   6 ######
 *            2. fox   5 #####
 *            3. is    4 ####
 *            4. quick 4 ####
 *            5. dog   3 ###
 *            6. a     2 ##
 *            7. lazy  2 ##
 *            8. and   1 #
 *            9. brown 1 #
 *           10. count 1 #
 *  DESIGN : The file is read ONCE in main. Everything else works on a
 *           plain String, so tests don't need files at all.
 *  NEW    : Regex("[^a-z]+") means "one or more characters that are NOT
 *           a to z": we split on them (more on regular expressions later).
 *           The file lives in src/main/resources/l06_collections/sample.txt.
 *  EXTEND : Ignore "stop words" like the/a/is/and with a Set and filterNot.
 * =====================================================================
 */

const val SAMPLE_RESOURCE = "l06_collections/sample.txt"
private const val TOP_COUNT = 10
private const val BAR_SYMBOL = "#"
private val NON_LETTERS = Regex("[^a-z]+")

/** Lower-case words made of letters only. Punctuation and digits are separators. */
fun normalizeWords(text: String): List<String> =
    text
        .lowercase()
        .split(NON_LETTERS)
        .filter { it.isNotEmpty() }

/** word -> count, in first-seen order. */
fun countWords(words: List<String>): Map<String, Int> = words.groupingBy { it }.eachCount()

/** The [n] most frequent words: highest count first, ties alphabetical. */
fun topWords(counts: Map<String, Int>, n: Int): List<Pair<String, Int>> =
    counts
        .toList()
        .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })
        .take(n)

/**
 * One table row, e.g. " 1. the   6 ######". [countWidth] right-aligns the
 * count so the bars still line up once a count reaches 10.
 */
fun formatRow(rank: Int, word: String, count: Int, wordWidth: Int, countWidth: Int = 1): String =
    "${rank.toString().padStart(2)}. ${word.padEnd(wordWidth)} " +
        "${count.toString().padStart(countWidth)} ${BAR_SYMBOL.repeat(count)}"

/**
 * Reads a resource (a file under src/main/resources) from the classpath,
 * or null if it doesn't exist. The class loader is what finds files that
 * were packaged with the program; paths here have no leading "/".
 */
fun readResource(path: String): String? =
    Thread.currentThread().contextClassLoader.getResource(path)?.readText()

fun main() {
    // Validate at the boundary: a missing file is a message, not a crash.
    val text = readResource(SAMPLE_RESOURCE)
    if (text == null) {
        println("Could not find $SAMPLE_RESOURCE on the classpath.")
        return
    }

    val words = normalizeWords(text)
    val counts = countWords(words)
    println("Words: ${words.size} total, ${counts.size} distinct")

    val top = topWords(counts, TOP_COUNT)
    val wordWidth = top.maxOfOrNull { it.first.length } ?: 0
    val countWidth = top.maxOfOrNull { it.second.toString().length } ?: 1
    println("Top $TOP_COUNT:")
    top.forEachIndexed { index, (word, count) ->
        println(formatRow(index + 1, word, count, wordWidth, countWidth))
    }
}
