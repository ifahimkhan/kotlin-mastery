package l02_control_flow.practice

import l02_control_flow.practice.guessing.CORRECT
import l02_control_flow.practice.guessing.TOO_HIGH
import l02_control_flow.practice.guessing.TOO_LOW
import l02_control_flow.practice.guessing.evaluateGuess
import l02_control_flow.practice.guessing.parseGuess
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NumberGuessingGameTest {
    @Test
    fun `guess below the secret is too low`() {
        assertEquals(TOO_LOW, evaluateGuess(secret = 42, guess = 10))
    }

    @Test
    fun `guess above the secret is too high`() {
        assertEquals(TOO_HIGH, evaluateGuess(secret = 42, guess = 99))
    }

    @Test
    fun `exact guess is correct`() {
        assertEquals(CORRECT, evaluateGuess(secret = 42, guess = 42))
    }

    @Test
    fun `off by one is still a hint`() {
        assertEquals(TOO_LOW, evaluateGuess(secret = 42, guess = 41))
        assertEquals(TOO_HIGH, evaluateGuess(secret = 42, guess = 43))
    }

    @Test
    fun `parseGuess accepts whole numbers inside 1 to 100 only`() {
        assertEquals(1, parseGuess("1"))
        assertEquals(100, parseGuess(" 100 "))
        assertNull(parseGuess("abc"))
        assertNull(parseGuess("0"))
        assertNull(parseGuess("101"))
        assertNull(parseGuess("-5"))
        assertNull(parseGuess("4.5"))
    }
}
