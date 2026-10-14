package l08_generics.lesson07_reified

/**
 * =====================================================================
 *  LESSON 08.7 — Reified type parameters
 * =====================================================================
 *  GOAL   : Keep a type argument available at runtime with
 *           `inline fun <reified T>`, and use it for `is T`, `as? T`
 *           and `T::class`.
 *  RUN    : ./gradlew -q run -Plesson=l08_generics.lesson07_reified.ReifiedKt
 *  OUTPUT :
 *           typeName<String>() = String
 *           typeName<List<Int>>() = List
 *           onlyOfType<String>() = [two, four]
 *           onlyOfType<Int>() = [1, 5]
 *           stdlib filterIsInstance<String>() = [two, four]
 *           port = 8080, host = localhost
 *           port as String = null
 *           ports as List<String> = [80, 443] (not null!)
 *           Reading an element -> ClassCastException
 *  NEXT   : lesson08_type_system/TypeSystem.kt
 * =====================================================================
 */

// `inline` copies the function body into every call site, with the REAL
// type in place of T. `reified` asks for exactly that, so T survives.
inline fun <reified T> typeName(): String? = T::class.simpleName

// Our own version of the standard library's filterIsInstance.
inline fun <reified T> List<Any?>.onlyOfType(): List<T> {
    val result = mutableListOf<T>()
    for (item in this) {
        if (item is T) result.add(item) // `is T` needs a reified T
    }
    return result
}

// `as? T` returns null instead of crashing when the type doesn't match.
inline fun <reified T> Map<String, Any>.getTyped(key: String): T? = this[key] as? T

fun main() {
    // ── 1. THE PROBLEM ───────────────────────────────────────────────
    // A normal type parameter is erased (lesson 08.6), so this fails:
    //   fun <T> typeNameOf(): String? = T::class.simpleName
    //   ❌ Cannot use 'T' as reified type parameter. Use a class instead.

    // ── 2. CONCEPT: inline + reified ─────────────────────────────────
    println("typeName<String>() = ${typeName<String>()}")
    println("typeName<List<Int>>() = ${typeName<List<Int>>()}") // only the outer class is known

    // ── 3. CONCEPT: `is T` with a reified T ──────────────────────────
    val mixed: List<Any?> = listOf(1, "two", 3.0, "four", null, 5)
    println("onlyOfType<String>() = ${mixed.onlyOfType<String>()}")
    println("onlyOfType<Int>() = ${mixed.onlyOfType<Int>()}")
    println("stdlib filterIsInstance<String>() = ${mixed.filterIsInstance<String>()}")

    // ── 4. PRACTICAL: typed lookups in a loosely typed map ───────────
    val config: Map<String, Any> = mapOf("port" to 8080, "host" to "localhost")
    val port: Int? = config.getTyped("port") // T inferred from the declared type
    val host = config.getTyped<String>("host") // or given explicitly
    println("port = $port, host = $host")
    println("port as String = ${config.getTyped<String>("port")}")

    // ── 5. PITFALL: reified removes erasure only for the OUTER class ──
    // With T = List<String>, `as? T` can check "is it a List?" but NOT
    // "of Strings?" (the <String> part is still erased, lesson 08.6).
    // So a list of Ints sneaks through as a "List<String>":
    val settings: Map<String, Any> = mapOf("ports" to listOf(80, 443))
    val ports: List<String> = settings.getTyped("ports") ?: emptyList()
    println("ports as List<String> = $ports (not null!)")
    try { // (try/catch: later level)
        val first: String = ports[0] // the crash comes when an element is used
        println(first)
    } catch (e: ClassCastException) {
        println("Reading an element -> ${e::class.simpleName}")
    }
    // Fix: check the elements yourself, like isStringList in lesson 08.6.

    // ── 6. LIMITS ────────────────────────────────────────────────────
    // - Only for `inline` functions: classes can't have reified params.
    // - The body is copied at every call, so keep reified functions short.
    // - Java code can't call them (there is no real T to pass).

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Write `inline fun <reified T> List<Any?>.countOf(): Int`.
    // b) Write `inline fun <reified T> Any?.isA(): Boolean = this is T`
    //    and try 5.isA<Int>() and "x".isA<Int>().
    // c) Remove `reified` from typeName and read the error.
}
