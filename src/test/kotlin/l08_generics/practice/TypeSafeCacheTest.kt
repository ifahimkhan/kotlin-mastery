package l08_generics.practice

import l08_generics.practice.cache.TypeSafeCache
import l08_generics.practice.cache.TypedKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class TypeSafeCacheTest {
    private val name = TypedKey<String>("name")
    private val age = TypedKey<Int>("age")
    private val city = TypedKey<String>("city")

    @Test
    fun `values come back with their own type`() {
        val cache = TypeSafeCache(capacity = 3)
        cache.put(name, "Ada")
        cache.put(age, 36)
        val storedName: String? = cache.get(name)
        val storedAge: Int? = cache.get(age)
        assertEquals("Ada", storedName)
        assertEquals(36, storedAge)
        assertNull(cache.get(city))
    }

    @Test
    fun `keys with the same name are still different keys`() {
        val cache = TypeSafeCache(capacity = 3)
        val otherName = TypedKey<String>("name")
        cache.put(name, "Ada")
        assertNull(cache.get(otherName), "Keys are compared by identity: reuse the same key object")
    }

    @Test
    fun `least recently used entry is evicted`() {
        val cache = TypeSafeCache(capacity = 2)
        cache.put(name, "Ada")
        cache.put(age, 36)
        cache.get(name) // name is now the most recently used
        cache.put(city, "Paris") // evicts age
        assertEquals(2, cache.size)
        assertNull(cache.get(age))
        assertEquals(listOf("name", "city"), cache.keyNames())
    }

    @Test
    fun `capacity must be positive`() {
        assertFailsWith<IllegalArgumentException> { TypeSafeCache(capacity = 0) }
    }
}
