package l11_advanced.exercises

import l11_advanced.AdvancedTasks
import l11_advanced.Cents
import l11_advanced.CountingList
import l11_advanced.Light
import l11_advanced.LightEvent
import l11_advanced.Route
import l11_advanced.Routes
import l11_advanced.TextHolder
import l11_advanced.Thermostat

/**
 * =====================================================================
 *  LEVEL 11 — EXERCISES (your turn!)
 * =====================================================================
 *  HOW    : Replace each TODO("...") with your answer. Read the task in
 *           ../AdvancedTasks.kt. Ex 4–7 ask for small classes: the stubs
 *           are at the bottom of this file. Change them freely (add
 *           properties, delegates, init blocks); keep the interface.
 *  CHECK  : ./gradlew exercises --tests "l11_*"
 *           or open src/test/kotlin/l11_advanced/AdvancedExercisesTest.kt
 *           and click ▶ next to the class name.
 *  STUCK? : Re-read the lesson in the hint. Still stuck? Peek at
 *           ../solutions/AdvancedSolutions.kt — but try first!
 * =====================================================================
 */
object AdvancedExercises : AdvancedTasks {
    // Hint: lesson02_result (runCatching)
    override fun safeDivide(
        a: Int,
        b: Int,
    ): Result<Int> = TODO("Ex 1: divide inside runCatching")

    // Hint: lesson02_result (runCatching, then mapCatching + require for the range)
    override fun parsePercent(text: String): Result<Int> = TODO("Ex 2: parse, then check 0..100")

    // Hint: lesson01_exceptions (use {} closes in reverse order, even on errors)
    override fun useBoth(
        log: MutableList<String>,
        failInside: Boolean,
    ): Unit = TODO("Ex 3: two nested use {} blocks")

    // Hint: lesson05_operator_overloading (operator fun plus/times, compareTo) + require
    override fun cents(amount: Long): Cents = MyCents(amount)

    // Hint: lesson03_class_delegation (`: MutableList<String> by inner`, override add)
    override fun countingList(): CountingList = MyCountingList()

    // Hint: lesson04_property_delegation (Delegates.vetoable) + require for `initial`
    override fun thermostat(initial: Int): Thermostat = MyThermostat(initial)

    // Hint: lesson04_property_delegation (a custom ReadWriteProperty, or a setter with `field`)
    override fun trimmedHolder(): TextHolder = MyTextHolder()

    // Hint: lesson06_dsl_builders (a builder class that implements Routes, then apply(block))
    override fun routes(block: Routes.() -> Unit): List<Route> = TODO("Ex 8: build the route list")

    // Hint: lesson08_state_machines (when on the event, then on the light; no else)
    override fun nextLight(
        light: Light,
        event: LightEvent,
    ): Light = TODO("Ex 9: traffic light transitions")

    // Hint: lesson07_annotations_reflection (declaredFields, isAnnotationPresent)
    override fun requiredFields(type: Class<*>): List<String> = TODO("Ex 10: read the @Required fields")
}

// ── Ex 4: finish this class ─────────────────────────────────────────
private class MyCents(
    override val amount: Long,
) : Cents {
    override fun plus(other: Cents): Cents = TODO("Ex 4: add the amounts")

    override fun times(factor: Int): Cents = TODO("Ex 4: multiply the amount")

    override fun compareTo(other: Cents): Int = TODO("Ex 4: compare the amounts")
}

// ── Ex 5: finish this class ─────────────────────────────────────────
// Tip: replace the whole class with one that delegates to a list.
private class MyCountingList : CountingList, MutableList<String> by mutableListOf() {
    override val adds: Int
        get() = TODO("Ex 5: count add(element) calls")
}

// ── Ex 6: finish this class ─────────────────────────────────────────
private class MyThermostat(
    initial: Int,
) : Thermostat {
    init {
        TODO("Ex 6: check that $initial is in range (require) and store it")
    }

    override var celsius: Int
        get() = TODO("Ex 6: read the temperature")
        set(value) = TODO("Ex 6: ignore values outside MIN_CELSIUS..MAX_CELSIUS")
}

// ── Ex 7: finish this class ─────────────────────────────────────────
private class MyTextHolder : TextHolder {
    override var text: String
        get() = TODO("Ex 7: read the text")
        set(value) = TODO("Ex 7: store the trimmed value")
}
