package l08_generics.lesson06_star_erasure

/**
 * =====================================================================
 *  LESSON 08.6 — Star projection (*) and type erasure
 * =====================================================================
 *  GOAL   : Work with "a list of something" via List<*>, and understand
 *           why `is List<String>` can't be checked at runtime.
 *  RUN    : ./gradlew -q run -Plesson=l08_generics.lesson06_star_erasure.StarAndErasureKt
 *  OUTPUT :
 *           3 items, first = 1
 *           2 items, first = a
 *           0 items, first = null
 *           Holder<String> and Holder<Int> share one class: true (Holder)
 *           [a, b] is a list of Strings: true
 *           [1, 2] is a list of Strings: false
 *           hello is a list of Strings: false
 *           Unchecked cast 'worked': size = 2
 *           Reading an element -> ClassCastException
 *  NEXT   : lesson07_reified/Reified.kt
 * =====================================================================
 */

class Holder<T>(
    val value: T,
)

// List<*> = "a List of SOME type I don't know". Reading is fine
// (you get Any?), which is all this function needs.
fun describeSize(items: List<*>): String = "${items.size} items, first = ${items.firstOrNull()}"

// The safe way to ask "is this a list of Strings?": check the list,
// then check every element. Note: an EMPTY list passes too (`all` on no
// elements is true). This checks the current contents, not a declared type.
fun isStringList(value: Any): Boolean = value is List<*> && value.all { it is String }

fun main() {
    // ── 1. CONCEPT: star projection ──────────────────────────────────
    println(describeSize(listOf(1, 2, 3)))
    println(describeSize(listOf("a", "b")))
    println(describeSize(emptyList<Double>()))

    // You can't PUT anything into a MutableList<*>: its element type is
    // unknown, so nothing is guaranteed to fit.
    //   fun addX(list: MutableList<*>) { list.add("x") }
    //   ❌ Receiver type 'MutableList<*>' contains star projection which prohibits the use of
    //      'fun add(element: E): Boolean'.

    // ── 2. CONCEPT: type erasure ─────────────────────────────────────
    // The type arguments of an INSTANCE are erased at runtime: the JVM
    // sees just "Holder", not Holder<String> or Holder<Int>. (Generic
    // declarations themselves, like `class Holder<T>`, keep some type
    // information in the class file for tools; objects don't.)
    val sameClass = Holder("a").javaClass == Holder(1).javaClass
    println("Holder<String> and Holder<Int> share one class: $sameClass (${Holder("a").javaClass.simpleName})")

    // So the runtime can't answer "is this a List<String>?":
    //   if (value is List<String>) { }
    //   ❌ Cannot check for instance of erased type 'List<String>'.
    println("[a, b] is a list of Strings: ${isStringList(listOf("a", "b"))}")
    println("[1, 2] is a list of Strings: ${isStringList(listOf(1, 2))}")
    println("hello is a list of Strings: ${isStringList("hello")}")

    // ── 3. PITFALL: unchecked casts can lie ──────────────────────────
    // The compiler warns "Unchecked cast" because it CAN'T check it.
    // Suppressing the warning doesn't make the cast true:
    val numbers: Any = listOf(1, 2)

    @Suppress("UNCHECKED_CAST")
    val lying = numbers as List<String> // no crash yet...
    println("Unchecked cast 'worked': size = ${lying.size}")
    try { // (try/catch: later level)
        val first: String = lying[0] // ...the crash comes when an element is used
        println(first)
    } catch (e: ClassCastException) {
        println("Reading an element -> ${e::class.simpleName}")
    }

    // ── 4. WHY erasure? ──────────────────────────────────────────────
    // Generics were added to Java late (2004). Erasure kept new code
    // compatible with old JVMs and libraries. Kotlin runs on the same
    // JVM, so it inherits the rule. Workaround for functions: `reified`
    // (next lesson).

    // ── 5. TRY IT ────────────────────────────────────────────────────
    // a) Write `fun countNulls(items: List<*>): Int`.
    // b) Write `isIntList(value: Any)` like isStringList.
    // c) Uncomment the `is List<String>` line and read the error.
}
