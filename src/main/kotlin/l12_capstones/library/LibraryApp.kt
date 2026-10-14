package l12_capstones.library

import java.io.IOException
import java.io.UncheckedIOException
import java.time.LocalDate

/** Where "today" comes from. Main uses the real date; tests pin one. */
fun interface Today {
    fun date(): LocalDate
}

/**
 * The application layer: turns a Command into new state + lines to
 * print. It owns the current state (the ONE mutable thing in the
 * project, kept private) and saves every change.
 *
 * Save FIRST, then commit: [onChange] runs before [state] is updated. If
 * saving fails (disk full, read-only folder...), the user is told
 * "could not save: ..." and the in-memory state stays as it was, so
 * memory and disk never disagree.
 *
 * Because it returns lines instead of printing them, the whole CLI can
 * be tested without a console.
 */
class LibraryApp(
    private val rules: LibraryRules,
    private val today: Today,
    initial: LibraryState = LibraryState(),
    private val onChange: (LibraryState) -> Unit = {},
) {
    var state: LibraryState = initial
        private set

    /** Handles one command and returns what to show the user. */
    fun handle(command: Command): List<String> =
        when (command) {
            is Command.AddBook -> change(rules.addBook(state, command.book), "added ${command.book.title}")
            is Command.AddMember -> addMember(command.member)
            is Command.Borrow -> loanStep(rules.borrow(state, command.isbn, command.memberId, today.date()))
            is Command.Return -> loanStep(rules.returnBook(state, command.isbn, command.memberId, today.date()))
            is Command.Renew -> loanStep(rules.renew(state, command.isbn, command.memberId, today.date()))
            is Command.Search -> rules.search(state, command.query).map { describe(it) }.ifEmpty { listOf("no match") }
            Command.ListBooks -> state.books.map { describe(it) }.ifEmpty { listOf("the library is empty") }
            Command.Overdue -> overdueLines()
            Command.Help -> COMMAND_HELP
            Command.Quit -> listOf("bye")
            is Command.Invalid -> listOf("? ${command.reason}")
        }

    // addMember uses require (a duplicate id is a caller bug), so the
    // caller - this app - checks first and turns it into a friendly message.
    private fun addMember(member: Member): List<String> {
        if (state.members.any { it.id == member.id }) return listOf("member id ${member.id} is already used")
        return change(rules.addMember(state, member), "welcome ${member.name}")
    }

    private fun change(newState: LibraryState, message: String): List<String> =
        commit(newState) ?: listOf(message)

    private fun loanStep(step: Step): List<String> {
        if (step.state != state) {
            commit(step.state)?.let { return it }
        }
        return listOf(describe(step.result))
    }

    /** Saves, then adopts [newState]. Returns an error message if saving failed, else null. */
    private fun commit(newState: LibraryState): List<String>? {
        try {
            onChange(newState)
        } catch (e: IOException) {
            return listOf("could not save: ${e.message ?: e::class.simpleName}")
        } catch (e: UncheckedIOException) {
            return listOf("could not save: ${e.message ?: e::class.simpleName}")
        }
        state = newState
        return null
    }

    private fun overdueLines(): List<String> =
        rules.overdue(state, today.date())
            .map {
                "${it.loan.isbn} by ${it.loan.memberId}: ${it.daysLate} days late, fine ${formatCents(it.fineCents)}"
            }
            .ifEmpty { listOf("nothing is overdue") }

    private fun describe(book: Book): String {
        val available = rules.availableCopies(state, book.isbn)
        return "${book.isbn}  ${book.title} by ${book.author} (${book.year})  $available/${book.copies} available"
    }

    private fun describe(result: LoanResult): String =
        when (result) {
            is LoanResult.Borrowed -> "borrowed ${result.loan.isbn}, due ${result.loan.dueOn}"
            is LoanResult.Returned ->
                if (result.fineCents == 0L) "returned ${result.loan.isbn}, thank you"
                else "returned ${result.loan.isbn}, fine ${formatCents(result.fineCents)}"
            is LoanResult.Renewed -> "renewed ${result.loan.isbn}, now due ${result.loan.dueOn}"
            LoanResult.NoSuchBook -> "no book with that isbn"
            LoanResult.NoSuchMember -> "no member with that id"
            LoanResult.NotAvailable -> "all copies are on loan"
            LoanResult.LimitReached -> "member already has $MAX_LOANS_PER_MEMBER books"
            LoanResult.HasOverdueLoans -> "member must return overdue books first"
            LoanResult.NotBorrowed -> "that member did not borrow that book"
            LoanResult.CannotRenew -> "cannot renew: already renewed or late"
        }
}

private const val CENTS_PER_UNIT = 100

/** 1234 -> "$12.34", -5 -> "-$0.05". Money is always Long cents; formatting happens only here. */
fun formatCents(cents: Long): String {
    val sign = if (cents < 0) "-" else ""
    val abs = if (cents < 0) -cents else cents
    return sign + "$" + (abs / CENTS_PER_UNIT) + "." + (abs % CENTS_PER_UNIT).toString().padStart(2, '0')
}
