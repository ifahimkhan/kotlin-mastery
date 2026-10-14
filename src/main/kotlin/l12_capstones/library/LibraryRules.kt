package l12_capstones.library

import java.time.LocalDate

/** How long a loan lasts, and how far one renewal extends it. */
const val LOAN_DAYS = 14L

/** A member may hold at most this many books at once. */
const val MAX_LOANS_PER_MEMBER = 3

/** Fine for each day a book is late, in cents. */
const val FINE_PER_DAY_CENTS = 25L

/** A single loan's fine never grows past this, in cents. */
const val MAX_FINE_CENTS = 1_000L

/**
 * The contract of the library capstone: what YOU build in
 * starter/LibraryStarter.kt. reference/LibraryReference.kt is one
 * finished answer; the same acceptance tests check both.
 *
 * Every function is pure: it gets the current state (and today's date
 * when time matters) and returns a result. Nothing reads a clock or a
 * file, which is what makes the rules easy to test.
 *
 * Invalid ARGUMENTS (blank isbn, copies < 1, ...) are caller bugs and
 * throw IllegalArgumentException via require. Business refusals (no
 * copies left, limit reached, ...) come back as LoanResult values.
 */
interface LibraryRules {
    // ── Milestone 1: catalogue and members (levels 05, 06) ──────────

    /**
     * Adds [book]. If a book with the same isbn exists, its copies are
     * increased by book.copies instead (title/author are kept).
     * require: isbn and title not blank, copies >= 1, year > 0, and the
     * merged copies must still fit in an Int.
     */
    fun addBook(state: LibraryState, book: Book): LibraryState

    /** Adds [member]. require: id and name not blank, id not already used. */
    fun addMember(state: LibraryState, member: Member): LibraryState

    /** Copies of [isbn] that are not on loan right now (0 for an unknown isbn). */
    fun availableCopies(state: LibraryState, isbn: String): Int

    /**
     * Books whose title OR author contains [query], ignoring case, in
     * catalogue order. The query is trimmed first ("  kotlin " finds
     * "Kotlin"), so a blank query returns every book.
     */
    fun search(state: LibraryState, query: String): List<Book>

    // ── Milestone 2: loans (levels 04, 05, 06) ──────────────────────

    /**
     * Lends one copy of [isbn] to [memberId] on [today], due LOAN_DAYS later.
     * Checks, in this order: NoSuchBook, NoSuchMember, HasOverdueLoans
     * (any of the member's loans is past due on [today]), LimitReached
     * (MAX_LOANS_PER_MEMBER), NotAvailable. Success: Borrowed(loan).
     */
    fun borrow(state: LibraryState, isbn: String, memberId: String, today: LocalDate): Step

    /**
     * Ends the member's loan of [isbn]. Unknown loan -> NotBorrowed.
     * Success: Returned(loan, fineCents) with the fine from [fineFor].
     */
    fun returnBook(state: LibraryState, isbn: String, memberId: String, today: LocalDate): Step

    /**
     * Extends the loan by LOAN_DAYS from its CURRENT due date.
     * Unknown loan -> NotBorrowed; already renewed or already late on
     * [today] -> CannotRenew (renewing ON the due date is still allowed).
     * Success: Renewed(updatedLoan).
     * If the member holds two copies of the same book, only the FIRST of
     * those loans changes (two loans from the same day are equal values,
     * so replace by position, not by ==).
     */
    fun renew(state: LibraryState, isbn: String, memberId: String, today: LocalDate): Step

    // ── Milestone 3: overdue report and fines (levels 01, 06) ───────

    /**
     * FINE_PER_DAY_CENTS for each day after dueOn, capped at
     * MAX_FINE_CENTS. Returning on the due date costs 0.
     */
    fun fineFor(loan: Loan, today: LocalDate): Long

    /** Loans past due on [today], most days late first (ties: in loan order). */
    fun overdue(state: LibraryState, today: LocalDate): List<OverdueLoan>

    // ── Milestone 4: saving and loading (levels 07, 11) ─────────────

    /** The state as JSON text (kotlinx.serialization). */
    fun encode(state: LibraryState): String

    /**
     * Parses [text] back. Anything that is not valid saved data ->
     * Corrupt(message). "Valid" means more than well-formed JSON: every
     * book has isbn/title, copies >= 1 and year > 0; isbns and member ids
     * are unique and not blank; every loan points to a known book and
     * member and is not due before it was borrowed; no book has more
     * loans than copies.
     */
    fun decode(text: String): LoadResult

    // ── Milestone 5: the command line (levels 02, 05) ───────────────

    /**
     * Parses one line typed by the user. Never throws: bad input ->
     * Command.Invalid. Words are separated by any whitespace (spaces or
     * tabs). Commands without arguments (list, overdue, help, quit)
     * reject extra words; year must be a positive number.
     */
    fun parseCommand(line: String): Command
}
