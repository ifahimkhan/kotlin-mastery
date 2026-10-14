// Kotlin code that is meant to be called from Java (see lesson 11.9).
// @file:JvmName renames the class Java sees for this file's top-level
// functions: Java writes Thermostats.toFahrenheit(...) instead of the
// default ThermostatsKt.toFahrenheit(...).
@file:JvmName("Thermostats")

package l11_advanced.lesson09_java_interop.api

import java.io.IOException

private const val FAHRENHEIT_RATIO = 9.0 / 5.0
private const val FAHRENHEIT_OFFSET = 32.0
private const val DEFAULT_TARGET = 21

// A top-level function: Java sees a static method on class Thermostats.
fun toFahrenheit(celsius: Double): Double = celsius * FAHRENHEIT_RATIO + FAHRENHEIT_OFFSET

// @JvmName gives the Java side a different method name.
@JvmName("describeRoom")
fun describe(room: String): String = "room $room"

// A nullable return: Java sees it annotated @Nullable, a hint to check for null.
fun findRoom(id: Int): String? = mapOf(1 to "kitchen", 2 to "office")[id]

// Kotlin has no checked exceptions. @Throws adds `throws IOException` to
// the JAVA signature, so Java callers must catch or declare it. It does
// not change anything for Kotlin callers.
@Throws(IOException::class)
fun loadProfile(name: String): String {
    if (name == "missing") throw IOException("no profile called $name")
    return "profile:$name"
}

// @JvmOverloads: Java has no default arguments, so the compiler also
// generates Thermostat(room) and Thermostat(room, target). It only helps
// when there ARE default parameters, and it drops them from the right:
// there is no Thermostat(room, eco) overload.
class Thermostat
    @JvmOverloads
    constructor(
        val room: String,
        val target: Int = DEFAULT_TARGET,
        val eco: Boolean = false,
    ) {
        override fun toString() = "Thermostat(room=$room, target=$target, eco=$eco)"

        companion object {
            // @JvmField: a plain public static field in Java (no getter).
            // Not allowed on private, open, override, delegated or
            // custom-getter properties. For a compile-time constant like
            // this one, `const val MAX_TARGET = 30` is the idiomatic choice
            // and is also a static field; @JvmField is for non-const values.
            @JvmField
            val MAX_TARGET: Int = 30

            // @JvmStatic: Java calls Thermostat.forOffice() instead of
            // Thermostat.Companion.forOffice() (which also keeps working).
            // It is only allowed in objects and companion objects.
            @JvmStatic
            fun forOffice(): Thermostat = Thermostat("office", eco = true)
        }
    }
