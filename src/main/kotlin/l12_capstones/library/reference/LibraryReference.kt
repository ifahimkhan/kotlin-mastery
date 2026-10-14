package l12_capstones.library.reference

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import l12_capstones.library.Book
import l12_capstones.library.Command
import l12_capstones.library.FINE_PER_DAY_CENTS
import l12_capstones.library.LOAN_DAYS
import l12_capstones.library.LibraryRules
import l12_capstones.library.LibraryState
import l12_capstones.library.LoadResult
import l12_capstones.library.Loan
import l12_capstones.library.LoanResult
import l12_capstones.library.MAX_FINE_CENTS
import l12_capstones.library.MAX_LOANS_PER_MEMBER
import l12_capstones.library.Member
import l12_capstones.library.OverdueLoan
import l12_capstones.library.Step
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/**
 * One finished answer to the library capstone. Try starter/ first!
 *
 * Shape of every rule: look things up, return early with a refusal
 * (`?: return ...`), and only at the end build the new state with
 * copy(). No list is ever modified in place.
 */
object LibraryReference : LibraryRules {
    // prettyPrint: the save file is meant to be readable by humans too.
    private val json = Json { prettyPrint = true }

    override fun addBook(state: LibraryState, book: Book): LibraryState {
        require(book.isbn.isNotBlank()) { "isbn must not be blank" }
        require(book.title.isNotBlank()) { "title must not be blank" }
        require(book.copies >= 1) { "copies must be at least 1, was ${book.copies}" }
        require(book.year > 0) { "year must be positive, was ${book.year}" }
        val existing = state.books.find { it.isbn == book.isbn }
            ?: return state.copy(books = state.books + book)
        // Int.MAX_VALUE + 1 would silently wrap to a negative number (l01 lesson 03).
        require(book.copies <= Int.MAX_VALUE - existing.copies) { "too many copies of ${book.isbn}" }
        val merged = existing.copy(copies = existing.copies + book.copies)
        return state.copy(books = state.books.map { if (it.isbn == book.isbn) merged else it })
    }

    override fun addMember(state: LibraryState, member: Member): LibraryState {
        require(member.id.isNotBlank()) { "member id must not be blank" }
        require(member.name.isNotBlank()) { "member name must not be blank" }
        require(state.members.none { it.id == member.id }) { "member id ${member.id} is already used" }
        return state.copy(members = state.members + member)
    }

    override fun availableCopies(state: LibraryState, isbn: String): Int {
        val book = state.books.find { it.isbn == isbn } ?: return 0
        return book.copies - state.loans.count { it.isbn == isbn }
    }

    override fun search(state: LibraryState, query: String): List<Book> {
        val needle = query.trim()
        return state.books.filter {
            it.title.contains(needle, ignoreCase = true) || it.author.contains(needle, ignoreCase = true)
        }
    }

    override fun borrow(state: LibraryState, isbn: String, memberId: String, today: LocalDate): Step {
        // The order of the checks is part of the contract: the user always
        // hears about the most fundamental problem first.
        fun refuse(result: LoanResult) = Step(state, result)
        if (state.books.none { it.isbn == isbn }) return refuse(LoanResult.NoSuchBook)
        if (state.members.none { it.id == memberId }) return refuse(LoanResult.NoSuchMember)
        val memberLoans = state.loans.filter { it.memberId == memberId }
        if (memberLoans.any { today.isAfter(it.dueOn) }) return refuse(LoanResult.HasOverdueLoans)
        if (memberLoans.size >= MAX_LOANS_PER_MEMBER) return refuse(LoanResult.LimitReached)
        if (availableCopies(state, isbn) <= 0) return refuse(LoanResult.NotAvailable)

        val loan = Loan(isbn, memberId, borrowedOn = today, dueOn = today.plusDays(LOAN_DAYS))
        return Step(state.copy(loans = state.loans + loan), LoanResult.Borrowed(loan))
    }

    override fun returnBook(state: LibraryState, isbn: String, memberId: String, today: LocalDate): Step {
        val loan = findLoan(state, isbn, memberId) ?: return Step(state, LoanResult.NotBorrowed)
        // `- loan` removes the first equal element: exactly this one loan.
        val newState = state.copy(loans = state.loans - loan)
        return Step(newState, LoanResult.Returned(loan, fineFor(loan, today)))
    }

    override fun renew(state: LibraryState, isbn: String, memberId: String, today: LocalDate): Step {
        val loan = findLoan(state, isbn, memberId) ?: return Step(state, LoanResult.NotBorrowed)
        if (loan.renewed || today.isAfter(loan.dueOn)) return Step(state, LoanResult.CannotRenew)
        val renewed = loan.copy(dueOn = loan.dueOn.plusDays(LOAN_DAYS), renewed = true)
        // Replace by POSITION, not by equality: a member may borrow two copies
        // of the same book on the same day, and those two Loans are equal.
        // `map { if (it == loan) ... }` would renew both of them.
        val index = state.loans.indexOf(loan)
        val newState = state.copy(loans = state.loans.mapIndexed { i, it -> if (i == index) renewed else it })
        return Step(newState, LoanResult.Renewed(renewed))
    }

    override fun fineFor(loan: Loan, today: LocalDate): Long {
        val daysLate = ChronoUnit.DAYS.between(loan.dueOn, today).coerceAtLeast(0)
        return (daysLate * FINE_PER_DAY_CENTS).coerceAtMost(MAX_FINE_CENTS)
    }

    override fun overdue(state: LibraryState, today: LocalDate): List<OverdueLoan> =
        state.loans
            .filter { today.isAfter(it.dueOn) }
            .map { OverdueLoan(it, ChronoUnit.DAYS.between(it.dueOn, today), fineFor(it, today)) }
            // sortedByDescending is stable: equal lateness keeps loan order.
            .sortedByDescending { it.daysLate }

    override fun encode(state: LibraryState): String = json.encodeToString(LibraryState.serializer(), state)

    override fun decode(text: String): LoadResult =
        try {
            val state = json.decodeFromString(LibraryState.serializer(), text)
            // Well-formed JSON can still describe an impossible library.
            val problem = findProblem(state)
            if (problem == null) LoadResult.Loaded(state) else LoadResult.Corrupt("invalid library file: $problem")
        } catch (e: SerializationException) {
            // Covers broken JSON and missing/extra fields.
            LoadResult.Corrupt("not a library file: ${e.message?.lineSequence()?.firstOrNull()}")
        } catch (e: IllegalArgumentException) {
            // decodeFromString also reports some bad input this way.
            LoadResult.Corrupt("not a library file: ${e.message?.lineSequence()?.firstOrNull()}")
        } catch (e: DateTimeParseException) {
            LoadResult.Corrupt("bad date in library file: ${e.parsedString}")
        }

    override fun parseCommand(line: String): Command = parseLibraryCommand(line)

    /**
     * The invariants every saved library must keep. Returns the first
     * broken one as a message, or null when the data makes sense.
     */
    private fun findProblem(state: LibraryState): String? {
        val bookIsbns = state.books.map { it.isbn }
        val memberIds = state.members.map { it.id }
        val badBook = state.books.find { it.isbn.isBlank() || it.title.isBlank() || it.copies < 1 || it.year <= 0 }
        return when {
            badBook != null -> "book '${badBook.isbn}' has a blank field, copies < 1 or year <= 0"
            bookIsbns.size != bookIsbns.toSet().size -> "the same isbn appears twice"
            state.members.any { it.id.isBlank() || it.name.isBlank() } -> "a member has a blank id or name"
            memberIds.size != memberIds.toSet().size -> "the same member id appears twice"
            state.loans.any { it.isbn !in bookIsbns } -> "a loan refers to an unknown book"
            state.loans.any { it.memberId !in memberIds } -> "a loan refers to an unknown member"
            state.loans.any { it.dueOn.isBefore(it.borrowedOn) } -> "a loan is due before it was borrowed"
            state.books.any { availableCopies(state, it.isbn) < 0 } -> "more loans than copies of a book"
            else -> null
        }
    }

    private fun findLoan(state: LibraryState, isbn: String, memberId: String): Loan? =
        state.loans.find { it.isbn == isbn && it.memberId == memberId }
}
