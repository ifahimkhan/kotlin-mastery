package l07_functional

/**
 * The list of Level 07 exercises.
 *
 * Both [l07_functional.exercises.FunctionalExercises] (your answers) and
 * [l07_functional.solutions.FunctionalSolutions] (reference answers)
 * implement this interface, so the SAME tests can check both.
 *
 * Many answers here RETURN a function. Remember: a lambda is a value,
 * so `return { x -> ... }` (or `= { x -> ... }`) is all it takes.
 */
interface FunctionalTasks {
    /**
     * Ex 1. Apply [f] to [x] exactly [n] times.  applyN({ it * 2 }, 3, 1) == 8
     * n == 0 returns x unchanged. A negative n is a caller bug: throw
     * IllegalArgumentException (use require).
     */
    fun applyN(f: (Int) -> Int, n: Int, x: Int): Int

    /**
     * Ex 2. Math-style composition: compose(outer, inner)(x) == outer(inner(x)).
     * The RIGHT-hand function runs first, the opposite of andThen in 07.3.
     * compose({ it + 1 }, { it * 10 })(2) == 21   (2 * 10 = 20, then + 1)
     */
    fun compose(outer: (Int) -> Int, inner: (Int) -> Int): (Int) -> Int

    /**
     * Ex 3. A counter: each call returns the next number, starting at [start].
     * val next = makeCounter(5); next() == 5; next() == 6; next() == 7
     * Two counters must not share their state.
     */
    fun makeCounter(start: Int): () -> Int

    /**
     * Ex 4. How many [items] match [predicate].
     * countMatching(listOf("a", "bb", "ccc")) { it.length > 1 } == 2
     */
    fun countMatching(items: List<String>, predicate: (String) -> Boolean): Int

    /**
     * Ex 5. Run the [validators] on [value] in order. Each validator returns
     * an error message, or null when the value is fine. Return the FIRST
     * error, or null when every validator passes (or the list is empty).
     * Stop at the first error: later validators must not run.
     */
    fun firstError(value: String, validators: List<(String) -> String?>): String?

    /**
     * Ex 6. Wrap a TWO-argument function [f] so each distinct (a, b)
     * combination is computed only ONCE; repeated calls with the same
     * pair return the remembered answer. Order matters: (2, 3) and
     * (3, 2) are different calls.
     */
    fun memoize2(f: (Int, Int) -> Long): (Int, Int) -> Long

    /**
     * Ex 7. Call [attempt] with 1, 2, 3, ... up to [maxAttempts] and stop at
     * the first call that returns true. Return the number of that attempt,
     * or null if none succeeded. maxAttempts == 0 makes no calls and returns
     * null; a negative maxAttempts is a caller bug: throw IllegalArgumentException.
     */
    fun retry(maxAttempts: Int, attempt: (Int) -> Boolean): Int?

    /**
     * Ex 8. Partial application: fix the FIRST of three arguments of [f]
     * and return a function that waits for the other two.
     * val digits = fixFirst({ a, b, c -> a * 100 + b * 10 + c }, 1)
     * digits(2, 3) == 123
     */
    fun fixFirst(f: (Int, Int, Int) -> Int, first: Int): (Int, Int) -> Int

    /**
     * Ex 9. Build a greeting from a configuration block.
     * Start from GreetingConfig() defaults, run [configure] on it, then
     * return "<greeting>, <name><punctuation>", upper-cased when shout is true.
     * buildGreeting { } == "Hello, World!"
     * buildGreeting { name = "Ada"; shout = true } == "HELLO, ADA!"
     */
    fun buildGreeting(configure: GreetingConfig.() -> Unit): String

    /**
     * Ex 10. Combine [steps] into ONE function that applies them in order
     * and returns EVERY stage: the input first, then each step's result.
     * tracedPipeline(listOf({ it.trim() }, { it.uppercase() }))("  hi ")
     *     == listOf("  hi ", "hi", "HI")
     * An empty list of steps gives listOf(input).
     */
    fun tracedPipeline(steps: List<(String) -> String>): (String) -> List<String>
}

/** Settings for Ex 9. Mutable on purpose: it's a builder you configure with a receiver lambda. */
class GreetingConfig {
    var greeting: String = "Hello"
    var name: String = "World"
    var punctuation: String = "!"
    var shout: Boolean = false
}
