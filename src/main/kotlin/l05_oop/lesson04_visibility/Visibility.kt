package l05_oop.lesson04_visibility

/**
 * =====================================================================
 *  LESSON 05.4 — Visibility and encapsulation
 * =====================================================================
 *  GOAL   : Hide the inside of a class so that nobody can break its
 *           rules. Know public, private, protected and internal.
 *  RUN    : ./gradlew -q run -Plesson=l05_oop.lesson04_visibility.VisibilityKt
 *  OUTPUT :
 *           Deposited 100 -> balance 100
 *           Withdraw 30 -> true, balance 70
 *           Withdraw 500 -> false, balance 70
 *           PIN 1234 correct? true
 *           PIN 9999 correct? false
 *           Family wallet operations recorded: 3
 *           Module-wide helper says: internal works here
 *  NEXT   : lesson05_inheritance/Inheritance.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: a class guards its own rules ─────────────────────
    // Rule: the balance only changes through deposit/withdraw, never
    // directly. `private set` makes that rule impossible to break.
    val wallet = Wallet(pin = 1234)
    wallet.deposit(100)
    println("Deposited 100 -> balance ${wallet.balance}")
    println("Withdraw 30 -> ${wallet.withdraw(30)}, balance ${wallet.balance}")
    println("Withdraw 500 -> ${wallet.withdraw(500)}, balance ${wallet.balance}")

    // The PIN is private: outsiders may ASK whether a PIN is right,
    // but can never READ it.
    println("PIN 1234 correct? ${wallet.checkPin(1234)}")
    println("PIN 9999 correct? ${wallet.checkPin(9999)}")

    // ── 2. CONCEPT: protected = me + my subclasses ───────────────────
    // (`open` and `:` for subclasses are explained in the next lesson.)
    val family = FamilyWallet(pin = 1111)
    family.deposit(50)
    family.withdraw(20)
    family.withdraw(10)
    println("Family wallet operations recorded: ${family.operationCount()}")

    // ── 3. CONCEPT: internal = anywhere in this module ───────────────
    // A "module" is one compiled unit, e.g. this Gradle project's main
    // source set (its tests may see internal declarations too).
    println("Module-wide helper says: ${moduleHelper()}")

    // ── 4. THE FOUR MODIFIERS ────────────────────────────────────────
    //   public    (default) visible everywhere
    //   private   inside the class (or inside the file, for top-level code)
    //   protected inside the class and its subclasses
    //   internal  inside the same module

    // ── 5. WHY hide things? ──────────────────────────────────────────
    // If `balance` were public and writable, ANY line of code anywhere
    // could set it to a million. Private state + public methods = one
    // place where the rules live. That's ENCAPSULATION.
    // Rule of thumb: make everything private, open up only what callers need.
    //
    // Two kinds of "no" (used across this level):
    //   invalid argument (caller bug)  -> require(...) throws
    //   normal refusal (no money left) -> return false / a result type

    // ── 6. PITFALL: these lines don't compile ────────────────────────
    //   wallet.balance = 1_000_000
    //   // ❌ Cannot access 'balance': it is private in 'l05_oop.lesson04_visibility.Wallet'.
    //   wallet.pin
    //   // ❌ Cannot access 'val pin: Int': it is private in 'l05_oop.lesson04_visibility.Wallet'.
    //   family.operations
    //   // ❌ Cannot access 'var operations: Int': it is protected in 'l05_oop.lesson04_visibility.Wallet'.

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Remove `private set` from balance and "steal" money in main.
    // b) Make deposit reject amounts above 10_000.
    // c) Change `operations` from protected to private. What breaks?
}

internal fun moduleHelper(): String = "internal works here"

open class Wallet(private val pin: Int) {
    var balance: Int = 0
        private set

    protected var operations: Int = 0

    fun deposit(amount: Int) {
        // require(condition) { message } throws IllegalArgumentException when
        // the condition is false. Use it for caller bugs (a bad argument).
        require(amount > 0) { "Deposit must be positive, was $amount" }
        balance += amount
        operations += 1
    }

    // Bad argument -> require (exception). Not enough money is a normal
    // refusal, so it returns false instead of throwing.
    fun withdraw(amount: Int): Boolean {
        require(amount > 0) { "Withdrawal must be positive, was $amount" }
        if (amount > balance) return false
        balance -= amount
        operations += 1
        return true
    }

    fun checkPin(attempt: Int): Boolean = attempt == pin
}

class FamilyWallet(pin: Int) : Wallet(pin) {
    // A subclass may read protected members of its parent.
    fun operationCount(): Int = operations
}
