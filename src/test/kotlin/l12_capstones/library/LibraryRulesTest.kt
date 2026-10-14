package l12_capstones.library

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Acceptance tests for the library capstone, grouped by milestone
 * (m1 ... m5). Subclasses pick the implementation: the reference or YOUR
 * starter. Run one milestone at a time, e.g. --tests "*LibraryStarterTest.m1*".
 */
@TestMethodOrder(MethodOrderer.MethodName::class)
abstract class LibraryRulesTest(
    private val rules: LibraryRules,
) {
    private val day1: LocalDate = LocalDate.of(2026, 1, 1)
    private val kotlinBook = Book("111", "Kotlin in Action", "Jemerov", 2017, copies = 2)
    private val effective = Book("222", "Effective Java", "Bloch", 2018, copies = 1)
    private val ada = Member("m1", "Ada")
    private val grace = Member("m2", "Grace")

    private fun library(): LibraryState {
        var state = LibraryState()
        state = rules.addBook(state, kotlinBook)
        state = rules.addBook(state, effective)
        state = rules.addMember(state, ada)
        return rules.addMember(state, grace)
    }

    private fun borrowed(state: LibraryState, isbn: String, member: String, on: LocalDate): LibraryState {
        val step = rules.borrow(state, isbn, member, on)
        assertIs<LoanResult.Borrowed>(step.result, "setup: borrowing $isbn should work")
        return step.state
    }

    // ── Milestone 1 ─────────────────────────────────────────────────

    @Test
    fun `m1 addBook appends new books and merges copies of a known isbn`() {
        val state = rules.addBook(library(), kotlinBook.copy(copies = 3, title = "ignored"))
        assertEquals(listOf("111", "222"), state.books.map { it.isbn })
        assertEquals(5, state.books.first().copies)
        assertEquals("Kotlin in Action", state.books.first().title, "Hint: merging keeps the original title")
    }

    @Test
    fun `m1 addBook and addMember reject invalid arguments`() {
        assertFailsWith<IllegalArgumentException> { rules.addBook(LibraryState(), kotlinBook.copy(copies = 0)) }
        assertFailsWith<IllegalArgumentException> { rules.addBook(LibraryState(), kotlinBook.copy(isbn = " ")) }
        assertFailsWith<IllegalArgumentException> { rules.addMember(library(), ada) }
        assertFailsWith<IllegalArgumentException> { rules.addMember(library(), Member("m9", "")) }
        assertFailsWith<IllegalArgumentException> { rules.addBook(LibraryState(), kotlinBook.copy(year = 0)) }
        val huge = rules.addBook(LibraryState(), kotlinBook.copy(copies = Int.MAX_VALUE))
        assertFailsWith<IllegalArgumentException>("Hint: Int.MAX_VALUE + 1 wraps around") {
            rules.addBook(huge, kotlinBook.copy(copies = 1))
        }
    }

    @Test
    fun `m1 availableCopies counts loans and is 0 for unknown books`() {
        val state = borrowed(library(), "111", "m1", day1)
        assertEquals(1, rules.availableCopies(state, "111"))
        assertEquals(1, rules.availableCopies(state, "222"))
        assertEquals(0, rules.availableCopies(state, "999"))
    }

    @Test
    fun `m1 search matches title or author ignoring case`() {
        val state = library()
        assertEquals(listOf("111"), rules.search(state, "kotlin").map { it.isbn })
        assertEquals(listOf("222"), rules.search(state, "BLOCH").map { it.isbn })
        assertEquals(listOf("111", "222"), rules.search(state, "  ").map { it.isbn }, "A blank query lists everything")
        assertEquals(listOf("111"), rules.search(state, "  jemEROV ").map { it.isbn }, "Trimmed, any case, by author")
        assertTrue(rules.search(state, "python").isEmpty())
    }

    // ── Milestone 2 ─────────────────────────────────────────────────

    @Test
    fun `m2 borrow creates a loan due 14 days later`() {
        val step = rules.borrow(library(), "111", "m1", day1)
        val result = assertIs<LoanResult.Borrowed>(step.result)
        assertEquals(Loan("111", "m1", day1, day1.plusDays(LOAN_DAYS)), result.loan)
        assertEquals(listOf(result.loan), step.state.loans)
    }

    @Test
    fun `m2 borrow refusals come back as results in the documented order`() {
        val state = library()
        assertEquals(LoanResult.NoSuchBook, rules.borrow(state, "999", "nobody", day1).result)
        assertEquals(LoanResult.NoSuchMember, rules.borrow(state, "111", "nobody", day1).result)

        val oneCopyGone = borrowed(state, "222", "m2", day1)
        assertEquals(LoanResult.NotAvailable, rules.borrow(oneCopyGone, "222", "m1", day1).result)
        assertEquals(oneCopyGone, rules.borrow(oneCopyGone, "222", "m1", day1).state, "A refusal changes nothing")

        val late = day1.plusDays(LOAN_DAYS + 1)
        assertEquals(
            LoanResult.HasOverdueLoans,
            rules.borrow(oneCopyGone, "111", "m2", late).result,
            "Hint: overdue is checked before the other rules",
        )
    }

    @Test
    fun `m2 borrow stops at the loan limit`() {
        var state = library()
        state = rules.addBook(state, Book("333", "Atomic Kotlin", "Eckel", 2021, copies = 5))
        state = borrowed(state, "111", "m1", day1)
        state = borrowed(state, "222", "m1", day1)
        state = borrowed(state, "333", "m1", day1)
        assertEquals(LoanResult.LimitReached, rules.borrow(state, "333", "m1", day1).result)
    }

    @Test
    fun `m2 borrow checks the limit before availability and overdue before the limit`() {
        var state = library()
        state = rules.addBook(state, Book("333", "Atomic Kotlin", "Eckel", 2021, copies = 5))
        state = borrowed(state, "111", "m1", day1)
        state = borrowed(state, "111", "m1", day1) // both copies of 111 are now gone
        state = borrowed(state, "333", "m1", day1)
        assertEquals(
            LoanResult.LimitReached,
            rules.borrow(state, "111", "m1", day1).result,
            "Hint: at the limit AND no copies left -> LimitReached comes first",
        )
        val late = day1.plusDays(LOAN_DAYS + 1)
        assertEquals(
            LoanResult.HasOverdueLoans,
            rules.borrow(state, "222", "m1", late).result,
            "Hint: late AND at the limit -> HasOverdueLoans comes first",
        )
    }

    @Test
    fun `m2 returnBook removes only that loan and charges nothing when on time`() {
        var state = borrowed(library(), "111", "m1", day1)
        state = borrowed(state, "111", "m2", day1)
        val step = rules.returnBook(state, "111", "m1", day1.plusDays(LOAN_DAYS))
        val result = assertIs<LoanResult.Returned>(step.result)
        assertEquals(0L, result.fineCents)
        assertEquals(listOf("m2"), step.state.loans.map { it.memberId })
        assertEquals(LoanResult.NotBorrowed, rules.returnBook(step.state, "111", "m1", day1).result)
    }

    @Test
    fun `m2 returnBook charges a fine when late`() {
        val state = borrowed(library(), "111", "m1", day1)
        val step = rules.returnBook(state, "111", "m1", day1.plusDays(LOAN_DAYS + 2))
        assertEquals(FINE_PER_DAY_CENTS * 2, assertIs<LoanResult.Returned>(step.result).fineCents)
        assertTrue(step.state.loans.isEmpty())
    }

    @Test
    fun `m2 renew works on the due date and changes only one of two equal loans`() {
        val state = borrowed(library(), "111", "m1", day1)
        val onDueDate = rules.renew(state, "111", "m1", day1.plusDays(LOAN_DAYS))
        assertIs<LoanResult.Renewed>(onDueDate.result, "Hint: the due date itself is not late yet")

        val twoCopies = borrowed(state, "111", "m1", day1) // same book, same member, same day
        val step = rules.renew(twoCopies, "111", "m1", day1)
        assertEquals(
            listOf(true, false),
            step.state.loans.map { it.renewed },
            "Hint: the two loans are equal (==), so replace by position, not by equality",
        )
    }

    @Test
    fun `m2 renew extends from the due date once and never when late`() {
        val state = borrowed(library(), "111", "m1", day1)
        val step = rules.renew(state, "111", "m1", day1.plusDays(1))
        val renewed = assertIs<LoanResult.Renewed>(step.result).loan
        assertEquals(day1.plusDays(LOAN_DAYS * 2), renewed.dueOn, "Hint: add LOAN_DAYS to the CURRENT due date")
        assertTrue(renewed.renewed)
        assertEquals(LoanResult.CannotRenew, rules.renew(step.state, "111", "m1", day1.plusDays(2)).result)

        val late = day1.plusDays(LOAN_DAYS + 1)
        assertEquals(LoanResult.CannotRenew, rules.renew(state, "111", "m1", late).result)
        assertEquals(LoanResult.NotBorrowed, rules.renew(state, "222", "m1", day1).result)
    }

    // ── Milestone 3 ─────────────────────────────────────────────────

    @Test
    fun `m3 fineFor charges per late day and is capped`() {
        val loan = Loan("111", "m1", day1, dueOn = day1.plusDays(LOAN_DAYS))
        assertEquals(0L, rules.fineFor(loan, loan.dueOn))
        assertEquals(0L, rules.fineFor(loan, day1), "Early returns are free")
        assertEquals(FINE_PER_DAY_CENTS * 3, rules.fineFor(loan, loan.dueOn.plusDays(3)))
        assertEquals(MAX_FINE_CENTS, rules.fineFor(loan, loan.dueOn.plusDays(365)))
    }

    @Test
    fun `m3 overdue lists late loans with the most late first`() {
        var state = borrowed(library(), "111", "m1", day1)
        state = borrowed(state, "222", "m2", day1.plusDays(5))
        state = borrowed(state, "111", "m2", day1.plusDays(5))
        val today = day1.plusDays(LOAN_DAYS + 10)

        val report = rules.overdue(state, today)
        assertEquals(listOf(10L, 5L, 5L), report.map { it.daysLate })
        assertEquals(listOf("m1", "m2", "m2"), report.map { it.loan.memberId })
        assertEquals(listOf("222", "111"), report.drop(1).map { it.loan.isbn }, "Ties keep loan order")
        assertTrue(rules.overdue(state, day1).isEmpty())
    }

    // ── Milestone 4 ─────────────────────────────────────────────────

    @Test
    fun `m4 encode then decode gives back the same library`() {
        val state = rules.renew(borrowed(library(), "111", "m1", day1), "111", "m1", day1).state
        val loaded = rules.decode(rules.encode(state))
        assertEquals(LoadResult.Loaded(state), loaded)
    }

    @Test
    fun `m4 decode reports corrupt data instead of throwing`() {
        assertIs<LoadResult.Corrupt>(rules.decode("this is not json"))
        assertIs<LoadResult.Corrupt>(rules.decode("""{"books": 42}"""))
        val badDate =
            """{"loans":[{"isbn":"1","memberId":"m","borrowedOn":"yesterday","dueOn":"2026-01-01"}]}"""
        assertIs<LoadResult.Corrupt>(rules.decode(badDate), "Hint: a bad date is corrupt data too")
    }

    @Test
    fun `m4 decode rejects JSON that describes an impossible library`() {
        val good = borrowed(library(), "111", "m1", day1)
        val impossible =
            mapOf(
                "zero copies" to good.copy(books = good.books.map { it.copy(copies = 0) }),
                "duplicate isbn" to good.copy(books = good.books + good.books.first()),
                "duplicate member" to good.copy(members = good.members + good.members.first()),
                "loan of unknown book" to good.copy(loans = good.loans.map { it.copy(isbn = "999") }),
                "loan of unknown member" to good.copy(loans = good.loans.map { it.copy(memberId = "nobody") }),
                "more loans than copies" to good.copy(loans = good.loans + good.loans + good.loans),
                "due before borrowed" to good.copy(loans = good.loans.map { it.copy(dueOn = day1.minusDays(1)) }),
            )
        impossible.forEach { (what, state) ->
            assertIs<LoadResult.Corrupt>(rules.decode(rules.encode(state)), "Hint: '$what' is not a valid library")
        }
    }

    // ── Milestone 5 ─────────────────────────────────────────────────

    @Test
    fun `m5 parseCommand understands every command`() {
        assertEquals(
            Command.AddBook(Book("111", "Kotlin in Action", "Jemerov", 2017, 2)),
            rules.parseCommand("add-book 111 | Kotlin in Action | Jemerov | 2017 | 2"),
        )
        assertEquals(
            Command.AddMember(Member("m1", "Ada Lovelace")),
            rules.parseCommand("add-member m1 | Ada Lovelace"),
        )
        assertEquals(Command.Borrow("111", "m1"), rules.parseCommand("  BORROW   111   m1 "))
        assertEquals(Command.Borrow("111", "m1"), rules.parseCommand("borrow\t111\tm1"), "Tabs separate words too")
        assertEquals(Command.Return("111", "m1"), rules.parseCommand("return 111 m1"))
        assertEquals(Command.Renew("111", "m1"), rules.parseCommand("renew 111 m1"))
        assertEquals(Command.Search("in action"), rules.parseCommand("search in action"))
        assertEquals(Command.ListBooks, rules.parseCommand("list"))
        assertEquals(Command.Overdue, rules.parseCommand("overdue"))
        assertEquals(Command.Help, rules.parseCommand("help"))
        assertEquals(Command.Quit, rules.parseCommand("quit"))
    }

    @Test
    fun `m5 parseCommand turns bad input into Invalid`() {
        val bad =
            listOf(
                "",
                "dance",
                "borrow 111",
                "add-book 111 | Title | Author | not-a-year | 1",
                "add-book 111 | Title | Author | 2017 | 0",
                "add-book 111 | Title",
                "add-book 111 | Title | Author | -5 | 1",
                "add-member m1",
                "list everything",
                "quit now",
            )
        bad.forEach { line -> assertIs<Command.Invalid>(rules.parseCommand(line), "'$line' should be Invalid") }
    }
}
