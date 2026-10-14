package l08_generics.practice

import l08_generics.practice.repository.Book
import l08_generics.practice.repository.InMemoryRepository
import l08_generics.practice.repository.Member
import l08_generics.practice.repository.saveAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GenericRepositoryTest {
    private val ada = Member(1, "Ada")
    private val grace = Member(2, "Grace")

    @Test
    fun `save returns a new repository and keeps the old one`() {
        val empty = InMemoryRepository.empty<Member, Int>()
        val one = empty.save(ada)
        assertEquals(emptyList(), empty.findAll())
        assertEquals(listOf(ada), one.findAll())
    }

    @Test
    fun `saving an existing id replaces it in place`() {
        val repo = InMemoryRepository.empty<Member, Int>().saveAll(listOf(ada, grace))
        val renamed = repo.save(Member(1, "Ada L."))
        assertEquals(listOf("Ada L.", "Grace"), renamed.findAll().map { it.name })
    }

    @Test
    fun `findById returns null for unknown ids`() {
        val repo = InMemoryRepository.empty<Book, String>().save(Book("b1", "Title"))
        assertEquals(Book("b1", "Title"), repo.findById("b1"))
        assertNull(repo.findById("nope"))
    }

    @Test
    fun `delete removes only the given id and ignores unknown ids`() {
        val repo = InMemoryRepository.empty<Member, Int>().saveAll(listOf(ada, grace))
        assertEquals(listOf(grace), repo.delete(1).findAll())
        assertEquals(repo.findAll(), repo.delete(99).findAll(), "Deleting a missing id changes nothing")
    }
}
