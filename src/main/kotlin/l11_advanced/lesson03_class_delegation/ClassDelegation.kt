package l11_advanced.lesson03_class_delegation

/**
 * =====================================================================
 *  LESSON 11.3 — Class delegation with `by`
 * =====================================================================
 *  GOAL   : Implement an interface by handing the work to another
 *           object, overriding only what you want to change. Build a
 *           decorator (counting, logging) without boilerplate.
 *  RUN    : ./gradlew -q run -Plesson=l11_advanced.lesson03_class_delegation.ClassDelegationKt
 *  OUTPUT :
 *           plain: Hello, Ada!
 *           loud: HELLO, ADA!
 *           loud farewell (not overridden): Bye, Ada.
 *           added: [a, b, b, c]
 *           add() was called 4 times, contains(b)=true
 *           log: get(apple)
 *           log: get(kiwi)
 *           prices: apple=3, kiwi=null
 *           this inside the delegate is NOT the wrapper: real-greeter
 *  NEXT   : lesson04_property_delegation/PropertyDelegation.kt
 * =====================================================================
 */

interface Greeter {
    fun greet(name: String): String

    fun farewell(name: String): String
}

class PoliteGreeter : Greeter {
    override fun greet(name: String) = "Hello, $name!"

    override fun farewell(name: String) = "Bye, $name."
}

// ── 1. CONCEPT: `: Greeter by inner` ────────────────────────────────
// The compiler writes every Greeter method for you, each one forwarding
// to `inner`. You override only the ones you want to change.
class LoudGreeter(
    private val inner: Greeter,
) : Greeter by inner {
    override fun greet(name: String) = inner.greet(name).uppercase()
}

// ── 2. CONCEPT: a decorator around a standard interface ─────────────
// MutableCollection has ~15 methods. Delegation forwards all of them;
// we only add counting to add(). Without `by` we'd write every method.
class CountingCollection<T>(
    private val inner: MutableCollection<T> = mutableListOf(),
) : MutableCollection<T> by inner {
    var addCalls = 0
        private set

    override fun add(element: T): Boolean {
        addCalls++
        return inner.add(element)
    }

    // PITFALL: `by` forwards only the INTERFACE's members. toString(),
    // equals() and hashCode() come from Any and are NOT forwarded: without
    // this override, printing shows "CountingCollection@6870f23e".
    // We fix toString here; equals() and hashCode() stay identity-based,
    // so two wrappers with equal contents are still NOT ==.
    override fun toString(): String = inner.toString()

    // Note: addAll() on the delegate does NOT call our add(). It goes
    // straight to `inner`. Override addAll too if you need to count it.
    override fun addAll(elements: Collection<T>): Boolean =
        // true if ANY add changed the collection (a set might refuse duplicates).
        elements.map { add(it) }.any { it }
}

// ── 3. CONCEPT: a logging wrapper for any interface ─────────────────
interface PriceList {
    fun get(item: String): Int?
}

class MapPriceList(
    private val prices: Map<String, Int>,
) : PriceList {
    override fun get(item: String): Int? = prices[item]
}

class LoggingPriceList(
    private val inner: PriceList,
    private val log: (String) -> Unit,
) : PriceList by inner {
    override fun get(item: String): Int? {
        log("get($item)")
        return inner.get(item)
    }
}

// Also note: the delegate is chosen ONCE, when the wrapper is constructed.
// `class Wrapper(var inner: X) : X by inner` keeps forwarding to the
// ORIGINAL object even if you later assign a new value to `inner`.

// ── 4. PITFALL: the delegate doesn't know about the wrapper ─────────
interface Named {
    val name: String

    fun introduce(): String = "I am $name"
}

class RealGreeterName : Named {
    override val name = "real-greeter"
}

class Renamed(
    inner: Named,
) : Named by inner {
    override val name = "wrapper"
}

private const val APPLE_PRICE = 3

fun main() {
    val polite = PoliteGreeter()
    val loud = LoudGreeter(polite)
    println("plain: ${polite.greet("Ada")}")
    println("loud: ${loud.greet("Ada")}")
    println("loud farewell (not overridden): ${loud.farewell("Ada")}")

    val counting = CountingCollection<String>()
    counting.add("a")
    counting.addAll(listOf("b", "b", "c"))
    println("added: $counting")
    println("add() was called ${counting.addCalls} times, contains(b)=${"b" in counting}")

    val prices = LoggingPriceList(MapPriceList(mapOf("apple" to APPLE_PRICE))) { println("log: $it") }
    val apple = prices.get("apple")
    val kiwi = prices.get("kiwi")
    println("prices: apple=$apple, kiwi=$kiwi")

    // introduce() runs INSIDE the delegate, where `name` is the delegate's
    // own name. Overriding `name` in the wrapper doesn't change it.
    val intro = Renamed(RealGreeterName()).introduce()
    println("this inside the delegate is NOT the wrapper: ${intro.removePrefix("I am ")}")

    // ── 5. WHY: delegation vs inheritance ────────────────────────────
    // Inheritance (level 05) needs an `open` class and ties you to its
    // internals. Delegation works with any interface and any instance,
    // even a final class you don't own. "Prefer composition."

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Make a CachingPriceList that remembers answers in a map.
    // b) Remove the addAll override. How many add() calls are counted now?
    // c) Override introduce() in Renamed. What prints?
}
