package l04_null_safety.lesson05_lateinit_lazy

/**
 * =====================================================================
 *  LESSON 04.5 — lateinit and lazy: non-null values that come later
 * =====================================================================
 *  GOAL   : Declare non-null properties whose value isn't ready at
 *           construction time, without falling back to `String?`.
 *  RUN    : ./gradlew -q run -Plesson=l04_null_safety.lesson05_lateinit_lazy.LateinitAndLazyKt
 *  OUTPUT :
 *           isInitialized before connect: false
 *           Reading too early -> UninitializedPropertyAccessException
 *           isInitialized after connect: true
 *           Connected to db://shop
 *           Report created (lazy not computed yet)
 *           computing summary...
 *           first access : 3 orders, total 60
 *           second access: 3 orders, total 60
 *  NEXT   : lesson06_platform/PlatformTypes.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: lateinit var ─────────────────────────────────────
    // "I promise to assign this before I read it." The type stays
    // non-null (String, not String?), so no ?. is needed later.
    val client = DatabaseClient()
    println("isInitialized before connect: ${client.isConnected()}")

    // PITFALL: reading before assigning throws a clear exception
    // (better than a mysterious NullPointerException, but still a crash).
    try {
        client.describe()
    } catch (e: UninitializedPropertyAccessException) {
        println("Reading too early -> ${e::class.simpleName}")
    }

    client.connect("db://shop")
    println("isInitialized after connect: ${client.isConnected()}")
    println(client.describe())

    // Rules for lateinit:
    //   - only `var` (it is assigned later, so it must be changeable)
    //   - not for primitive types: lateinit var count: Int
    //     ❌ 'lateinit' modifier is not allowed on properties of primitive types.
    //   - typical use: values set by a framework or a setup() method

    // ── 2. CONCEPT: by lazy ──────────────────────────────────────────
    // `val x by lazy { ... }` runs the block on FIRST access only, then
    // remembers the result. Good for expensive values you may not need.
    val report = Report(listOf(10, 20, 30))
    println("Report created (lazy not computed yet)")
    println("first access : ${report.summary}") // prints "computing..." first
    println("second access: ${report.summary}") // cached: no "computing..."

    // ── 3. WHY ───────────────────────────────────────────────────────
    // Without these, you'd write `var connection: String? = null` and
    // then `connection?.` / `connection!!` everywhere. lateinit and lazy
    // keep the type honest: "this is never null when you use it".
    //
    //   lateinit var -> assigned LATER by you,   mutable,   no primitives
    //   val by lazy  -> computed LATER by itself, read-only, thread-safe

    // ── 4. TRY IT ────────────────────────────────────────────────────
    // a) Remove the try/catch and let describe() crash. Read the message.
    // b) Add a second lazy property and prove it is computed only once.
    // c) Why can't `lazy` be used with `var`? (Hint: who would assign it?)
}

// lateinit and lazy live on properties, so these helpers are small classes.
// Classes are level 05; `by` (delegation) gets its own lesson in level 11.
private class DatabaseClient {
    private lateinit var url: String

    fun connect(address: String) {
        url = address
    }

    // `::url` is a reference to the property itself (more in a later level).
    fun isConnected(): Boolean = ::url.isInitialized

    fun describe(): String = "Connected to $url"
}

private class Report(private val orderTotals: List<Int>) {
    val summary: String by lazy {
        println("computing summary...")
        "${orderTotals.size} orders, total ${orderTotals.sum()}"
    }
}
