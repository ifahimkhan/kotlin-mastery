package l08_generics

/**
 * The list of Level 08 exercises.
 *
 * Both [l08_generics.exercises.GenericsExercises] (your answers) and
 * [l08_generics.solutions.GenericsSolutions] (reference answers)
 * implement this interface, so the SAME tests can check both.
 *
 * Every function here is generic: `fun <T> ...` means the SAME code must
 * work for Ints, Strings, your own classes... The tests try several types.
 */
interface GenericsTasks {
    /** Ex 1. The first item, or [default] when the list is empty.  ([], "x") -> "x" */
    fun <T> firstOrDefault(
        items: List<T>,
        default: T,
    ): T

    /** Ex 2. Swap the two halves of a pair.  ("a", 1) -> (1, "a") */
    fun <A, B> swap(pair: Pair<A, B>): Pair<B, A>

    /** Ex 3. The largest item, or null for an empty list. Don't use maxOrNull(). */
    fun <T : Comparable<T>> largest(items: List<T>): T?

    /**
     * Ex 4. Merge two lists that are ALREADY sorted (ascending) into one
     * sorted list. On equal items, the one from [first] comes first
     * (a "stable" merge; the tests check this with items that compare
     * equal but are still different objects).
     * ([1, 4, 9], [2, 4]) -> [1, 2, 4, 4, 9]
     */
    fun <T : Comparable<T>> mergeSorted(
        first: List<T>,
        second: List<T>,
    ): List<T>

    /** Ex 5. Create a new, empty [Stack]. Implement the stack class yourself! */
    fun <T : Any> createStack(): Stack<T>

    /**
     * Ex 6. Transform every value; drop the entries where [transform]
     * returns null. Keys keep their original order.
     * ({a="1", b="x"}, toIntOrNull) -> {a=1}
     */
    fun <K, V, R : Any> mapValuesNotNull(
        map: Map<K, V>,
        transform: (V) -> R?,
    ): Map<K, R>

    /**
     * Ex 7. Turn an [Either] into one value: call [onLeft] for a Left,
     * [onRight] for a Right.
     */
    fun <L, R, T> fold(
        either: Either<L, R>,
        onLeft: (L) -> T,
        onRight: (R) -> T,
    ): T

    /**
     * Ex 8. Count items per key. Keys appear in first-seen order.
     * (["ant", "bee", "ape"], first letter) -> {a=2, b=1}
     */
    fun <T, K> groupCount(
        items: List<T>,
        keySelector: (T) -> K,
    ): Map<K, Int>

    /**
     * Ex 9. Append every item of [from] to [to]. [to] may hold a SUPERTYPE
     * of T, e.g. copy a List<String> into a MutableList<Any>.
     */
    fun <T> copyAll(
        from: List<T>,
        to: MutableList<in T>,
    )

    /**
     * Ex 10. All items, but only if NONE of them is null; otherwise null.
     * [1, 2] -> [1, 2],  [1, null] -> null,  [] -> []
     */
    fun <T : Any> allOrNull(items: List<T?>): List<T>?
}

/**
 * A last-in, first-out stack (Ex 5).
 *
 * `T : Any` (lesson03_constraints): pop() and peek() use null to mean
 * "empty". If T could itself be nullable, a popped null would be
 * impossible to tell apart from an empty stack.
 */
interface Stack<T : Any> {
    val size: Int
    val isEmpty: Boolean

    fun push(item: T)

    /** Removes and returns the top item, or null when the stack is empty. */
    fun pop(): T?

    /** Returns the top item without removing it, or null when empty. */
    fun peek(): T?
}

/** Either a Left (usually an error) or a Right (usually a success) (Ex 7). */
sealed interface Either<out L, out R> {
    data class Left<out L>(
        val value: L,
    ) : Either<L, Nothing>

    data class Right<out R>(
        val value: R,
    ) : Either<Nothing, R>
}
