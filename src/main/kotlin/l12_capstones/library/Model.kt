package l12_capstones.library

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

/*
 * The data of the library capstone. Everything is an immutable `val`:
 * a change to the library is a NEW LibraryState (l05_oop data classes,
 * copy()). That makes every rule a pure function that is easy to test.
 */

/** A title in the catalogue. [copies] is how many physical copies the library owns. */
@Serializable
data class Book(
    val isbn: String,
    val title: String,
    val author: String,
    val year: Int,
    val copies: Int,
)

@Serializable
data class Member(
    val id: String,
    val name: String,
)

/**
 * One borrowed copy. [renewed] is true after the single allowed renewal.
 * LocalDate has no built-in serializer, so we plug in our own (below).
 */
@Serializable
data class Loan(
    val isbn: String,
    val memberId: String,
    @Serializable(with = LocalDateIsoSerializer::class) val borrowedOn: LocalDate,
    @Serializable(with = LocalDateIsoSerializer::class) val dueOn: LocalDate,
    val renewed: Boolean = false,
)

/** The whole library. Lists keep insertion order, so listings are stable. */
@Serializable
data class LibraryState(
    val books: List<Book> = emptyList(),
    val members: List<Member> = emptyList(),
    val loans: List<Loan> = emptyList(),
)

/** A loan that is past its due date, with the fine owed so far (in cents). */
data class OverdueLoan(
    val loan: Loan,
    val daysLate: Long,
    val fineCents: Long,
)

/**
 * What happened when a member tried to borrow, return or renew.
 * Refusals are normal business outcomes, so they are values, not
 * exceptions (the l05_oop rule).
 */
sealed interface LoanResult {
    data class Borrowed(val loan: Loan) : LoanResult

    data class Returned(val loan: Loan, val fineCents: Long) : LoanResult

    data class Renewed(val loan: Loan) : LoanResult

    data object NoSuchBook : LoanResult

    data object NoSuchMember : LoanResult

    /** Every copy is already on loan. */
    data object NotAvailable : LoanResult

    /** The member already has the maximum number of loans. */
    data object LimitReached : LoanResult

    /** The member has an overdue loan and must return it first. */
    data object HasOverdueLoans : LoanResult

    /** This member does not have this book on loan. */
    data object NotBorrowed : LoanResult

    /** A loan can be renewed once, and never when it is already late. */
    data object CannotRenew : LoanResult
}

/** A rule application: the new state plus what happened. */
data class Step(
    val state: LibraryState,
    val result: LoanResult,
)

/** Loading saved data can fail; a corrupt file is reported, not thrown. */
sealed interface LoadResult {
    data class Loaded(val state: LibraryState) : LoadResult

    data class Corrupt(val message: String) : LoadResult
}

/** Writes a LocalDate as "2026-10-14" and reads it back. */
object LocalDateIsoSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("l12.LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
}
