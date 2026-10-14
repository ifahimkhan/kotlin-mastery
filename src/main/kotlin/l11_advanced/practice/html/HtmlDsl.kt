package l11_advanced.practice.html

/**
 * =====================================================================
 *  PRACTICE 11.A — An HTML builder DSL
 * =====================================================================
 *  GOAL   : Build a small, safe HTML DSL: nested tags with lambdas with
 *           receiver, @DslMarker against scope leakage, text escaping,
 *           and an immutable tree rendered as indented HTML.
 *  RUN    : ./gradlew -q run -Plesson=l11_advanced.practice.html.HtmlDslKt
 *  OUTPUT :
 *           <html>
 *             <head>
 *               <title>Kotlin &amp; DSLs</title>
 *             </head>
 *             <body>
 *               <h1>Shopping list</h1>
 *               <p>Prices &lt; 5 EUR only</p>
 *               <ul>
 *                 <li>tea</li>
 *                 <li>milk</li>
 *               </ul>
 *             </body>
 *           </html>
 *  DESIGN : Builders are mutable while building; html { } returns an
 *           immutable Element tree. Rendering is a pure function of the
 *           tree. All text goes through escape(), so user text can never
 *           inject tags. escape() also handles both quote characters, so it
 *           is safe for attribute values too. Tag names are NOT checked:
 *           they come from this file, never from users.
 *  EXTEND : Add attributes, e.g. a(href = "...") { +"link" }, and pass
 *           every attribute value through escape().
 * =====================================================================
 */

// ── The result: an immutable tree ───────────────────────────────────
sealed interface Node

data class Text(
    val value: String,
) : Node

data class Element(
    val tag: String,
    val children: List<Node>,
) : Node

private const val INDENT = "  "

/**
 * Escapes the characters that would otherwise be read as HTML markup.
 * Safe for text content AND for quoted attribute values ("..." or '...').
 */
fun escape(text: String): String =
    buildString {
        for (char in text) {
            when (char) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&#39;")
                else -> append(char)
            }
        }
    }

/** Renders [node] as indented HTML. Elements with only text stay on one line. */
fun render(
    node: Node,
    depth: Int = 0,
): String {
    val pad = INDENT.repeat(depth)
    return when (node) {
        is Text -> pad + escape(node.value)
        is Element ->
            if (node.children.all { it is Text }) {
                val text = node.children.joinToString("") { escape((it as Text).value) }
                "$pad<${node.tag}>$text</${node.tag}>"
            } else {
                val inner = node.children.joinToString("\n") { render(it, depth + 1) }
                "$pad<${node.tag}>\n$inner\n$pad</${node.tag}>"
            }
    }
}

// ── The builders ────────────────────────────────────────────────────
@DslMarker
annotation class HtmlDsl

@HtmlDsl
open class TagBuilder(
    private val tag: String,
) {
    private val children = mutableListOf<Node>()

    /** `+"text"` adds a text node: unaryPlus (lesson05_operator_overloading). */
    operator fun String.unaryPlus() {
        children += Text(this)
    }

    protected fun <B : TagBuilder> child(
        builder: B,
        block: B.() -> Unit,
    ) {
        children += builder.apply(block).build()
    }

    // NOTE: build() is public, so `body { build() }` also compiles inside the
    // DSL. Hiding it completely needs extra machinery (a private member plus
    // a factory); real DSL libraries such as kotlinx.html take care of this.
    fun build(): Element = Element(tag, children.toList())
}

class HeadBuilder : TagBuilder("head") {
    fun title(text: String) = child(TagBuilder("title")) { +text }
}

class ListBuilder : TagBuilder("ul") {
    fun li(text: String) = child(TagBuilder("li")) { +text }
}

class BodyBuilder : TagBuilder("body") {
    fun h1(text: String) = child(TagBuilder("h1")) { +text }

    fun p(text: String) = child(TagBuilder("p")) { +text }

    fun ul(block: ListBuilder.() -> Unit) = child(ListBuilder(), block)
}

class HtmlBuilder : TagBuilder("html") {
    fun head(block: HeadBuilder.() -> Unit) = child(HeadBuilder(), block)

    fun body(block: BodyBuilder.() -> Unit) = child(BodyBuilder(), block)
}

/** Entry point of the DSL. */
fun html(block: HtmlBuilder.() -> Unit): Element = HtmlBuilder().apply(block).build()

fun main() {
    val page =
        html {
            head { title("Kotlin & DSLs") }
            body {
                h1("Shopping list")
                p("Prices < 5 EUR only")
                ul {
                    li("tea")
                    li("milk")
                }
            }
        }
    println(render(page))
}
