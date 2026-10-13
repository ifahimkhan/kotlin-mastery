package l02_control_flow.practice

import l02_control_flow.practice.grades.averageScore
import l02_control_flow.practice.grades.bestScore
import l02_control_flow.practice.grades.letterGrade
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GradeCalculatorTest {
    @Test
    fun `average keeps decimals`() {
        assertEquals(79.75, averageScore(listOf(78, 92, 85, 64)), DELTA)
        assertEquals(100.0, averageScore(listOf(100)), DELTA)
    }

    @Test
    fun `letter grade boundaries`() {
        assertEquals('A', letterGrade(90.0))
        assertEquals('B', letterGrade(89.99))
        assertEquals('C', letterGrade(79.75))
        assertEquals('D', letterGrade(60.0))
        assertEquals('F', letterGrade(59.5))
    }

    @Test
    fun `best score is the maximum`() {
        assertEquals(92, bestScore(listOf(78, 92, 85, 64)))
        assertEquals(5, bestScore(listOf(5)))
    }

    @Test
    fun `invalid input is rejected`() {
        assertFailsWith<IllegalArgumentException> { averageScore(emptyList()) }
        assertFailsWith<IllegalArgumentException> { averageScore(listOf(50, 101)) }
        assertFailsWith<IllegalArgumentException> { letterGrade(-0.5) }
        assertFailsWith<IllegalArgumentException> { bestScore(emptyList()) }
    }

    private companion object {
        const val DELTA = 1e-9
    }
}
