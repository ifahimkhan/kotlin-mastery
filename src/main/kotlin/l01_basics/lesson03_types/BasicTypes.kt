package l01_basics.lesson03_types

import kotlin.math.roundToInt

/**
 * =====================================================================
 *  LESSON 01.3 — Basic types and conversions
 * =====================================================================
 *  GOAL   : Know Kotlin's built-in types (numbers, Boolean, Char),
 *           their limits, and how to convert between them safely.
 *  RUN    : ./gradlew -q run -Plesson=l01_basics.lesson03_types.BasicTypesKt
 *  OUTPUT :
 *           Byte  : -128 .. 127
 *           Short : -32768 .. 32767
 *           Int   : -2147483648 .. 2147483647
 *           Long  : -9223372036854775808 .. 9223372036854775807
 *           Int.MAX_VALUE + 1 = -2147483648  <-- overflow!
 *           As Long instead  = 2147483648
 *           7 / 2 = 3 (Int division drops the decimals)
 *           7 / 2.0 = 3.5
 *           0.1 + 0.2 = 0.30000000000000004
 *           Double -> Int: 9.99.toInt() = 9 (truncated, not rounded)
 *           Rounded properly: 10
 *           "42".toInt() + 1 = 43
 *           "abc".toIntOrNull() = null
 *           Char 'A' has code 65; next char is B
 *           isAdult = true, hasTicket = false, canEnter = false
 *  NEXT   : lesson04_strings/Strings.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: whole numbers ────────────────────────────────────
    // Byte (8 bit) < Short (16) < Int (32, the default) < Long (64).
    println("Byte  : ${Byte.MIN_VALUE} .. ${Byte.MAX_VALUE}")
    println("Short : ${Short.MIN_VALUE} .. ${Short.MAX_VALUE}")
    println("Int   : ${Int.MIN_VALUE} .. ${Int.MAX_VALUE}")
    println("Long  : ${Long.MIN_VALUE} .. ${Long.MAX_VALUE}")

    // ── 2. PITFALL: overflow wraps around silently ───────────────────
    val big = Int.MAX_VALUE
    println("Int.MAX_VALUE + 1 = ${big + 1}  <-- overflow!")
    println("As Long instead  = ${big.toLong() + 1}")
    // Suffix L makes a Long literal: val worldPopulation = 8_100_000_000L

    // ── 3. CONCEPT: decimal numbers ──────────────────────────────────
    // Double (64 bit, default) and Float (32 bit, needs suffix f: 2.5f).
    println("7 / 2 = ${7 / 2} (Int division drops the decimals)")
    println("7 / 2.0 = ${7 / 2.0}")

    // PITFALL: binary floating point can't store 0.1 exactly.
    // Never use Double for money — use Long cents or BigDecimal.
    println("0.1 + 0.2 = ${0.1 + 0.2}")

    // ── 4. CONCEPT: explicit conversion ──────────────────────────────
    // Kotlin NEVER converts number types for you (Java sometimes does):
    //   val i: Int = 5
    //   val l: Long = i   // ❌ Initializer type mismatch: expected 'Long', actual 'Int'.
    //   val l: Long = i.toLong() // ✅
    val price = 9.99
    println("Double -> Int: 9.99.toInt() = ${price.toInt()} (truncated, not rounded)")
    println("Rounded properly: ${price.roundToInt()}") // roundToInt() returns an Int

    // String -> number
    println("\"42\".toInt() + 1 = ${"42".toInt() + 1}")
    // "abc".toInt() would CRASH with NumberFormatException.
    // toIntOrNull() returns null instead — safer for user input (level 04).
    println("\"abc\".toIntOrNull() = ${"abc".toIntOrNull()}")

    // ── 5. CONCEPT: Char ─────────────────────────────────────────────
    // A Char is ONE character in single quotes. It has a numeric code.
    val letter = 'A'
    println("Char '$letter' has code ${letter.code}; next char is ${letter + 1}")

    // ── 6. CONCEPT: Boolean ──────────────────────────────────────────
    val age = 20
    val isAdult = age >= 18
    val hasTicket = false
    val canEnter = isAdult && hasTicket // && = AND, || = OR, ! = NOT
    println("isAdult = $isAdult, hasTicket = $hasTicket, canEnter = $canEnter")

    // ── 7. WHY no automatic conversion? ──────────────────────────────
    // Hidden conversions cause hidden bugs (lost precision, overflow).
    // Kotlin makes every conversion visible in the code.

    // ── 8. TRY IT ────────────────────────────────────────────────────
    // a) Compute the number of milliseconds in 30 days. Does Int overflow?
    // b) Print 10 / 3, 10 % 3 and 10 / 3.0. Explain each result.
    // c) Convert "3.14" to a Double and multiply it by 2.
}
