package l08_generics.lesson04_variance_out

/**
 * =====================================================================
 *  LESSON 08.4 — Variance, part 1: `out` (producers)
 * =====================================================================
 *  GOAL   : Understand why a List<String> may be used as a List<Any>
 *           but a MutableList<String> may NOT be a MutableList<Any>,
 *           and declare your own `out` type parameters.
 *  RUN    : ./gradlew -q run -Plesson=l08_generics.lesson04_variance_out.VarianceOutKt
 *  OUTPUT :
 *           As List<Any>: [a, b]
 *           printAll got 2 items: a, b
 *           numbers.next() = 7
 *           Best of the shelter: Rex
 *  NEXT   : lesson05_variance_in/VarianceIn.kt
 * =====================================================================
 */

/** Only PRODUCES T (returns it), never takes T in. So it can be `out T`. */
interface Source<out T> {
    fun next(): T
}

class RepeatSource<out T>(
    private val value: T,
) : Source<T> {
    override fun next(): T = value
}

// (classes and inheritance: level 05)
open class Animal(
    val name: String,
)

class Dog(
    name: String,
) : Animal(name)

/** Any Source of Animals works here, including a Source<Dog>. */
fun bestOf(shelter: Source<Animal>): String = shelter.next().name

fun printAll(items: List<Any>) {
    println("printAll got ${items.size} items: ${items.joinToString()}")
}

fun main() {
    // ── 1. CONCEPT: List is covariant ────────────────────────────────
    // A String IS an Any, so a list of Strings may be used as a list of
    // Any. This works because List is declared as `List<out E>`.
    val strings: List<String> = listOf("a", "b")
    val anything: List<Any> = strings
    println("As List<Any>: $anything")
    printAll(strings) // a List<String> passed where List<Any> is expected

    // ── 2. PITFALL: MutableList is NOT ───────────────────────────────
    //   val words: MutableList<String> = mutableListOf("a")
    //   val objects: MutableList<Any> = words
    //   ❌ Initializer type mismatch: expected 'MutableList<Any>', actual 'MutableList<String>'.
    // WHY: if it compiled, this would be legal...
    //   objects.add(42)          // an Int goes into the list of Strings
    //   val w: String = words[1] // ...and crashes here: 42 is not a String
    // A MutableList both TAKES T (add) and GIVES T (get), so it must stay
    // exactly MutableList<String>. That is called INVARIANT.

    // ── 3. CONCEPT: your own `out` type ──────────────────────────────
    val ints: Source<Int> = RepeatSource(7)
    val numbers: Source<Number> = ints // ✅ Int is a Number, Source is `out`
    println("numbers.next() = ${numbers.next()}")

    val dogs: Source<Dog> = RepeatSource(Dog("Rex"))
    println("Best of the shelter: ${bestOf(dogs)}") // Source<Dog> used as Source<Animal>

    // ── 4. RULE: what `out` allows ───────────────────────────────────
    // `out T` may appear only where values come OUT: return types and
    // read-only `val` properties (a `var` would also let values IN).
    // Using it as a parameter type is an error:
    //   class Producer<out T>(private val v: T) { fun replace(nv: T) {} }
    //   ❌ Type parameter 'T' is declared as 'out' but occurs in 'in' position in type 'T (of class Producer<out T>)'.
    // (private members are exempt: they can't leak T to the outside.)

    // ── 5. WHY variance? ─────────────────────────────────────────────
    // Without `out` you couldn't pass a List<Dog> to a function that
    // takes List<Animal>, even though it's perfectly safe to READ dogs
    // as animals. `out` tells the compiler "this type only gives T".

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Make `interface Factory<out T> { fun create(): T }` and use a
    //    Factory<Dog> as a Factory<Animal>.
    // b) Remove `out` from Source and read the new compiler error.
    // c) Uncomment the MutableList lines in section 2.
}
