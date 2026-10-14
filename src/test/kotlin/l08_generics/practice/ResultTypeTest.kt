package l08_generics.practice

import l08_generics.practice.result.Member
import l08_generics.practice.result.Outcome
import l08_generics.practice.result.SignupError
import l08_generics.practice.result.checkAge
import l08_generics.practice.result.flatMap
import l08_generics.practice.result.fold
import l08_generics.practice.result.getOrElse
import l08_generics.practice.result.map
import l08_generics.practice.result.parseAge
import l08_generics.practice.result.signUp
import kotlin.test.Test
import kotlin.test.assertEquals

class ResultTypeTest {
    @Test
    fun `valid input becomes a member`() {
        assertEquals(Outcome.Success(Member("Ada", 36)), signUp(" Ada ", " 36 "))
    }

    @Test
    fun `the first failing step wins`() {
        assertEquals(Outcome.Failure(SignupError.BlankName), signUp("", "abc"))
        assertEquals(Outcome.Failure(SignupError.NotANumber("abc")), signUp("Kim", "abc"))
        assertEquals(Outcome.Failure(SignupError.AgeOutOfRange(-1)), signUp("Kim", "-1"))
    }

    @Test
    fun `age range edges are inclusive`() {
        assertEquals(Outcome.Success(0), checkAge(0))
        assertEquals(Outcome.Success(130), checkAge(130))
        assertEquals(Outcome.Failure(SignupError.AgeOutOfRange(131)), checkAge(131))
    }

    @Test
    fun `map and flatMap skip failures`() {
        val failure: Outcome<Int, String> = Outcome.Failure("bad")
        assertEquals(Outcome.Failure("bad"), failure.map { it + 1 })
        assertEquals(Outcome.Failure("bad"), failure.flatMap { Outcome.Success(it + 1) })
        assertEquals(Outcome.Success(2), Outcome.Success(1).map { it + 1 })
    }

    @Test
    fun `fold and getOrElse unwrap both cases`() {
        assertEquals("ok 5", parseAge("5").fold({ "ok $it" }, { "err" }))
        assertEquals(-1, parseAge("x").getOrElse { -1 })
        assertEquals(7, parseAge("7").getOrElse { -1 })
    }
}
