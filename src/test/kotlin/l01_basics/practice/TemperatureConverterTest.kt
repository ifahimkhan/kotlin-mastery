package l01_basics.practice

import l01_basics.practice.temperature.celsiusToFahrenheit
import l01_basics.practice.temperature.celsiusToKelvin
import l01_basics.practice.temperature.describeCelsius
import l01_basics.practice.temperature.fahrenheitToCelsius
import l01_basics.practice.temperature.parseCelsius
import l01_basics.practice.temperature.roundTo2Decimals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TemperatureConverterTest {
    @Test
    fun `water boils at 212 F`() {
        assertEquals(212.0, celsiusToFahrenheit(100.0), DELTA)
    }

    @Test
    fun `minus 40 is the same in both scales`() {
        assertEquals(-40.0, celsiusToFahrenheit(-40.0), DELTA)
        assertEquals(-40.0, fahrenheitToCelsius(-40.0), DELTA)
    }

    @Test
    fun `converting there and back returns the original`() {
        assertEquals(37.0, fahrenheitToCelsius(celsiusToFahrenheit(37.0)), DELTA)
    }

    @Test
    fun `zero celsius is 273_15 kelvin`() {
        assertEquals(273.15, celsiusToKelvin(0.0), DELTA)
    }

    @Test
    fun `rounding removes floating point noise`() {
        assertEquals(0.3, roundTo2Decimals(0.1 + 0.2), DELTA)
    }

    @Test
    fun `description shows all three scales`() {
        assertEquals("100.0 C = 212.0 F = 373.15 K", describeCelsius(100.0))
    }

    @Test
    fun `parseCelsius accepts numbers and rejects text, NaN and Infinity`() {
        assertEquals(21.5, parseCelsius(" 21.5 "))
        assertNull(parseCelsius("warm"))
        assertNull(parseCelsius("NaN"))
        assertNull(parseCelsius("Infinity"))
        assertNull(parseCelsius("1e999"), "1e999 overflows to Infinity")
    }

    private companion object {
        const val DELTA = 1e-9
    }
}
