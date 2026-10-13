package l01_basics.lesson04_strings

/**
 * =====================================================================
 *  LESSON 01.4 — Strings and string templates
 * =====================================================================
 *  GOAL   : Build text with templates, write multi-line raw strings,
 *           and use the most common String functions.
 *  RUN    : ./gradlew -q run -Plesson=l01_basics.lesson04_strings.StringsKt
 *  OUTPUT :
 *           Hi, I'm Linus and I have 3 cats.
 *           Next year I'll have 4 cats. My name has 5 letters.
 *           Price: $25
 *           Line one
 *           Line two
 *           "Quotes" and C:\paths need no escaping here
 *           Tab:[	] Quote:["] Backslash:[\]
 *           length=13 upper=KOTLIN ROCKS! first=K last=!
 *           substring(0, 6)=Kotlin contains("Rocks")=true
 *           split -> [red, green, blue]
 *           replace -> Kotlin is fun!
 *           padStart -> [00042]
 *           trim -> [spaces]
 *           original is still: Kotlin Rocks!
 *           Built: 1-2-3-4-5
 *  NEXT   : lesson05_operators/Operators.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: string templates ─────────────────────────────────
    // $name inserts a variable. ${...} inserts any expression.
    val name = "Linus"
    val cats = 3
    println("Hi, I'm $name and I have $cats cats.")
    println("Next year I'll have ${cats + 1} cats. My name has ${name.length} letters.")

    // A literal dollar sign: write \$ (or ${'$'}).
    println("Price: \$25")

    // ── 2. CONCEPT: raw strings (triple quotes) ──────────────────────
    // Keep line breaks, need no escaping. trimIndent() removes the
    // common leading whitespace so you can indent the code nicely.
    val poem =
        """
        Line one
        Line two
        """.trimIndent()
    println(poem)
    println("""
        "Quotes" and C:\paths need no escaping here
    """.trim())

    // Escape sequences in normal strings: \t tab, \n new line, \" quote, \\ backslash
    println("Tab:[\t] Quote:[\"] Backslash:[\\]")

    // ── 3. CONCEPT: common String functions ──────────────────────────
    val text = "Kotlin Rocks!"
    println("length=${text.length} upper=${text.uppercase()} first=${text.first()} last=${text.last()}")
    // substring(start, end): start is included, end is EXCLUDED.
    println("substring(0, 6)=${text.substring(0, 6)} contains(\"Rocks\")=${text.contains("Rocks")}")
    println("split -> ${"red,green,blue".split(",")}")
    println("replace -> ${text.replace("Rocks", "is fun")}")
    println("padStart -> [${"42".padStart(5, '0')}]")
    println("trim -> [${"   spaces   ".trim()}]")

    // ── 4. WHY: Strings are immutable ────────────────────────────────
    // Every function above returns a NEW string. The original never changes.
    text.uppercase() // result thrown away!
    println("original is still: $text")

    // To build a string piece by piece, use buildString (fast, no copies).
    val numbers =
        buildString {
            for (i in 1..5) { // loops are explained in level 02
                append(i)
                if (i < 5) append("-")
            }
        }
    println("Built: $numbers")

    // ── 5. PITFALL: index out of bounds ──────────────────────────────
    // Indexes start at 0 and end at length - 1.
    //   "abc"[3]               // ❌ StringIndexOutOfBoundsException
    //   val s = ""
    //   s.substring(0, s.length - 1) // ❌ works for "abc", crashes for ""!
    //   s.first()                    // ❌ NoSuchElementException for ""
    // Always think: "What if the string is empty?"

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Print "Hello, <your name>! Your name reversed is <...>" (hint: reversed()).
    // b) Count how many times 'o' appears in "foo boo zoo" (hint: count { it == 'o' }).
    // c) Print a 3-line address using a raw string with trimIndent().
}
