package l07_functional.lesson04_references

/**
 * =====================================================================
 *  LESSON 07.4 — Function references (::)
 * =====================================================================
 *  GOAL   : Pass an EXISTING function where a lambda is expected, using
 *           `::`. Know the four kinds of references.
 *  RUN    : ./gradlew -q run -Plesson=l07_functional.lesson04_references.FunctionReferencesKt
 *  OUTPUT :
 *           lambda    : [false, true, false, true]
 *           reference : [false, true, false, true]
 *           lengths   : [3, 6, 2]
 *           upper     : [FUN, KOTLIN, IT]
 *           people    : [Person(name=Ada), Person(name=Alan)]
 *           greetings : [Hi, Ada!, Hi, Alan!]
 *           counter after forEach(counter::add): 10
 *           ref(4) = true
 *  NEXT   : lesson05_closures/Closures.kt
 * =====================================================================
 */

fun isEven(n: Int): Boolean = n % 2 == 0

data class Person(val name: String)

class Greeter(private val greeting: String) {
    fun greet(name: String): String = "$greeting, $name!"
}

class Counter {
    var total: Int = 0
        private set // only Counter itself can change it (level 05)

    fun add(amount: Int) {
        total += amount
    }
}

fun main() {
    val numbers = listOf(1, 2, 3, 4)

    // ── 1. CONCEPT: top-level function reference ─────────────────────
    // ::isEven means "the function isEven itself", NOT "call isEven".
    println("lambda    : ${numbers.map { isEven(it) }}")
    println("reference : ${numbers.map(::isEven)}")
    // Same result. Use the reference when the lambda would only forward
    // its parameter to one function.

    // ── 2. CONCEPT: member reference  Type::member ───────────────────
    // String::length is a function (String) -> Int.
    // String::uppercase is a function (String) -> String.
    val words = listOf("fun", "kotlin", "it")
    println("lengths   : ${words.map(String::length)}")
    println("upper     : ${words.map(String::uppercase)}")

    // ── 3. CONCEPT: constructor reference  ::ClassName ───────────────
    // ::Person is a function (String) -> Person.
    println("people    : ${listOf("Ada", "Alan").map(::Person)}")

    // ── 4. CONCEPT: bound reference  object::member ──────────────────
    // `hello::greet` is bound to THAT greeter, so it only needs the name.
    val hello = Greeter("Hi")
    println("greetings : ${listOf("Ada", "Alan").map(hello::greet)}")

    val counter = Counter()
    numbers.forEach(counter::add) // calls counter.add(n) for each n
    println("counter after forEach(counter::add): ${counter.total}")

    // ── 5. CONCEPT: a reference is a value with a function type ──────
    val ref: (Int) -> Boolean = ::isEven
    println("ref(4) = ${ref(4)}")

    // ── 6. PITFALL: () after the name calls the function ─────────────
    //   numbers.map(isEven)     // ❌ Function invocation 'isEven(...)' expected.
    //   numbers.map(isEven())   // ❌ No value passed for parameter 'n'.
    //   numbers.map(::isEven)   // ✅ the function itself
    // Without `::`, Kotlin thinks you want to CALL isEven. The fix is the `::`.

    // ── 7. WHY ───────────────────────────────────────────────────────
    // References read like English: map(::Person) = "make a Person from
    // each". They also reuse named, tested functions instead of copying
    // logic into lambdas.

    // ── 8. TRY IT ────────────────────────────────────────────────────
    // a) Write fun isVowel(c: Char): Boolean and use "kotlin".filter(::isVowel).
    // b) Use listOf(" a ", " b").map(String::trim).
    // c) Make a Greeter("Hello") and pass its greet to map with a bound reference.
}
