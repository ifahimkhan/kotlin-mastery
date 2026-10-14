package l12_capstones.library.starter

import l12_capstones.library.Book
import l12_capstones.library.Command
import l12_capstones.library.LibraryRules
import l12_capstones.library.LibraryState
import l12_capstones.library.LoadResult
import l12_capstones.library.Loan
import l12_capstones.library.Member
import l12_capstones.library.OverdueLoan
import l12_capstones.library.Step
import java.time.LocalDate

/**
 * =====================================================================
 *  CAPSTONE 12.A — Library manager: YOUR implementation
 * =====================================================================
 *  HOW    : Read ../README.md, then replace every TODO() below, one
 *           milestone at a time. The KDoc of each function is in
 *           ../LibraryRules.kt.
 *  CHECK  : ./gradlew exercises --tests "l12_capstones.library*"
 *  STUCK? : ../reference/ has one finished answer. Try first!
 *  TIP    : You may add private helper functions and files freely.
 *           Keep every function pure: no clocks, no files, no println.
 * =====================================================================
 */
object LibraryStarter : LibraryRules {
    // Milestone 1
    override fun addBook(state: LibraryState, book: Book): LibraryState = TODO("M1: add or merge a book")

    override fun addMember(state: LibraryState, member: Member): LibraryState = TODO("M1: add a member")

    override fun availableCopies(state: LibraryState, isbn: String): Int = TODO("M1: copies not on loan")

    override fun search(state: LibraryState, query: String): List<Book> = TODO("M1: search title/author")

    // Milestone 2
    override fun borrow(state: LibraryState, isbn: String, memberId: String, today: LocalDate): Step =
        TODO("M2: borrow with the checks in order")

    override fun returnBook(state: LibraryState, isbn: String, memberId: String, today: LocalDate): Step =
        TODO("M2: return and compute the fine")

    override fun renew(state: LibraryState, isbn: String, memberId: String, today: LocalDate): Step =
        TODO("M2: renew once, never when late")

    // Milestone 3
    override fun fineFor(loan: Loan, today: LocalDate): Long = TODO("M3: fine in cents, capped")

    override fun overdue(state: LibraryState, today: LocalDate): List<OverdueLoan> =
        TODO("M3: overdue report, most late first")

    // Milestone 4
    override fun encode(state: LibraryState): String = TODO("M4: state -> JSON")

    override fun decode(text: String): LoadResult = TODO("M4: JSON -> state, Corrupt on bad input")

    // Milestone 5
    override fun parseCommand(line: String): Command = TODO("M5: parse one CLI line")
}
