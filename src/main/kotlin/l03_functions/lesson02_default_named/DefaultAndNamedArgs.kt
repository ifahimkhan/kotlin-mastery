package l03_functions.lesson02_default_named

/**
 * =====================================================================
 *  LESSON 03.2 — Default and named arguments
 * =====================================================================
 *  GOAL   : Give parameters default values, call functions with named
 *           arguments, and know why Kotlin rarely needs overloads.
 *  RUN    : ./gradlew -q run -Plesson=l03_functions.lesson02_default_named.DefaultAndNamedArgsKt
 *  OUTPUT :
 *           Hello, Guest!
 *           Hello, Ada!
 *           Good morning, Ada!
 *           [INFO] Server started
 *           [WARN] Disk almost full
 *           [ERROR] (db) Connection lost
 *           Pizza: size=L, cheese=true, extras=[]
 *           Pizza: size=M, cheese=false, extras=[olives]
 *           Pizza: size=S, cheese=true, extras=[ham, pineapple]
 *           createUser(name=Lee, age=30, isAdmin=false)
 *           createUser(name=Kim, age=25, isAdmin=false)
 *  NEXT   : lesson03_vararg/VarargAndSpread.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: default values ───────────────────────────────────
    // `= "Guest"` is used when the caller leaves the argument out.
    println(greet())
    println(greet("Ada"))
    println(greet("Ada", "Good morning"))

    // ── 2. CONCEPT: named arguments ──────────────────────────────────
    // Name an argument to skip the ones before it, or just for clarity.
    log("Server started")
    log("Disk almost full", level = "WARN")
    log(level = "ERROR", tag = "db", message = "Connection lost") // any order!

    // Named arguments make Boolean flags readable:
    //   orderPizza("M", false)          // false... what was false?
    //   orderPizza("M", cheese = false) // ✅ obvious
    // (listOf/emptyList create lists — full story in level 06.)
    println(orderPizza())
    println(orderPizza(size = "M", cheese = false, extras = listOf("olives")))
    println(orderPizza("S", extras = listOf("ham", "pineapple")))

    // ── 3. CONCEPT: mixing positional and named ──────────────────────
    // The usual style: positional arguments first, then named ones.
    println(createUser("Lee", age = 30))
    // Since Kotlin 1.4 a named argument may be followed by positional ones,
    // as long as every argument is still in its declared position.
    println(createUser(name = "Kim", 25))

    // ── 4. WHY defaults beat overloads ───────────────────────────────
    // In Java you'd write 4 versions of `orderPizza` (overloads) to get
    // optional parameters. In Kotlin, ONE function with defaults does it.

    // ── 5. PITFALL: positional after an out-of-order named argument ──
    // log(level = "WARN", "Disk full")
    // ❌ Mixing named and positional arguments is not allowed unless the
    //    order of the arguments matches the order of the parameters.
    // `level` is the 2nd parameter but was passed 1st, so Kotlin can't tell
    // where "Disk full" belongs. Once you name an argument out of order,
    // name the rest too: log(level = "WARN", message = "Disk full")

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Add a `punctuation: String = "!"` parameter to greet.
    // b) Call orderPizza with only `cheese = false`. What size do you get?
    // c) Call createUser with isAdmin = true using a named argument.
}

fun greet(name: String = "Guest", greeting: String = "Hello"): String = "$greeting, $name!"

fun log(message: String, level: String = "INFO", tag: String = "") {
    if (tag.isEmpty()) {
        println("[$level] $message")
    } else {
        println("[$level] ($tag) $message")
    }
}

fun orderPizza(size: String = "L", cheese: Boolean = true, extras: List<String> = emptyList()): String =
    "Pizza: size=$size, cheese=$cheese, extras=$extras"

fun createUser(name: String, age: Int, isAdmin: Boolean = false): String =
    "createUser(name=$name, age=$age, isAdmin=$isAdmin)"
