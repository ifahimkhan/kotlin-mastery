package l07_functional.solutions

import l07_functional.FunctionalTasks
import l07_functional.GreetingConfig

/**
 * Reference answers for Level 07. Try the exercises first!
 * Each answer explains WHY it is written this way.
 */
object FunctionalSolutions : FunctionalTasks {
    // require rejects the caller bug first. Then a val result is rebuilt by
    // fold over 1..n (no var needed); 1..0 is empty, so n == 0 returns x.
    override fun applyN(f: (Int) -> Int, n: Int, x: Int): Int {
        require(n >= 0) { "n must not be negative, was $n" }
        return (1..n).fold(x) { acc, _ -> f(acc) }
    }

    // Returning a lambda: nothing runs now. Read outer(inner(x)) from the
    // inside out: inner runs first, its result goes into outer.
    override fun compose(outer: (Int) -> Int, inner: (Int) -> Int): (Int) -> Int = { x -> outer(inner(x)) }

    // `next` lives inside this call only, so every counter gets its own.
    // Return the current value, THEN move on, so the first call gives start.
    override fun makeCounter(start: Int): () -> Int {
        var next = start
        return {
            val current = next
            next += 1
            current
        }
    }

    // count { } is the standard library's version of "filter, then size",
    // without building the filtered list.
    override fun countMatching(items: List<String>, predicate: (String) -> Boolean): Int = items.count(predicate)

    // A plain loop with an early return stops at the first error, so the
    // remaining validators never run (important when they are expensive).
    override fun firstError(value: String, validators: List<(String) -> String?>): String? {
        for (validator in validators) {
            val error = validator(value)
            if (error != null) return error
        }
        return null
    }

    // Two arguments -> one key: the Pair (a to b). Pairs are data classes,
    // so (2 to 3) equals another (2 to 3) but not (3 to 2).
    // The cache is captured by the returned lambda (a closure): it survives
    // between calls but nobody outside can see or corrupt it.
    // Single-threaded and unbounded, like the lesson's version.
    override fun memoize2(f: (Int, Int) -> Long): (Int, Int) -> Long {
        val cache = mutableMapOf<Pair<Int, Int>, Long>()
        return { a, b -> cache.getOrPut(a to b) { f(a, b) } }
    }

    // `1..maxAttempts` is empty when maxAttempts == 0, so no calls happen.
    override fun retry(maxAttempts: Int, attempt: (Int) -> Boolean): Int? {
        require(maxAttempts >= 0) { "maxAttempts must not be negative, was $maxAttempts" }
        for (number in 1..maxAttempts) {
            if (attempt(number)) return number
        }
        return null
    }

    // The returned lambda remembers `first` and waits for the other two.
    override fun fixFirst(f: (Int, Int, Int) -> Int, first: Int): (Int, Int) -> Int =
        { second, third -> f(first, second, third) }

    // apply(configure) runs the receiver lambda on a fresh config and returns
    // that same config, so the defaults are visible to the block.
    override fun buildGreeting(configure: GreetingConfig.() -> Unit): String {
        val config = GreetingConfig().apply(configure)
        val text = "${config.greeting}, ${config.name}${config.punctuation}"
        return if (config.shout) text.uppercase() else text
    }

    // runningFold is fold that keeps every intermediate value: it starts
    // with the input itself, then adds each step's output. With no steps,
    // the list holds just the input.
    override fun tracedPipeline(steps: List<(String) -> String>): (String) -> List<String> =
        { input -> steps.runningFold(input) { acc, step -> step(acc) } }
}
