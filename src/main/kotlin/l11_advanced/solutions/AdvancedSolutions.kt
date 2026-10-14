package l11_advanced.solutions

import l11_advanced.AdvancedTasks
import l11_advanced.Cents
import l11_advanced.CountingList
import l11_advanced.Light
import l11_advanced.LightEvent
import l11_advanced.MAX_CELSIUS
import l11_advanced.MIN_CELSIUS
import l11_advanced.Required
import l11_advanced.Route
import l11_advanced.Routes
import l11_advanced.TextHolder
import l11_advanced.Thermostat
import l11_advanced.Tracked
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Reference answers for Level 11. Try the exercises first!
 * Each answer explains WHY it is written this way.
 */
object AdvancedSolutions : AdvancedTasks {
    // The division throws ArithmeticException for b == 0; runCatching turns
    // that into a Failure value instead of a crash.
    override fun safeDivide(
        a: Int,
        b: Int,
    ): Result<Int> = runCatching { a / b }

    // runCatching keeps the NumberFormatException; mapCatching then turns
    // require's IllegalArgumentException into a Failure too. Plain map
    // would let it escape as a crash.
    override fun parsePercent(text: String): Result<Int> =
        runCatching { text.trim().toInt() }
            .mapCatching { value ->
                require(value in 0..MAX_PERCENT) { "percent must be in 0..$MAX_PERCENT, was $value" }
                value
            }

    // Nested use {}: the inner resource closes first, and both close even
    // when the block throws. The exception still escapes to the caller.
    override fun useBoth(
        log: MutableList<String>,
        failInside: Boolean,
    ) {
        Tracked("a", log).use {
            Tracked("b", log).use {
                log += "work"
                check(!failInside) { "work failed" } // check throws IllegalStateException
            }
        }
    }

    override fun cents(amount: Long): Cents {
        require(amount >= 0) { "amount must be >= 0, was $amount" }
        return SimpleCents(amount)
    }

    override fun countingList(): CountingList = CountingStringList()

    override fun thermostat(initial: Int): Thermostat {
        require(initial in MIN_CELSIUS..MAX_CELSIUS) { "initial must be in $MIN_CELSIUS..$MAX_CELSIUS, was $initial" }
        return VetoThermostat(initial)
    }

    override fun trimmedHolder(): TextHolder = TrimmedTextHolder()

    // A builder that implements the Routes contract; apply(block) runs the
    // DSL with the builder as `this`. toList() hands out a copy.
    override fun routes(block: Routes.() -> Unit): List<Route> = RouteCollector().apply(block).routes.toList()

    // Event first: PowerOff/PowerOn don't care much about the colour.
    // No else branch, so a new Light or LightEvent is a compile error here.
    override fun nextLight(
        light: Light,
        event: LightEvent,
    ): Light =
        when (event) {
            LightEvent.PowerOff -> Light.Off
            LightEvent.PowerOn -> if (light == Light.Off) Light.Red else light
            LightEvent.Timer ->
                when (light) {
                    Light.Red -> Light.Green
                    Light.Green -> Light.Yellow
                    Light.Yellow -> Light.Red
                    Light.Off -> Light.Off
                }
        }

    // Java reflection is enough to read a RUNTIME annotation on a field.
    // Sorting makes the answer independent of the JVM's field order.
    override fun requiredFields(type: Class<*>): List<String> =
        type.declaredFields
            .filter { it.isAnnotationPresent(Required::class.java) }
            .map { it.name }
            .sorted()

    private const val MAX_PERCENT = 100
}

// Comparing by amount only; data class gives equals/hashCode for free.
private data class SimpleCents(
    override val amount: Long,
) : Cents {
    override fun plus(other: Cents): Cents = SimpleCents(amount + other.amount)

    override fun times(factor: Int): Cents {
        require(factor >= 0) { "factor must be >= 0, was $factor" }
        return SimpleCents(amount * factor)
    }

    override fun compareTo(other: Cents): Int = amount.compareTo(other.amount)
}

// `by inner` writes the ~30 other MutableList members for us. Only
// add(element) is overridden to count. toString is not delegated
// (lesson03_class_delegation), so forward it explicitly.
private class CountingStringList(
    private val inner: MutableList<String> = mutableListOf(),
) : CountingList,
    MutableList<String> by inner {
    override var adds: Int = 0
        private set

    override fun add(element: String): Boolean {
        adds++
        return inner.add(element)
    }

    override fun toString(): String = inner.toString()
}

// vetoable: return false to keep the old value.
private class VetoThermostat(
    initial: Int,
) : Thermostat {
    override var celsius: Int by Delegates.vetoable(initial) { _, _, new -> new in MIN_CELSIUS..MAX_CELSIUS }
}

// A reusable delegate: any String property can be "trimmed".
private class Trimmed : ReadWriteProperty<Any?, String> {
    private var value = ""

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): String = value

    override fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: String,
    ) {
        this.value = value.trim()
    }
}

private class TrimmedTextHolder : TextHolder {
    override var text: String by Trimmed()
}

private class RouteCollector : Routes {
    val routes = mutableListOf<Route>()

    override fun get(path: String) = add("GET", path)

    override fun post(path: String) = add("POST", path)

    private fun add(
        method: String,
        path: String,
    ) {
        require(path.startsWith("/")) { "path must start with '/', was '$path'" }
        routes += Route(method, path)
    }
}
