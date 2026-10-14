package l08_generics.lesson01_why

/**
 * =====================================================================
 *  LESSON 08.1 — Why generics?
 * =====================================================================
 *  GOAL   : See the problem generics solve (casts that crash at
 *           runtime) and write your first generic class, Box<T>.
 *  RUN    : ./gradlew -q run -Plesson=l08_generics.lesson01_why.WhyGenericsKt
 *  OUTPUT :
 *           anyBox holds: hello (String)
 *           After a cast: HELLO
 *           Wrong cast -> crashed with ClassCastException
 *           stringBox.value.uppercase() = HELLO
 *           intBox.value + 1 = 43
 *           stringBox now holds: bye
 *           List<String> first = Ada, Map<String, Int> Ada = 36
 *  NEXT   : lesson02_functions/GenericFunctions.kt
 * =====================================================================
 */

/** A box that can hold anything — but forgets WHAT it holds. */
class AnyBox(val value: Any)

/**
 * A GENERIC box. <T> is a type parameter: a placeholder for a real type
 * that is filled in each time you create a Box.
 */
class Box<T>(var value: T)

fun main() {
    // ── 1. THE PROBLEM: a box for "anything" ─────────────────────────
    val anyBox = AnyBox("hello")
    println("anyBox holds: ${anyBox.value} (${anyBox.value::class.simpleName})")

    // The compiler only knows the type is Any, so it won't let you use
    // the value as a String:
    //   val s: String = anyBox.value // ❌ Initializer type mismatch: expected 'String', actual 'Any'.
    // You have to CAST it yourself:
    val text = anyBox.value as String
    println("After a cast: ${text.uppercase()}")

    // PITFALL: a wrong cast compiles fine and crashes only at RUNTIME.
    val numberBox = AnyBox(42)
    try { // (try/catch: later level) — catches the crash so the lesson continues
        val wrong = numberBox.value as String
        println(wrong)
    } catch (e: ClassCastException) {
        println("Wrong cast -> crashed with ${e::class.simpleName}")
    }

    // ── 2. CONCEPT: a generic class ──────────────────────────────────
    val stringBox = Box("hello") // T = String (inferred from the argument)
    val intBox: Box<Int> = Box(42) // T = Int (written explicitly)
    // No casts: the compiler KNOWS stringBox.value is a String.
    println("stringBox.value.uppercase() = ${stringBox.value.uppercase()}")
    println("intBox.value + 1 = ${intBox.value + 1}")

    stringBox.value = "bye" // ✅ a String fits a Box<String>
    //   stringBox.value = 42 // ❌ Assignment type mismatch: actual type is 'Int', but 'String' was expected.
    println("stringBox now holds: ${stringBox.value}")

    // ── 3. You have used generics since level 06 ─────────────────────
    // List<String>, Map<String, Int> and Pair<A, B> are generic types.
    val names: List<String> = listOf("Ada", "Linus")
    val ages: Map<String, Int> = mapOf("Ada" to 36)
    println("List<String> first = ${names.first()}, Map<String, Int> Ada = ${ages["Ada"]}")

    // ── 4. WHY generics? ─────────────────────────────────────────────
    // - Mistakes move from RUNTIME (a crash for your user) to COMPILE
    //   TIME (a red line for you).
    // - No casts: the code says what it means.
    // - ONE class (Box) works for every type, so there is no StringBox,
    //   IntBox, UserBox...

    // ── 5. TRY IT ────────────────────────────────────────────────────
    // a) Create a Box<Boolean> and flip its value with `!`.
    // b) Uncomment the `stringBox.value = 42` line and read the error.
    // c) Write `class PairBox<A, B>(val first: A, val second: B)` and
    //    store a name and an age in it.
}
