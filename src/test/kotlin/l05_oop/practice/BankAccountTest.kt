package l05_oop.practice

import l05_oop.practice.bank.BankAccount
import l05_oop.practice.bank.SavingsAccount
import l05_oop.practice.bank.Transaction
import l05_oop.practice.bank.TransactionResult
import l05_oop.practice.bank.TransactionType
import l05_oop.practice.bank.formatCents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BankAccountTest {
    @Test
    fun `deposit and withdraw update the balance`() {
        val account = BankAccount("Ada")
        assertEquals(TransactionResult.Success(10_000), account.deposit(10_000))
        assertEquals(TransactionResult.Success(6_950), account.withdraw(3_050))
        assertEquals(6_950, account.balance)
    }

    @Test
    fun `insufficient funds is a result that changes nothing`() {
        val account = BankAccount("Ada")
        account.deposit(100)
        assertEquals(TransactionResult.InsufficientFunds(available = 100, requested = 500), account.withdraw(500))
        assertEquals(100, account.balance)
        assertEquals(1, account.history.size, "Refused operations are not recorded")
    }

    @Test
    fun `non-positive amounts are caller bugs and throw`() {
        val account = BankAccount("Ada")
        account.deposit(100)
        assertFailsWith<IllegalArgumentException> { account.deposit(-5) }
        assertFailsWith<IllegalArgumentException> { account.withdraw(0) }
        assertEquals(100, account.balance)
        assertEquals(1, account.history.size)
    }

    @Test
    fun `history is a copy that callers cannot change`() {
        val account = BankAccount("Ada")
        account.deposit(1_000)
        val leaked = account.history
        // Even a cast back to MutableList can only touch the COPY (or throw
        // UnsupportedOperationException for a truly read-only list).
        runCatching { (leaked as? MutableList<*>)?.clear() }
        assertEquals(1, account.history.size)
    }

    @Test
    fun `history records every successful transaction in order`() {
        val account = BankAccount("Ada")
        account.deposit(1_000)
        account.withdraw(250)
        assertEquals(
            listOf(
                Transaction(TransactionType.DEPOSIT, 1_000, 1_000),
                Transaction(TransactionType.WITHDRAWAL, 250, 750),
            ),
            account.history,
        )
    }

    @Test
    fun `savings interest is rounded down to whole cents`() {
        val savings = SavingsAccount("Ada", yearlyRatePercent = 5)
        savings.deposit(6_950)
        assertEquals(347, savings.addYearlyInterest(), "5% of 69.50 is 3.475 -> 3.47")
        assertEquals(7_297, savings.balance)
        assertEquals(TransactionType.INTEREST, savings.history.last().type)
    }

    @Test
    fun `zero interest is not recorded and bad rates are rejected`() {
        val empty = SavingsAccount("Bob", yearlyRatePercent = 3)
        assertEquals(0, empty.addYearlyInterest())
        assertEquals(0, empty.history.size)
        assertFailsWith<IllegalArgumentException> { SavingsAccount("Eve", yearlyRatePercent = 150) }
    }

    @Test
    fun `cents are formatted with two decimals`() {
        assertEquals("100.00", formatCents(10_000))
        assertEquals("0.05", formatCents(5))
        assertEquals("-5.00", formatCents(-500))
    }
}
