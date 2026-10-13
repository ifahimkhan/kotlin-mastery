package l02_control_flow.practice

import l02_control_flow.practice.fizzbuzz.fizzBuzzWithIf
import l02_control_flow.practice.fizzbuzz.fizzBuzzWithMap
import l02_control_flow.practice.fizzbuzz.fizzBuzzWithWhen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FizzBuzzTest {
    @Test
    fun `first fifteen values are correct`() {
        val expected =
            listOf("1", "2", "Fizz", "4", "Buzz", "Fizz", "7", "8", "Fizz", "Buzz", "11", "Fizz", "13", "14", "FizzBuzz")
        assertEquals(expected, fizzBuzzWithIf(15))
    }

    @Test
    fun `all three versions agree`() {
        val withIf = fizzBuzzWithIf(100)
        assertEquals(withIf, fizzBuzzWithWhen(100))
        assertEquals(withIf, fizzBuzzWithMap(100))
    }

    @Test
    fun `zero limit gives an empty list`() {
        assertTrue(fizzBuzzWithIf(0).isEmpty())
        assertTrue(fizzBuzzWithWhen(0).isEmpty())
        assertTrue(fizzBuzzWithMap(0).isEmpty())
    }
}
