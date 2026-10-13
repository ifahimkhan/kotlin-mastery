package l05_oop.lesson08_objects

/**
 * =====================================================================
 *  LESSON 05.8 — Objects and companion objects
 * =====================================================================
 *  GOAL   : Create exactly-one-instance singletons with `object`, put
 *           factories and constants in a `companion object`, and make
 *           one-off objects with object expressions.
 *  RUN    : ./gradlew -q run -Plesson=l05_oop.lesson08_objects.ObjectsAndCompanionsKt
 *  OUTPUT :
 *           AppConfig created (happens once, on first use)
 *           App: KotlinMastery v1.0
 *           Same instance everywhere: true
 *           Ticket #1 for Ada
 *           Ticket #2 for Grace
 *           Tickets issued: 2 (max per order: 4)
 *           fromFahrenheit(212.0) -> 100.0 C
 *           fromCelsius(-40.0)    -> -40.0 F
 *           Anonymous greeter: Hello, Linus!
 *  NEXT   : lesson09_enums_sealed/EnumsAndSealed.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: object = class + its only instance ───────────────
    // No constructor call: you use the name directly. It is created
    // lazily, the first time it is used (watch the first output line).
    println("App: ${AppConfig.APP_NAME} v${AppConfig.version}")
    val first = AppConfig
    val second = AppConfig
    println("Same instance everywhere: ${first === second}")

    // ── 2. CONCEPT: companion object ─────────────────────────────────
    // Members that belong to the CLASS, not to one instance. Called as
    // Ticket.issue(...), similar to Java's static methods. (They are real
    // members of a companion object, not JVM statics, unless you add
    // @JvmStatic: Java interop, level 11.)
    // The constructor is private, so issue() is the ONLY way in.
    val t1 = Ticket.issue("Ada")
    val t2 = Ticket.issue("Grace")
    println("Ticket #${t1.number} for ${t1.owner}")
    println("Ticket #${t2.number} for ${t2.owner}")
    println("Tickets issued: ${Ticket.issuedCount()} (max per order: ${Ticket.MAX_PER_ORDER})")

    // ── 3. CONCEPT: named factory functions ──────────────────────────
    // Two constructors with the same parameter type (Double) would
    // clash. Factories with clear names don't.
    println("fromFahrenheit(212.0) -> ${Temperature.fromFahrenheit(212.0).celsius} C")
    println("fromCelsius(-40.0)    -> ${Temperature.fromCelsius(-40.0).fahrenheit} F")

    // ── 4. CONCEPT: object expressions (anonymous objects) ───────────
    // A one-off implementation of an interface, without naming a class.
    val greeter =
        object : Greeter {
            override fun greet(name: String): String = "Hello, $name!"
        }
    println("Anonymous greeter: ${greeter.greet("Linus")}")

    // ── 5. WHY ───────────────────────────────────────────────────────
    // `object` gives you a thread-safe singleton in one word (Java needs
    // a pattern for it). Use it for stateless helpers, configuration, and
    // fixed values. Be careful with MUTABLE state in objects: it is
    // global, shared by the whole program, and hard to test.

    // ── 6. PITFALL: these lines don't compile ────────────────────────
    //   Ticket(99, "Eve")
    //   // ❌ Cannot access 'constructor(number: Int, owner: String): Ticket': it is private in 'l05_oop.lesson08_objects.Ticket'.
    //   AppConfig()   // an object has no constructor
    //   // ❌ Unresolved reference 'AppConfig'.

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Make Ticket.issue() refuse when MAX_PER_ORDER tickets exist.
    // b) Add Temperature.fromKelvin().
    // c) Write a second anonymous Greeter that greets in another language.
}

object AppConfig {
    const val APP_NAME = "KotlinMastery"
    val version: String = "1.0"

    init {
        println("AppConfig created (happens once, on first use)")
    }
}

class Ticket private constructor(val number: Int, val owner: String) {
    companion object {
        const val MAX_PER_ORDER = 4
        // Global mutable state: shared by the whole program and NOT
        // thread-safe (two threads could get the same number). Fine for
        // this demo; level 09 shows how to make it safe.
        private var issued = 0

        fun issue(owner: String): Ticket {
            issued += 1
            return Ticket(issued, owner)
        }

        fun issuedCount(): Int = issued
    }
}

class Temperature private constructor(val celsius: Double) {
    val fahrenheit: Double
        get() = celsius * FAHRENHEIT_RATIO + FAHRENHEIT_OFFSET

    companion object {
        private const val FAHRENHEIT_RATIO = 9.0 / 5.0
        private const val FAHRENHEIT_OFFSET = 32.0

        fun fromCelsius(celsius: Double): Temperature = Temperature(celsius)

        fun fromFahrenheit(fahrenheit: Double): Temperature =
            Temperature((fahrenheit - FAHRENHEIT_OFFSET) / FAHRENHEIT_RATIO)
    }
}

interface Greeter {
    fun greet(name: String): String
}
