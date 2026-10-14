package l08_generics.solutions

import l08_generics.Either
import l08_generics.GenericsTasks
import l08_generics.Stack

/**
 * Reference answers for Level 08. Try the exercises first!
 * Each answer explains WHY it is written this way.
 */
object GenericsSolutions : GenericsTasks {
    // T is whatever the list holds; the default must be the same type.
    override fun <T> firstOrDefault(
        items: List<T>,
        default: T,
    ): T = if (items.isEmpty()) default else items[0]

    // Two type parameters, swapped in the result type: the compiler checks
    // we really return (B, A).
    override fun <A, B> swap(pair: Pair<A, B>): Pair<B, A> = Pair(pair.second, pair.first)

    // The bound T : Comparable<T> is what makes `item > best` compile.
    override fun <T : Comparable<T>> largest(items: List<T>): T? {
        var best: T? = null
        for (item in items) {
            if (best == null || item > best) best = item
        }
        return best
    }

    // Classic merge: walk both lists with one index each and always take
    // the smaller head. `<=` takes from `first` on ties (stable). O(n + m).
    override fun <T : Comparable<T>> mergeSorted(
        first: List<T>,
        second: List<T>,
    ): List<T> =
        buildList {
            var i = 0
            var j = 0
            while (i < first.size && j < second.size) {
                if (first[i] <= second[j]) add(first[i++]) else add(second[j++])
            }
            while (i < first.size) add(first[i++])
            while (j < second.size) add(second[j++])
        }

    // A fresh object each call: two stacks never share items.
    override fun <T : Any> createStack(): Stack<T> = ListStack()

    // R : Any guarantees R itself is non-null. After the null check each
    // stored value is a real R, so the returned map holds no nulls.
    override fun <K, V, R : Any> mapValuesNotNull(
        map: Map<K, V>,
        transform: (V) -> R?,
    ): Map<K, R> =
        buildMap {
            for ((key, value) in map) {
                val transformed = transform(value)
                if (transformed != null) put(key, transformed)
            }
        }

    // Exhaustive `when` over the sealed type: no else needed, and adding a
    // third subtype would turn this into a compile error (a good thing).
    override fun <L, R, T> fold(
        either: Either<L, R>,
        onLeft: (L) -> T,
        onRight: (R) -> T,
    ): T =
        when (either) {
            is Either.Left -> onLeft(either.value)
            is Either.Right -> onRight(either.value)
        }

    // groupingBy keeps first-seen key order, like the task asks.
    override fun <T, K> groupCount(
        items: List<T>,
        keySelector: (T) -> K,
    ): Map<K, Int> = items.groupingBy(keySelector).eachCount()

    // `in T`: we only PUT Ts into `to`, so any list that can hold a T works.
    override fun <T> copyAll(
        from: List<T>,
        to: MutableList<in T>,
    ) {
        to.addAll(from)
    }

    // One null makes the whole answer null. filterNotNull() turns List<T?>
    // into List<T>, which is safe here because we checked first.
    override fun <T : Any> allOrNull(items: List<T?>): List<T>? =
        if (items.any { it == null }) null else items.filterNotNull()
}

/** The top of the stack is the END of the list, so push/pop are O(1). */
private class ListStack<T : Any> : Stack<T> {
    private val items = mutableListOf<T>()

    override val size: Int
        get() = items.size

    override val isEmpty: Boolean
        get() = items.isEmpty()

    override fun push(item: T) {
        items.add(item)
    }

    override fun pop(): T? = if (items.isEmpty()) null else items.removeAt(items.lastIndex)

    override fun peek(): T? = items.lastOrNull()
}
