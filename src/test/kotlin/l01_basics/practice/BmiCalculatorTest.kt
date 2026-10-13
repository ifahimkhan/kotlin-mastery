package l01_basics.practice

import l01_basics.practice.bmi.bmiCategory
import l01_basics.practice.bmi.calculateBmi
import l01_basics.practice.bmi.parsePositiveNumber
import l01_basics.practice.bmi.roundTo1Decimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class BmiCalculatorTest {
    @Test
    fun `computes bmi from weight and height`() {
        assertEquals(22.9, roundTo1Decimal(calculateBmi(70.0, 1.75)), DELTA)
    }

    @Test
    fun `categories use upper-exclusive limits`() {
        assertEquals("Underweight", bmiCategory(18.4))
        assertEquals("Normal", bmiCategory(18.5))
        assertEquals("Normal", bmiCategory(24.9))
        assertEquals("Overweight", bmiCategory(25.0))
        assertEquals("Obese", bmiCategory(30.0))
    }

    @Test
    fun `rejects non-positive input`() {
        assertFailsWith<IllegalArgumentException> { calculateBmi(70.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { calculateBmi(-1.0, 1.75) }
        assertFailsWith<IllegalArgumentException> { calculateBmi(Double.NaN, 1.75) }
    }

    @Test
    fun `parsePositiveNumber rejects text, zero, negatives, NaN and Infinity`() {
        assertEquals(70.0, parsePositiveNumber(" 70 "))
        assertNull(parsePositiveNumber("heavy"))
        assertNull(parsePositiveNumber("0"))
        assertNull(parsePositiveNumber("-5"))
        assertNull(parsePositiveNumber("NaN"))
        assertNull(parsePositiveNumber("Infinity"))
    }

    private companion object {
        const val DELTA = 1e-9
    }
}
