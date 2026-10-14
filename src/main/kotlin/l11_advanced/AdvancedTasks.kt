package l11_advanced

/**
 * The list of Level 11 exercises.
 *
 * Both [l11_advanced.exercises.AdvancedExercises] (your answers) and
 * [l11_advanced.solutions.AdvancedSolutions] (reference answers)
 * implement this interface, so the SAME tests can check both.
 *
 * Several exercises ask you to write a small class. The interfaces and
 * types those classes must follow are declared below the AdvancedTasks
 * interface in this file.
 */
interface AdvancedTasks {
    /** Ex 1. a / b as a Result: Success(quotient), or Failure(ArithmeticException) when b == 0. */
    fun safeDivide(
        a: Int,
        b: Int,
    ): Result<Int>

    /**
     * Ex 2. Parse a percentage. Success for whole numbers 0..100.
     * Failure(NumberFormatException) for text that is not a number,
     * Failure(IllegalArgumentException) for numbers outside 0..100.
     * The two failures must be distinguishable: a number outside the range
     * must NOT be reported as a NumberFormatException (which is itself a
     * subclass of IllegalArgumentException).
     * Leading/trailing spaces are allowed: " 42 " -> Success(42).
     */
    fun parsePercent(text: String): Result<Int>

    /**
     * Ex 3. Open [Tracked] resource "a", then "b" inside it, add "work" to
     * [log], and close both, LAST opened FIRST closed, even when
     * [failInside] is true (then throw IllegalStateException after "work").
     * Expected log: [open a, open b, work, close b, close a]
     */
    fun useBoth(
        log: MutableList<String>,
        failInside: Boolean,
    )

    /** Ex 4. Create a [Cents] amount. Negative amounts are a caller bug: IllegalArgumentException. */
    fun cents(amount: Long): Cents

    /** Ex 5. A MutableList<String> that counts calls to add(element). Hint: `by`. */
    fun countingList(): CountingList

    /**
     * Ex 6. A thermostat whose `celsius` silently ignores values outside
     * [MIN_CELSIUS]..[MAX_CELSIUS] (keeps the old value). An out-of-range
     * [initial] value is a caller bug: IllegalArgumentException.
     */
    fun thermostat(initial: Int): Thermostat

    /** Ex 7. A holder whose `text` is stored trimmed: text = "  hi " -> "hi". Starts as "". */
    fun trimmedHolder(): TextHolder

    /**
     * Ex 8. A tiny routing DSL. Collect the routes in the order they are
     * declared. Every path must start with "/" (IllegalArgumentException).
     * routes { get("/home"); post("/login") } ->
     *   [Route(GET, /home), Route(POST, /login)]
     */
    fun routes(block: Routes.() -> Unit): List<Route>

    /**
     * Ex 9. Traffic light state machine.
     *   Timer:    Red -> Green -> Yellow -> Red;  Off stays Off
     *   PowerOff: any state -> Off
     *   PowerOn:  Off -> Red; a light that is already on stays as it is
     */
    fun nextLight(
        light: Light,
        event: LightEvent,
    ): Light

    /** Ex 10. Names of the fields of [type] marked with [Required], sorted alphabetically. */
    fun requiredFields(type: Class<*>): List<String>
}

// ── Types used by the exercises ─────────────────────────────────────

/** Ex 3: a resource that logs when it is opened and closed. */
class Tracked(
    private val name: String,
    private val log: MutableList<String>,
) : AutoCloseable {
    init {
        log += "open $name"
    }

    override fun close() {
        log += "close $name"
    }
}

/** Ex 4: an amount of money in cents with operators. */
interface Cents : Comparable<Cents> {
    val amount: Long

    operator fun plus(other: Cents): Cents

    /** Multiplies by a NON-negative [factor]; a negative factor is a caller bug (require). */
    operator fun times(factor: Int): Cents
}

/** Ex 5. */
interface CountingList : MutableList<String> {
    /** How many times add(element) was called. */
    val adds: Int
}

const val MIN_CELSIUS = -50
const val MAX_CELSIUS = 60

/** Ex 6. */
interface Thermostat {
    var celsius: Int
}

/** Ex 7. */
interface TextHolder {
    var text: String
}

/** Ex 8. */
data class Route(
    val method: String,
    val path: String,
)

/** Ex 8: what the routes { } block can call. */
interface Routes {
    fun get(path: String)

    fun post(path: String)
}

/** Ex 9. */
sealed interface Light {
    data object Red : Light

    data object Green : Light

    data object Yellow : Light

    data object Off : Light
}

/** Ex 9. */
sealed interface LightEvent {
    data object Timer : LightEvent

    data object PowerOff : LightEvent

    data object PowerOn : LightEvent
}

/** Ex 10: marks a field that must be filled in. Kept at runtime so code can read it. */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Required
