package l08_generics.lesson05_variance_in

/**
 * =====================================================================
 *  LESSON 08.5 — Variance, part 2: `in` (consumers) and projections
 * =====================================================================
 *  GOAL   : Understand `in` (contravariance) on your own types and in
 *           Comparable, see why a Comparator works only through a
 *           use-site `in`, and use `out`/`in` at the USE SITE (type
 *           projections) for invariant types like Array and MutableList.
 *  RUN    : ./gradlew -q run -Plesson=l08_generics.lesson05_variance_in.VarianceInKt
 *  OUTPUT :
 *             sink got: hello
 *             sink got: 42
 *           sorted by length: [a, bb, ccc]
 *           copied: [x, y]
 *           numbers = [0.5, 1, 2, 3]
 *           anything = [start, 1, 2]
 *  NEXT   : lesson06_star_erasure/StarAndErasure.kt
 * =====================================================================
 */

/** Only CONSUMES T (takes it in), never returns it. So it can be `in T`. */
interface Sink<in T> {
    fun accept(item: T)
}

class PrintSink : Sink<Any> {
    override fun accept(item: Any) {
        println("  sink got: $item")
    }
}

// `Array<out Any>` = "an array of SOME subtype of Any; I will only READ it".
fun copyInto(
    from: Array<out Any>,
    to: Array<Any>,
) {
    for (i in from.indices) to[i] = from[i]
}

// `MutableList<in Int>` = "a list I may PUT Ints into" (Int, Number, Any...).
// Reading from it is still allowed, but you only get `Any?` back: the
// list might really be a MutableList<Any> holding Strings.
fun fillWithCount(
    target: MutableList<in Int>,
    count: Int,
) {
    for (n in 1..count) target.add(n)
}

fun main() {
    // ── 1. CONCEPT: `in` reverses the direction ──────────────────────
    // A sink that accepts ANY value can certainly accept Strings,
    // so a Sink<Any> may be used as a Sink<String>.
    val anySink: Sink<Any> = PrintSink()
    val stringSink: Sink<String> = anySink // ✅ thanks to `in T`
    stringSink.accept("hello")
    anySink.accept(42)
    // The other way round is unsafe and rejected:
    //   val s: Sink<String> = ...; val a: Sink<Any> = s
    //   ❌ Initializer type mismatch: expected 'Sink<Any>', actual 'Sink<String>'.

    // ── 2. CONCEPT: Comparable is `in`, Comparator uses a use-site `in`
    // Declaration-site `in` in the standard library: Comparable is
    // declared `Comparable<in T>`, exactly like our Sink<in T> above.
    // Comparator is NOT: on the JVM it is Java's Comparator<T>, which
    // is INVARIANT. So this assignment is rejected:
    val byLength: Comparator<CharSequence> = compareBy { it.length }
    //   val forStrings: Comparator<String> = byLength
    //   ❌ Initializer type mismatch: expected 'Comparator<String>', actual 'Comparator<CharSequence>'.
    // Yet sorting Strings with it works. Why? sortedWith declares its
    // parameter as `comparator: Comparator<in T>`: the `in` is written
    // at the USE SITE (a type projection, see section 4).
    println("sorted by length: ${listOf("ccc", "a", "bb").sortedWith(byLength)}")

    // ── 3. RULE: `in` may appear only where values go IN ─────────────
    //   class Consumer<in T> { fun get(): T = TODO() }
    //   ❌ Type parameter 'T' is declared as 'in' but occurs in 'out' position in type 'T (of class Consumer<in T>)'.

    // ── 4. CONCEPT: use-site variance (type projections) ─────────────
    // Array<T> is invariant: an Array<String> is NOT an Array<Any>.
    //   val texts: Array<String> = arrayOf("x")
    //   val objects: Array<Any> = texts
    //   ❌ Initializer type mismatch: expected 'Array<Any>', actual 'Array<String>'.
    // But copyInto promises to only READ `from` (Array<out Any>), so
    // an Array<String> is accepted there:
    val words: Array<String> = arrayOf("x", "y")
    val target: Array<Any> = arrayOf("", "")
    copyInto(words, target)
    println("copied: ${target.joinToString(prefix = "[", postfix = "]")}")
    // Writing to an `out`-projected array is forbidden:
    //   from[0] = "z"
    //   ❌ Receiver type 'Array<out Any>' contains out projection which prohibits the use of
    //      'fun set(index: Int, value: T): Unit'.

    // `in` at the use site: any list that can HOLD Ints may be filled.
    val numbers: MutableList<Number> = mutableListOf(0.5)
    val anything: MutableList<Any> = mutableListOf("start")
    fillWithCount(numbers, 3)
    fillWithCount(anything, 2)
    println("numbers = $numbers")
    println("anything = $anything")

    // ── 5. RULE OF THUMB ─────────────────────────────────────────────
    //   out T  -> PRODUCER: only returns T      Source<Dog>  is a Source<Animal>
    //   in T   -> CONSUMER: only takes T        Sink<Animal> is a Sink<Dog>
    //   (none) -> both: INVARIANT               MutableList<Dog> is only itself
    // Memory hook: "Producer out, Consumer in".

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Write `fun addGreeting(target: MutableList<in String>)` and call
    //    it with a MutableList<Any>.
    // b) Remove `out` from copyInto's `from` parameter. What breaks?
    // c) Make `interface Logger<in T> { fun log(item: T) }` and use a
    //    Logger<Any> where a Logger<Int> is expected.
}
