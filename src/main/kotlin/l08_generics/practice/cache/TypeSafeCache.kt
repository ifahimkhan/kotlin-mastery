package l08_generics.practice.cache

/**
 * =====================================================================
 *  PRACTICE 08.C — A type-safe cache with LRU eviction
 * =====================================================================
 *  GOAL   : Store values of DIFFERENT types in one cache, yet get each
 *           one back with its exact type and no casts at the call site.
 *           Evict the least recently used entry when the cache is full.
 *  RUN    : ./gradlew -q run -Plesson=l08_generics.practice.cache.TypeSafeCacheKt
 *  OUTPUT :
 *           user.name uppercased = ADA
 *           login.count + 1 = 4
 *           keys, oldest use first: [login.count, user.name]
 *           after adding theme: [user.name, theme]
 *           login.count was evicted: null
 *  DESIGN : The type lives in the KEY: a TypedKey<Int> can only store and
 *           return Ints. The single unchecked cast is hidden inside get()
 *           and is safe because put() is the only way in.
 *           Keys are compared by identity, so declare each key ONCE (as a
 *           constant) and reuse it.
 *           A cache is mutable by nature; the state is private and the
 *           API is small, so nobody can break it from outside.
 *           Note: even get() changes state (it marks the entry as most
 *           recently used). Single-threaded: two threads using one cache
 *           at once can corrupt it (thread safety: level 09).
 *  EXTEND : Add `fun <T : Any> getOrPut(key: TypedKey<T>, compute: () -> T): T`.
 * =====================================================================
 */

/** A key that remembers the type of the value stored under it. */
class TypedKey<T : Any>(
    val name: String,
) {
    override fun toString(): String = "TypedKey($name)"
}

private const val INITIAL_CAPACITY = 16
private const val LOAD_FACTOR = 0.75f
private const val ACCESS_ORDER = true // iterate from least to most recently USED

class TypeSafeCache(
    private val capacity: Int,
) {
    init {
        require(capacity > 0) { "capacity must be positive, was $capacity" }
    }

    // A LinkedHashMap in access order + removeEldestEntry = an LRU cache.
    private val entries =
        object : LinkedHashMap<TypedKey<*>, Any>(INITIAL_CAPACITY, LOAD_FACTOR, ACCESS_ORDER) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<TypedKey<*>, Any>?): Boolean =
                size > capacity
        }

    val size: Int
        get() = entries.size

    // T is tied to the key: put(LOGIN_COUNT, "three") does not compile.
    fun <T : Any> put(
        key: TypedKey<T>,
        value: T,
    ) {
        entries[key] = value
    }

    fun <T : Any> get(key: TypedKey<T>): T? {
        // Safe: put() only ever stores a T under a TypedKey<T>.
        @Suppress("UNCHECKED_CAST")
        return entries[key] as T?
    }

    /** Key names, least recently used first. */
    fun keyNames(): List<String> = entries.keys.map { it.name }
}

val USER_NAME = TypedKey<String>("user.name")
val LOGIN_COUNT = TypedKey<Int>("login.count")
val THEME = TypedKey<String>("theme")

private const val DEMO_CAPACITY = 2

fun main() {
    val cache = TypeSafeCache(capacity = DEMO_CAPACITY)
    cache.put(USER_NAME, "ada")
    cache.put(LOGIN_COUNT, 3)
    //   cache.put(LOGIN_COUNT, "three")
    //   ❌ Argument type mismatch: actual type is 'String', but 'Int' was expected.

    val name: String? = cache.get(USER_NAME) // a String, no cast needed
    println("user.name uppercased = ${name?.uppercase()}")

    val count: Int = cache.get(LOGIN_COUNT) ?: 0 // an Int, no cast needed
    println("login.count + 1 = ${count + 1}")

    // Reading counts as USING. We just read login.count, which made
    // user.name the least recently used. Reading user.name again flips
    // that: now login.count is the oldest use.
    cache.get(USER_NAME)
    println("keys, oldest use first: ${cache.keyNames()}")

    // The cache is full (2 entries): adding a third evicts the oldest use.
    cache.put(THEME, "dark")
    println("after adding theme: ${cache.keyNames()}")
    println("login.count was evicted: ${cache.get(LOGIN_COUNT)}")
}
