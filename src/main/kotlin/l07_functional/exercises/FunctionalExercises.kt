package l07_functional.exercises

import l07_functional.FunctionalTasks
import l07_functional.GreetingConfig

/**
 * =====================================================================
 *  LEVEL 07 — EXERCISES (your turn!)
 * =====================================================================
 *  HOW    : Replace each TODO("...") with your answer.
 *           Keep everything else as it is.
 *  CHECK  : ./gradlew exercises --tests "l07_*"
 *           or open src/test/kotlin/l07_functional/FunctionalExercisesTest.kt
 *           and click ▶ next to the class name.
 *  STUCK? : Re-read the lesson in the hint. Still stuck? Peek at
 *           ../solutions/FunctionalSolutions.kt — but try first!
 * =====================================================================
 */
object FunctionalExercises : FunctionalTasks {
    // Hint: lesson03_higher_order (calling a function parameter) + repeat(n) { } or fold
    // + l05_oop/lesson04_visibility (require)
    override fun applyN(f: (Int) -> Int, n: Int, x: Int): Int = TODO("Ex 1: apply f n times")

    // Hint: lesson03_higher_order (returning a lambda). Which function must run first here?
    override fun compose(outer: (Int) -> Int, inner: (Int) -> Int): (Int) -> Int = TODO("Ex 2: outer(inner(x))")

    // Hint: lesson05_closures (a captured var outlives the call that created it)
    override fun makeCounter(start: Int): () -> Int = TODO("Ex 3: a counter starting at start")

    // Hint: lesson03_higher_order (call the predicate per item) or l06_collections/lesson06_filtering (count)
    override fun countMatching(items: List<String>, predicate: (String) -> Boolean): Int =
        TODO("Ex 4: count the matches")

    // Hint: lesson02_function_types (functions in a list: call each one like validator(value))
    // + a for loop with an early return, so later validators don't run
    override fun firstError(value: String, validators: List<(String) -> String?>): String? =
        TODO("Ex 5: first error or null")

    // Hint: lesson09_patterns (memoization idea) + l06_collections/lesson04_maps (getOrPut).
    // What can be the map KEY when there are two arguments? (a to b makes a Pair)
    override fun memoize2(f: (Int, Int) -> Long): (Int, Int) -> Long = TODO("Ex 6: remember answers per pair")

    // Hint: lesson03_higher_order (call the function parameter) + a for loop over 1..maxAttempts
    // with an early return + l05_oop/lesson04_visibility (require) for the negative case
    override fun retry(maxAttempts: Int, attempt: (Int) -> Boolean): Int? = TODO("Ex 7: first successful attempt")

    // Hint: lesson09_patterns (partial application). The returned lambda takes TWO parameters.
    override fun fixFirst(f: (Int, Int, Int) -> Int, first: Int): (Int, Int) -> Int =
        TODO("Ex 8: fix the first argument")

    // Hint: lesson08_receivers (calling a T.() -> Unit block) + lesson06_scope_functions (apply)
    override fun buildGreeting(configure: GreetingConfig.() -> Unit): String = TODO("Ex 9: configure, then format")

    // Hint: lesson09_patterns (pipelines) + l06_collections/lesson07_aggregation (runningFold keeps every stage)
    override fun tracedPipeline(steps: List<(String) -> String>): (String) -> List<String> =
        TODO("Ex 10: every stage of the pipeline")
}
