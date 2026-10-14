package l08_generics.practice.repository

/**
 * =====================================================================
 *  PRACTICE 08.B — A generic, immutable repository
 * =====================================================================
 *  GOAL   : Write ONE repository that stores any entity type with any
 *           id type, using a constrained type parameter
 *           `T : Entity<ID>`, and reuse it for books and members.
 *  RUN    : ./gradlew -q run -Plesson=l08_generics.practice.repository.GenericRepositoryKt
 *  OUTPUT :
 *           books v1: [Kotlin in Action, Effective Java]
 *           books v2: [Kotlin in Action 2e, Effective Java]
 *           v1 is unchanged: [Kotlin in Action, Effective Java]
 *           findById("isbn-2") = Book(id=isbn-2, title=Effective Java)
 *           findById("isbn-9") = null
 *           members: [Ada, Grace, Linus]
 *           after delete(1): [Grace, Linus]
 *  DESIGN : save() and delete() never change a repository; they RETURN
 *           a new one (copy-on-write). Old versions stay valid, which is
 *           easy to reason about and safe to share between threads
 *           (nothing is ever mutated). The price: every save copies the
 *           whole map, so saveAll of n items does O(n^2) work. Fine for
 *           learning and small data; real stores use persistent maps.
 *           Saving an id that exists REPLACES it (keeping its position);
 *           deleting a missing id changes nothing.
 *  EXTEND : Add `fun findBy(predicate: (T) -> Boolean): List<T>` to the
 *           interface and implement it once — it works for every entity.
 * =====================================================================
 */

/** Anything stored in a repository has an id of some type ID. */
interface Entity<ID> {
    val id: ID
}

interface Repository<T : Entity<ID>, ID> {
    fun findAll(): List<T>

    fun findById(id: ID): T?

    /** Returns a NEW repository that also contains [entity]. */
    fun save(entity: T): Repository<T, ID>

    /** Returns a NEW repository without the entity with [id]. */
    fun delete(id: ID): Repository<T, ID>
}

class InMemoryRepository<T : Entity<ID>, ID> private constructor(
    private val items: Map<ID, T>,
) : Repository<T, ID> {
    override fun findAll(): List<T> = items.values.toList()

    override fun findById(id: ID): T? = items[id]

    // `map + pair` builds a new map; an existing key keeps its position.
    override fun save(entity: T): Repository<T, ID> = InMemoryRepository(items + (entity.id to entity))

    override fun delete(id: ID): Repository<T, ID> = if (id in items) InMemoryRepository(items - id) else this

    companion object {
        /** The only way to create a repository: start empty. */
        fun <T : Entity<ID>, ID> empty(): Repository<T, ID> = InMemoryRepository(emptyMap())
    }
}

/** A generic extension: works for EVERY repository, whatever T and ID are. */
fun <T : Entity<ID>, ID> Repository<T, ID>.saveAll(entities: List<T>): Repository<T, ID> =
    entities.fold(this) { repository, entity -> repository.save(entity) }

data class Book(
    override val id: String,
    val title: String,
) : Entity<String>

data class Member(
    override val id: Int,
    val name: String,
) : Entity<Int>

fun main() {
    // Same repository class, two entity types, two id types.
    val booksV1 =
        InMemoryRepository
            .empty<Book, String>()
            .save(Book("isbn-1", "Kotlin in Action"))
            .save(Book("isbn-2", "Effective Java"))
    val booksV2 = booksV1.save(Book("isbn-1", "Kotlin in Action 2e")) // replace

    println("books v1: ${booksV1.findAll().map { it.title }}")
    println("books v2: ${booksV2.findAll().map { it.title }}")
    println("v1 is unchanged: ${booksV1.findAll().map { it.title }}")
    println("findById(\"isbn-2\") = ${booksV2.findById("isbn-2")}")
    println("findById(\"isbn-9\") = ${booksV2.findById("isbn-9")}")

    val members =
        InMemoryRepository
            .empty<Member, Int>()
            .saveAll(listOf(Member(1, "Ada"), Member(2, "Grace"), Member(3, "Linus")))
    println("members: ${members.findAll().map { it.name }}")
    println("after delete(1): ${members.delete(1).findAll().map { it.name }}")
    //   members.save(Book("isbn-3", "?")) // does not compile: a Book is not a Member
}
