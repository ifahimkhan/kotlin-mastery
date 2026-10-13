package l01_basics.practice.temperature

import kotlin.math.roundToLong

/**
 * =====================================================================
 *  PRACTICE 01.A — Temperature converter (CLI)
 * =====================================================================
 *  GOAL   : Combine variables, types, operators, templates and input
 *           into a small, real program.
 *  RUN    : ./gradlew -q run -Plesson=l01_basics.practice.temperature.TemperatureConverterKt
 *  OUTPUT : (you type 100)
 *           Enter a temperature in Celsius: 100
 *           100.0 C = 212.0 F = 373.15 K
 *  DESIGN : The math lives in small functions with no input/output, so
 *           tests can check them (see TemperatureConverterTest). `main`
 *           only reads input and prints results. Keep these two jobs
 *           separate in every program you write.
 *  NOTE   : `if`/`else if` is a preview of level 02, null checks of
 *           level 04. Read them like plain English for now.
 *  EXTEND : Add Fahrenheit -> Celsius mode chosen by the user.
 * =====================================================================
 */

private const val FAHRENHEIT_RATIO = 9.0 / 5.0
private const val FAHRENHEIT_OFFSET = 32.0
private const val KELVIN_OFFSET = 273.15
private const val HUNDREDTHS = 100.0

/** Absolute zero: nothing can be colder. */
const val ABSOLUTE_ZERO_CELSIUS = -273.15

fun celsiusToFahrenheit(celsius: Double): Double = celsius * FAHRENHEIT_RATIO + FAHRENHEIT_OFFSET

fun fahrenheitToCelsius(fahrenheit: Double): Double = (fahrenheit - FAHRENHEIT_OFFSET) / FAHRENHEIT_RATIO

fun celsiusToKelvin(celsius: Double): Double = celsius + KELVIN_OFFSET

/** Rounds to 2 decimals so 0.1 + 0.2 style noise doesn't reach the user. */
fun roundTo2Decimals(value: Double): Double = (value * HUNDREDTHS).roundToLong() / HUNDREDTHS

/**
 * Turns user text into a Celsius value, or null if it isn't a usable number.
 * isFinite() matters: toDoubleOrNull() happily accepts "NaN" and "Infinity".
 */
fun parseCelsius(text: String): Double? = text.trim().toDoubleOrNull()?.takeIf { it.isFinite() }

fun describeCelsius(celsius: Double): String {
    val fahrenheit = roundTo2Decimals(celsiusToFahrenheit(celsius))
    val kelvin = roundTo2Decimals(celsiusToKelvin(celsius))
    return "$celsius C = $fahrenheit F = $kelvin K"
}

fun main() {
    print("Enter a temperature in Celsius: ")
    val input = readln().trim()
    val celsius = parseCelsius(input)

    // Validate at the boundary: reject text and physically impossible values.
    if (celsius == null) {
        println("\"$input\" is not a usable number.")
    } else if (celsius < ABSOLUTE_ZERO_CELSIUS) {
        println("$celsius C is below absolute zero ($ABSOLUTE_ZERO_CELSIUS C).")
    } else {
        println(describeCelsius(celsius))
    }
}
