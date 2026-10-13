package l01_basics.lesson01_hello

/**
 * =====================================================================
 *  LESSON 01.1 — Hello, World!
 * =====================================================================
 *  GOAL   : Write and run your first Kotlin program. Meet `main`,
 *           `println`, `print` and comments.
 *  RUN    : Click the green ▶ next to `fun main` in IntelliJ, or
 *           ./gradlew -q run -Plesson=l01_basics.lesson01_hello.HelloWorldKt
 *  OUTPUT :
 *           Hello, World!
 *           Hello, Kotlin learner!
 *           print() stays on the same line... see? Now a new line.
 *           Program arguments received: 0
 *  NEXT   : lesson02_variables/Variables.kt
 * =====================================================================
 */
fun main(args: Array<String>) {
    // ── 1. CONCEPT ───────────────────────────────────────────────────
    // `main` is the entry point: the JVM starts your program here.
    // `println` writes text to the console and then moves to a new line.
    println("Hello, World!")
    println("Hello, Kotlin learner!")

    // `print` writes text but does NOT move to a new line.
    print("print() stays on the same line... ")
    print("see? ")
    println("Now a new line.")

    // `args` holds words typed after the program name on the command line.
    // From the IDE there are none, so the size is 0.
    println("Program arguments received: ${args.size}")

    // ── 2. WHY ───────────────────────────────────────────────────────
    // Kotlin needs no class around `main` (unlike Java) and no semicolons.
    // Java equivalent, for comparison:
    //   public class Hello {
    //       public static void main(String[] args) {
    //           System.out.println("Hello, World!");
    //       }
    //   }
    // If you don't need the arguments, `fun main()` is also valid.

    // ── 3. COMMENTS ──────────────────────────────────────────────────
    // A single-line comment starts with //.
    /* A block comment can span
       several lines. */
    /** A KDoc comment (like the one above `main`) documents code. */

    // ── 4. PITFALL ───────────────────────────────────────────────────
    // Text must be inside double quotes. Single quotes are for ONE char:
    //   println('Hello')   // ❌ Too many characters in a character literal.
    //   println('H')       // ✅ prints H

    // ── 5. TRY IT ────────────────────────────────────────────────────
    // a) Print your own name on a new line.
    // b) Use two print() calls and one println() to build one sentence.
    // c) In IntelliJ: Run ▸ Edit Configurations ▸ Program arguments,
    //    type "one two", run again and watch the argument count change.
}
