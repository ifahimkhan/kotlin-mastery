package l11_advanced.practice

import l11_advanced.practice.html.Element
import l11_advanced.practice.html.Text
import l11_advanced.practice.html.escape
import l11_advanced.practice.html.html
import l11_advanced.practice.html.render
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlDslTest {
    @Test
    fun `escape replaces the markup characters`() {
        assertEquals("a &lt;b&gt; &amp; c", escape("a <b> & c"))
        assertEquals("plain", escape("plain"))
        assertEquals("", escape(""))
        assertEquals("say &quot;hi&quot; &amp; it&#39;s fine", escape("say \"hi\" & it's fine"))
    }

    @Test
    fun `the builder produces an immutable tree in declaration order`() {
        val page =
            html {
                body {
                    h1("Hi")
                    ul {
                        li("one")
                        li("two")
                    }
                }
            }
        val body = page.children.single() as Element
        assertEquals("body", body.tag)
        assertEquals(listOf("h1", "ul"), body.children.map { (it as Element).tag })
        val list = body.children[1] as Element
        assertEquals(listOf(Text("one"), Text("two")), list.children.flatMap { (it as Element).children })
    }

    @Test
    fun `render indents nested tags and keeps text-only tags on one line`() {
        val rendered = render(html { body { p("x < y") } })
        assertEquals("<html>\n  <body>\n    <p>x &lt; y</p>\n  </body>\n</html>", rendered)
    }

    @Test
    fun `user text can never inject a tag`() {
        val rendered = render(html { body { p("<script>alert(1)</script>") } })
        assertTrue("<script>" !in rendered, "Text must be escaped")
    }
}
