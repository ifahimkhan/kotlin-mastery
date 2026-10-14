package l07_functional.practice

import l07_functional.practice.validation.SignUpForm
import l07_functional.practice.validation.ValidationError
import l07_functional.practice.validation.firstFailing
import l07_functional.practice.validation.inRange
import l07_functional.practice.validation.minLength
import l07_functional.practice.validation.mustContain
import l07_functional.practice.validation.notBlank
import l07_functional.practice.validation.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidationPipelineTest {
    @Test
    fun `rule factories build working rules`() {
        assertNull(notBlank()("x"))
        assertEquals("must not be blank", notBlank()("  "))
        assertEquals("must be at least 3 characters", minLength(3)("ab"))
        assertNull(minLength(3)("abc"))
        assertEquals("must contain @", mustContain('@')("a.b"))
        assertEquals("must be between 1 and 5", inRange(1..5)(6))
        assertEquals("must be between 1 and 5", inRange(1..5)(0))
        assertNull(inRange(1..5)(5))
    }

    @Test
    fun `firstFailing reports only the first failing rule`() {
        val rule = firstFailing(notBlank(), minLength(3))
        assertEquals("must not be blank", rule(""), "Blank fails first, so the length rule is not reported")
        assertEquals("must be at least 3 characters", rule("ab"))
        assertNull(rule("abc"))
        assertNull(firstFailing()("anything"), "No rules means no errors")
    }

    @Test
    fun `a valid form has no errors`() {
        assertTrue(validate(SignUpForm("ada_l", "ada@example.com", 36)).isEmpty())
    }

    @Test
    fun `validate collects errors from every field in order`() {
        val errors = validate(SignUpForm("al", "al.example.com", 9))
        assertEquals(
            listOf(
                ValidationError("username", "must be at least 3 characters"),
                ValidationError("email", "must contain @"),
                ValidationError("age", "must be between 13 and 120"),
            ),
            errors,
        )
    }

    @Test
    fun `age limits are inclusive`() {
        assertTrue(validate(SignUpForm("ada", "a@b", 13)).isEmpty())
        assertTrue(validate(SignUpForm("ada", "a@b", 120)).isEmpty())
        assertEquals(1, validate(SignUpForm("ada", "a@b", 121)).size)
    }
}
