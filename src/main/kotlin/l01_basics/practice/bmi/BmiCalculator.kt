package l01_basics.practice.bmi

import kotlin.math.roundToLong

/**
 * =====================================================================
 *  PRACTICE 01.B — BMI calculator (CLI)
 * =====================================================================
 *  GOAL   : Read two numbers, validate them, compute and classify.
 *  RUN    : ./gradlew -q run -Plesson=l01_basics.practice.bmi.BmiCalculatorKt
 *  OUTPUT : (you type 70 and 1.75)
 *           Weight in kg: 70
 *           Height in meters: 1.75
 *           Your BMI is 22.9 (Normal)
 *  NEW    : `require(...)` throws IllegalArgumentException with your
 *           message when a condition is false — a one-line guard.
 *           Previews: `when` and `if`/`return` (level 02), null checks
 *           and `takeIf` (level 04). Read them like plain English.
 *  EXTEND : Accept height in centimeters too (if value > 3, divide by 100).
 * =====================================================================
 */

private const val UNDERWEIGHT_LIMIT = 18.5
private const val NORMAL_LIMIT = 25.0
private const val OVERWEIGHT_LIMIT = 30.0
private const val TENTHS = 10.0

/**
 * Turns user text into a positive, finite number, or null if it isn't one.
 * isFinite() matters: toDoubleOrNull() happily accepts "NaN" and "Infinity".
 */
fun parsePositiveNumber(text: String): Double? =
    text.trim().toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }

/** BMI = weight (kg) / height² (m). Both values must be positive and finite. */
fun calculateBmi(weightKg: Double, heightM: Double): Double {
    require(weightKg.isFinite() && weightKg > 0) { "Weight must be a positive number, was $weightKg" }
    require(heightM.isFinite() && heightM > 0) { "Height must be a positive number, was $heightM" }
    return weightKg / (heightM * heightM)
}

fun bmiCategory(bmi: Double): String =
    when {
        bmi < UNDERWEIGHT_LIMIT -> "Underweight"
        bmi < NORMAL_LIMIT -> "Normal"
        bmi < OVERWEIGHT_LIMIT -> "Overweight"
        else -> "Obese"
    }

/** Rounds to one decimal, e.g. 22.857 -> 22.9 */
fun roundTo1Decimal(value: Double): Double = (value * TENTHS).roundToLong() / TENTHS

fun main() {
    print("Weight in kg: ")
    val weight = parsePositiveNumber(readln())
    print("Height in meters: ")
    val height = parsePositiveNumber(readln())

    // After this null check, Kotlin knows both values are real Doubles (level 04).
    if (weight == null || height == null) {
        println("Please enter two positive numbers, e.g. 70 and 1.75")
        return
    }

    val bmi = calculateBmi(weight, height)
    println("Your BMI is ${roundTo1Decimal(bmi)} (${bmiCategory(bmi)})")
}
