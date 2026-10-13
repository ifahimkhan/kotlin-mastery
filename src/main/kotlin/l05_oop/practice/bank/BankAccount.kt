package l05_oop.practice.bank

import kotlin.math.abs

/**
 * =====================================================================
 *  PRACTICE 05.A — Bank account system
 * =====================================================================
 *  GOAL   : Combine encapsulation, inheritance, data classes, enums and
 *           sealed results into a small, safe banking model.
 *  RUN    : ./gradlew -q run -Plesson=l05_oop.practice.bank.BankAccountKt
 *  OUTPUT :
 *           Ada opened a savings account at 5% per year
 *           Deposit 100.00 -> OK, balance 100.00
 *           Withdraw 30.50 -> OK, balance 69.50
 *           Withdraw 500.00 -> refused: only 69.50 available
 *           Interest 5% -> +3.47, balance 72.97
 *           History:
 *             DEPOSIT    100.00 -> 100.00
 *             WITHDRAWAL  30.50 ->  69.50
 *             INTEREST     3.47 ->  72.97
 *  DESIGN : Money is stored as Long CENTS, never Double (0.1 + 0.2 bug,
 *           level 01). Two kinds of "no":
 *           - Invalid argument (a caller bug, e.g. depositing -5.00)
 *             -> require(...) throws IllegalArgumentException.
 *           - Legitimate business refusal (not enough money)
 *             -> a sealed [TransactionResult]; `when` forces the caller
 *             to handle every case.
 *           The history is exposed as a read-only List copy, so nobody
 *           can rewrite it.
 *           (List/mutableListOf and padStart: level 06 / level 01.)
 *  EXTEND : Add a transfer(to: BankAccount, cents) that is all-or-nothing.
 * =====================================================================
 */

private const val CENTS_PER_UNIT = 100
private const val PERCENT = 100
private const val CENT_DIGITS = 2
private const val TYPE_COLUMN = 10
private const val AMOUNT_COLUMN = 6

enum class TransactionType { DEPOSIT, WITHDRAWAL, INTEREST }

data class Transaction(val type: TransactionType, val cents: Long, val balanceAfter: Long)

sealed interface TransactionResult {
    data class Success(val balanceAfter: Long) : TransactionResult

    data class InsufficientFunds(val available: Long, val requested: Long) : TransactionResult
}

/** Formats cents as "12.34" (or "-5.00"). */
fun formatCents(cents: Long): String {
    val sign = if (cents < 0) "-" else ""
    val absolute = abs(cents)
    val fraction = (absolute % CENTS_PER_UNIT).toString().padStart(CENT_DIGITS, '0')
    return "$sign${absolute / CENTS_PER_UNIT}.$fraction"
}

open class BankAccount(val owner: String) {
    var balance: Long = 0
        private set

    private val transactions = mutableListOf<Transaction>()

    /** A copy: callers can read the history but never change it. */
    val history: List<Transaction>
        get() = transactions.toList()

    fun deposit(cents: Long): TransactionResult {
        require(cents > 0) { "Deposit must be positive, was $cents" } // caller bug
        return record(TransactionType.DEPOSIT, cents)
    }

    fun withdraw(cents: Long): TransactionResult {
        require(cents > 0) { "Withdrawal must be positive, was $cents" } // caller bug
        // Not enough money is a normal outcome, not a bug: return a result.
        if (cents > balance) return TransactionResult.InsufficientFunds(available = balance, requested = cents)
        return record(TransactionType.WITHDRAWAL, cents)
    }

    // protected: subclasses (SavingsAccount) may add their own kinds of
    // transactions, but outsiders can't fake one.
    protected fun record(type: TransactionType, cents: Long): TransactionResult {
        balance += if (type == TransactionType.WITHDRAWAL) -cents else cents
        transactions.add(Transaction(type, cents, balance))
        return TransactionResult.Success(balance)
    }
}

class SavingsAccount(owner: String, val yearlyRatePercent: Int) : BankAccount(owner) {
    init {
        require(yearlyRatePercent in 0..PERCENT) { "Rate must be 0..100%, was $yearlyRatePercent" }
    }

    /** Adds one year of interest, rounded DOWN to whole cents. Returns the interest. */
    fun addYearlyInterest(): Long {
        val interest = balance * yearlyRatePercent / PERCENT
        if (interest > 0) record(TransactionType.INTEREST, interest)
        return interest
    }
}

/** Turns any result into one line of text. Exhaustive: no else branch. */
fun describe(action: String, result: TransactionResult): String =
    when (result) {
        is TransactionResult.Success -> "$action -> OK, balance ${formatCents(result.balanceAfter)}"
        is TransactionResult.InsufficientFunds ->
            "$action -> refused: only ${formatCents(result.available)} available"
    }

fun formatHistoryLine(transaction: Transaction): String {
    val type = transaction.type.name.padEnd(TYPE_COLUMN)
    val amount = formatCents(transaction.cents).padStart(AMOUNT_COLUMN)
    val after = formatCents(transaction.balanceAfter).padStart(AMOUNT_COLUMN)
    return "$type $amount -> $after"
}

fun main() {
    val account = SavingsAccount("Ada", yearlyRatePercent = 5)
    println("${account.owner} opened a savings account at ${account.yearlyRatePercent}% per year")

    println(describe("Deposit 100.00", account.deposit(10_000)))
    println(describe("Withdraw 30.50", account.withdraw(3_050)))
    println(describe("Withdraw 500.00", account.withdraw(50_000)))
    // account.deposit(-500) would throw IllegalArgumentException: a negative
    // deposit is a bug in the calling code, not a normal outcome.

    val interest = account.addYearlyInterest()
    println(
        "Interest ${account.yearlyRatePercent}% -> +${formatCents(interest)}, " +
            "balance ${formatCents(account.balance)}",
    )

    println("History:")
    for (transaction in account.history) {
        println("  ${formatHistoryLine(transaction)}")
    }
}
