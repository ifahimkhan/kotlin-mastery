package l07_functional.practice.validation

/**
 * =====================================================================
 *  PRACTICE 07.A — Validation pipeline
 * =====================================================================
 *  GOAL   : Build form validation out of tiny, reusable functions:
 *           each rule is a lambda, rules are combined with
 *           higher-order functions, and ALL errors are collected.
 *  RUN    : ./gradlew -q run -Plesson=l07_functional.practice.validation.ValidationPipelineKt
 *  OUTPUT :
 *           Form 1: valid
 *           Form 2: 3 problem(s)
 *             - username: must be at least 3 characters
 *             - email: must contain @
 *             - age: must be between 13 and 120
 *           Form 3: 1 problem(s)
 *             - username: must not be blank
 *  DESIGN : A Rule is just a function (String) -> String?: null means
 *           "fine", text means "this is the problem". Small rules are
 *           made by factory functions (minLength(3)), chained per field
 *           with firstFailing(...), and applied to every field so the
 *           user sees all problems at once, not one per attempt.
 *           Fields keep their real types: age is an Int, so it gets an
 *           IntRule = (Int) -> String? instead of being turned back into
 *           text. (One Rule<T> type for both needs generics: level 08.)
 *  EXTEND : Add a rule that the username has no spaces, and a rule that
 *           the email ends with a known domain.
 * =====================================================================
 */

/** A single problem found in a form. */
data class ValidationError(val field: String, val message: String)

/** The data a user typed into a sign-up form. */
data class SignUpForm(val username: String, val email: String, val age: Int)

/** A rule checks one text value: null = OK, otherwise the error message. */
typealias Rule = (String) -> String?

/** The same idea for whole numbers. */
typealias IntRule = (Int) -> String?

private const val MIN_USERNAME = 3
private const val MAX_USERNAME = 20
private const val MIN_AGE = 13
private const val MAX_AGE = 120

// ── Rule factories: functions that BUILD rules ────────────────────────
fun notBlank(): Rule = { value -> if (value.isBlank()) "must not be blank" else null }

fun minLength(min: Int): Rule = { value -> if (value.length < min) "must be at least $min characters" else null }

fun maxLength(max: Int): Rule = { value -> if (value.length > max) "must be at most $max characters" else null }

fun mustContain(char: Char): Rule = { value -> if (char !in value) "must contain $char" else null }

fun inRange(range: IntRange): IntRule =
    { value -> if (value !in range) "must be between ${range.first} and ${range.last}" else null }

/** Combine rules: run them in order and report only the FIRST failure. */
fun firstFailing(vararg rules: Rule): Rule =
    { value ->
        var error: String? = null
        for (rule in rules) {
            error = rule(value)
            if (error != null) break
        }
        error
    }

// One combined rule per field. The order inside firstFailing matters:
// "must not be blank" is more helpful than "must be at least 3 characters".
private val usernameRule: Rule = firstFailing(notBlank(), minLength(MIN_USERNAME), maxLength(MAX_USERNAME))
private val emailRule: Rule = firstFailing(notBlank(), mustContain('@'))
private val ageRule: IntRule = inRange(MIN_AGE..MAX_AGE)

/** Checks every field and returns ALL problems (empty list = valid). */
fun validate(form: SignUpForm): List<ValidationError> {
    // field name -> the error for that field (or null). Each field uses
    // the rule for ITS type: text rules for text, an IntRule for the age.
    val results: List<Pair<String, String?>> =
        listOf(
            "username" to usernameRule(form.username),
            "email" to emailRule(form.email),
            "age" to ageRule(form.age),
        )
    return results.mapNotNull { (field, message) ->
        message?.let { ValidationError(field, it) }
    }
}

fun describe(label: String, errors: List<ValidationError>): String =
    if (errors.isEmpty()) {
        "$label: valid"
    } else {
        val lines = errors.joinToString("\n") { "  - ${it.field}: ${it.message}" }
        "$label: ${errors.size} problem(s)\n$lines"
    }

fun main() {
    val forms =
        listOf(
            SignUpForm(username = "ada_l", email = "ada@example.com", age = 36),
            SignUpForm(username = "al", email = "al.example.com", age = 9),
            SignUpForm(username = "   ", email = "x@y.z", age = 20),
        )
    forms.forEachIndexed { index, form ->
        println(describe("Form ${index + 1}", validate(form)))
    }
}
