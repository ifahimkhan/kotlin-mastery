package l05_oop.lesson10_nested_value_java

/**
 * =====================================================================
 *  LESSON 05.10 — Nested and inner classes, value classes, Kotlin vs Java
 * =====================================================================
 *  GOAL   : Put helper classes inside other classes (nested vs inner),
 *           use value classes to stop mixing up IDs, and see how much
 *           code a Kotlin data class saves compared to Java.
 *  RUN    : ./gradlew -q run -Plesson=l05_oop.lesson10_nested_value_java.NestedValueAndJavaKt
 *  OUTPUT :
 *           Nested: V8 engine (knows nothing about any car)
 *           Inner : wheel #1 of Model S (reads the outer car's name)
 *           UserId(value=42) -> loading user #42
 *           OrderId(value=42) -> loading order #42
 *           Kotlin: Book(title=Dune, pages=412)
 *           Java  : JavaBook{title='Dune', pages=412}
 *           Equal content? kotlin=true, java=true
 *           Java getter used as a property: book.title = Dune
 *  NEXT   : practice/bank/BankAccount.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: nested vs inner ──────────────────────────────────
    // NESTED (the default): just a class that lives inside another for
    //   organization. It has NO link to an outer object.
    // INNER: holds a reference to the outer object and can use its members.
    val engine = Car.Engine("V8") // no Car needed
    println("Nested: ${engine.describe()}")

    val car = Car("Model S")
    val wheel = car.Wheel(1) // needs a Car: created FROM one
    println("Inner : ${wheel.describe()}")

    // ── 2. CONCEPT: value classes ────────────────────────────────────
    // UserId and OrderId both wrap an Int, but the compiler treats them
    // as different types. At runtime they are usually just the Int: no
    // extra object, no extra cost. Exceptions: as a nullable `UserId?`,
    // inside a collection like List<UserId>, or used as an interface type,
    // the value is BOXED into a real UserId object.
    val userId = UserId(42)
    val orderId = OrderId(42)
    println("$userId -> ${loadUser(userId)}")
    println("$orderId -> ${loadOrder(orderId)}")
    //   loadUser(orderId)
    //   // ❌ Argument type mismatch: actual type is 'OrderId', but 'UserId' was expected.
    // With plain Ints, loadUser(orderId) would compile and load the WRONG thing.

    // ── 3. CONCEPT: Kotlin data class vs Java POJO ───────────────────
    // JavaBook.java (src/main/java/...) is ~45 lines. Book is 1 line.
    val kotlinBook = Book("Dune", 412)
    val javaBook = JavaBook("Dune", 412)
    println("Kotlin: $kotlinBook")
    println("Java  : $javaBook")
    println(
        "Equal content? kotlin=${kotlinBook == Book("Dune", 412)}, " +
            "java=${javaBook == JavaBook("Dune", 412)}",
    )
    // Kotlin lets you call Java's getTitle() as if it were a property.
    println("Java getter used as a property: book.title = ${javaBook.title}")

    // ── 4. WHY ───────────────────────────────────────────────────────
    // Nested by default: an inner class silently keeps its outer object
    // alive (memory leaks in Android are a classic). Kotlin makes you ask
    // for that link explicitly with `inner`.

    // ── 5. PITFALL: a nested class can't see the outer object ────────
    //   class Car(val name: String) {
    //       class Engine { fun car() = name }
    //   }
    //   // ❌ Outer class 'class Car : Any' of non-inner class cannot be used as receiver.

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Remove `inner` from Wheel and read the compiler error.
    // b) Create `@JvmInline value class Email(val text: String)` that
    //    rejects text without '@' in an init block.
    // c) Count the lines of JavaBook.java. Write the same as a Java record.
}

class Car(val name: String) {
    class Engine(val type: String) {
        fun describe(): String = "$type engine (knows nothing about any car)"
    }

    inner class Wheel(val position: Int) {
        fun describe(): String = "wheel #$position of $name (reads the outer car's name)"
    }
}

@JvmInline
value class UserId(val value: Int)

@JvmInline
value class OrderId(val value: Int)

fun loadUser(id: UserId): String = "loading user #${id.value}"

fun loadOrder(id: OrderId): String = "loading order #${id.value}"

data class Book(val title: String, val pages: Int)
