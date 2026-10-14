package l08_generics.exercises

import l08_generics.Either
import l08_generics.GenericsTasks
import l08_generics.Stack

/**
 * =====================================================================
 *  LEVEL 08 — EXERCISES (your turn!)
 * =====================================================================
 *  HOW    : Replace each TODO("...") with your answer.
 *           Keep the signatures as they are: they are generic, so your
 *           code must work for every type the tests use.
 *           Ex 5: finish the MyStack class at the bottom of this file.
 *  CHECK  : ./gradlew exercises --tests "l08_*"
 *           or open src/test/kotlin/l08_generics/GenericsExercisesTest.kt
 *           and click ▶ next to the class name.
 *  STUCK? : Re-read the lesson in the hint. Still stuck? Peek at
 *           ../solutions/GenericsSolutions.kt — but try first!
 * =====================================================================
 */
object GenericsExercises : GenericsTasks {
    // Hint: lesson02_functions (fun <T>, isEmpty, items[0])
    override fun <T> firstOrDefault(
        items: List<T>,
        default: T,
    ): T = TODO("Ex 1: first item or default")

    // Hint: lesson02_functions (several type parameters, Pair(first, second))
    override fun <A, B> swap(pair: Pair<A, B>): Pair<B, A> = TODO("Ex 2: swap the pair")

    // Hint: lesson03_constraints (T : Comparable<T> lets you use > ; see `biggest`)
    override fun <T : Comparable<T>> largest(items: List<T>): T? = TODO("Ex 3: largest item")

    // Hint: lesson03_constraints (compare with <=) + two indexes in a while loop (level 02)
    // Simpler but slower: (first + second).sorted() (l06_collections/lesson08_sorting) is stable too.
    override fun <T : Comparable<T>> mergeSorted(
        first: List<T>,
        second: List<T>,
    ): List<T> = TODO("Ex 4: merge two sorted lists")

    // Ex 5 is already wired up: finish the MyStack class at the bottom of this file.
    // Hint: lesson01_why (a generic class) + a private MutableList<T> inside it;
    //       T : Any (lesson03_constraints) keeps null free to mean "empty"
    override fun <T : Any> createStack(): Stack<T> = MyStack()

    // Hint: lesson03_constraints (R : Any) + l06_collections/lesson04_maps (mutableMapOf, map[key] = value)
    override fun <K, V, R : Any> mapValuesNotNull(
        map: Map<K, V>,
        transform: (V) -> R?,
    ): Map<K, R> = TODO("Ex 6: transform values, drop nulls")

    // Hint: lesson08_type_system (sealed + Nothing, exhaustive `when` with `is`)
    override fun <L, R, T> fold(
        either: Either<L, R>,
        onLeft: (L) -> T,
        onRight: (R) -> T,
    ): T = TODO("Ex 7: fold an Either")

    // Hint: lesson02_functions (several type parameters) + l06_collections/lesson04_maps (counting)
    // or l06_collections/lesson07_aggregation (groupingBy().eachCount())
    override fun <T, K> groupCount(
        items: List<T>,
        keySelector: (T) -> K,
    ): Map<K, Int> = TODO("Ex 8: count per key")

    // Hint: lesson05_variance_in (MutableList<in T> accepts adds of T)
    override fun <T> copyAll(
        from: List<T>,
        to: MutableList<in T>,
    ): Unit = TODO("Ex 9: copy into a list of a supertype")

    // Hint: lesson03_constraints (T : Any) + l04_null_safety/lesson07_safe_casts (filterNotNull)
    override fun <T : Any> allOrNull(items: List<T?>): List<T>? = TODO("Ex 10: all items or null")
}

/**
 * Ex 5: a stack backed by a private MutableList.
 * The top of the stack can be the END of the list (add / removeAt(lastIndex)).
 * You may change `val` members to `get() = ...` or add properties.
 */
private class MyStack<T : Any> : Stack<T> {
    override val size: Int
        get() = TODO("Ex 5: number of items")

    override val isEmpty: Boolean
        get() = TODO("Ex 5: true when there are no items")

    override fun push(item: T): Unit = TODO("Ex 5: add on top")

    override fun pop(): T? = TODO("Ex 5: remove and return the top, null when empty")

    override fun peek(): T? = TODO("Ex 5: return the top, null when empty")
}
