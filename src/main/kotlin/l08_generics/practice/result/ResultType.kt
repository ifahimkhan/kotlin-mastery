package l08_generics.practice.result

/**
 * =====================================================================
 *  PRACTICE 08.A — Your own Result type: Outcome<T, E>
 * =====================================================================
 *  GOAL   : Build a generic success-or-error type with `out` variance
 *           and Nothing, give it map / flatMap / fold / getOrElse, and
 *           use it to validate a sign-up form step by step.
 *  RUN    : ./gradlew -q run -Plesson=l08_generics.practice.result.ResultTypeKt
 *  OUTPUT :
 *           signUp("Ada", "36") -> OK: Ada (36)
 *           signUp("  ", "20") -> Error: name must not be blank
 *           signUp("Kim", "abc") -> Error: "abc" is not a number
 *           signUp("Lee", "150") -> Error: age 150 is outside 0..130
 *           parseAge("41").map { it + 1 } = Success(value=42)
 *           parseAge("x").getOrElse { 0 } = 0
 *  DESIGN : A failed sign-up is a normal BUSINESS outcome, not a bug, so
 *           it is returned as a value (Failure) instead of thrown (the
 *           rule from level 05). Each step either passes its value on or
 *           stops the chain: flatMap does the plumbing.
 *  EXTEND : Add an e-mail field with its own SignupError case. The
 *           compiler will point at the `when` you must update.
 * =====================================================================
 */

/** Either a Success holding a T, or a Failure holding an E. */
sealed interface Outcome<out T, out E> {
    data class Success<out T>(
        val value: T,
    ) : Outcome<T, Nothing>

    data class Failure<out E>(
        val error: E,
    ) : Outcome<Nothing, E>
}

/** Change the success value; a Failure passes through untouched. */
fun <T, E, R> Outcome<T, E>.map(transform: (T) -> R): Outcome<R, E> =
    when (this) {
        is Outcome.Success -> Outcome.Success(transform(value))
        is Outcome.Failure -> Outcome.Failure(error)
    }

/** Run the next step that can ALSO fail; stop at the first Failure. */
fun <T, E, R> Outcome<T, E>.flatMap(transform: (T) -> Outcome<R, E>): Outcome<R, E> =
    when (this) {
        is Outcome.Success -> transform(value)
        is Outcome.Failure -> Outcome.Failure(error)
    }

/** Turn both cases into one result type. */
fun <T, E, R> Outcome<T, E>.fold(
    onSuccess: (T) -> R,
    onFailure: (E) -> R,
): R =
    when (this) {
        is Outcome.Success -> onSuccess(value)
        is Outcome.Failure -> onFailure(error)
    }

/** The success value, or a fallback computed from the error. */
fun <T, E> Outcome<T, E>.getOrElse(fallback: (E) -> T): T = fold(onSuccess = { it }, onFailure = fallback)

// ── The sign-up domain ──────────────────────────────────────────────

private const val MIN_AGE = 0
private const val MAX_AGE = 130

sealed interface SignupError {
    data class NotANumber(
        val text: String,
    ) : SignupError

    data class AgeOutOfRange(
        val age: Int,
    ) : SignupError

    data object BlankName : SignupError
}

data class Member(
    val name: String,
    val age: Int,
)

fun parseAge(text: String): Outcome<Int, SignupError> {
    val number = text.trim().toIntOrNull()
    return if (number == null) Outcome.Failure(SignupError.NotANumber(text)) else Outcome.Success(number)
}

fun checkAge(age: Int): Outcome<Int, SignupError> =
    if (age in MIN_AGE..MAX_AGE) Outcome.Success(age) else Outcome.Failure(SignupError.AgeOutOfRange(age))

fun checkName(name: String): Outcome<String, SignupError> {
    val trimmed = name.trim()
    return if (trimmed.isEmpty()) Outcome.Failure(SignupError.BlankName) else Outcome.Success(trimmed)
}

/** name -> age text -> age range -> Member. The first failing step wins. */
fun signUp(
    name: String,
    ageText: String,
): Outcome<Member, SignupError> =
    checkName(name).flatMap { validName ->
        parseAge(ageText)
            .flatMap { age -> checkAge(age) }
            .map { age -> Member(validName, age) }
    }

fun describeError(error: SignupError): String =
    when (error) {
        is SignupError.NotANumber -> "\"${error.text}\" is not a number"
        is SignupError.AgeOutOfRange -> "age ${error.age} is outside $MIN_AGE..$MAX_AGE"
        SignupError.BlankName -> "name must not be blank"
    }

fun main() {
    val attempts = listOf("Ada" to "36", "  " to "20", "Kim" to "abc", "Lee" to "150")
    for ((name, ageText) in attempts) {
        val message =
            signUp(name, ageText).fold(
                onSuccess = { member -> "OK: ${member.name} (${member.age})" },
                onFailure = { error -> "Error: ${describeError(error)}" },
            )
        println("signUp(\"$name\", \"$ageText\") -> $message")
    }

    println("parseAge(\"41\").map { it + 1 } = ${parseAge("41").map { it + 1 }}")
    println("parseAge(\"x\").getOrElse { 0 } = ${parseAge("x").getOrElse { 0 }}")
}
