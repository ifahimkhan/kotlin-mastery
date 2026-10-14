package l11_advanced.practice

import l11_advanced.practice.config.config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConfigDslTest {
    @Test
    fun `values set in the DSL end up in the result`() {
        val app =
            config("shop") {
                server {
                    host = "example.org"
                    port = 443
                }
                database {
                    url = "jdbc:h2:mem:x"
                    poolSize = 3
                }
            }
        assertEquals("shop", app.name)
        assertEquals("example.org", app.server.host)
        assertEquals(443, app.server.port)
        assertEquals(3, app.database.poolSize)
    }

    @Test
    fun `server defaults are used when the server section is left out`() {
        val app = config("tiny") { database { url = "jdbc:h2:mem:t" } }
        assertEquals("localhost", app.server.host)
        assertEquals(8080, app.server.port)
        assertEquals(5, app.database.poolSize)
    }

    @Test
    fun `invalid settings fail when the config is built`() {
        assertFailsWith<IllegalArgumentException> {
            config("x") {
                server { port = 0 }
                database { url = "jdbc:h2:mem:x" }
            }
        }
        val missingUrl = assertFailsWith<IllegalArgumentException> { config("x") { } }
        assertEquals("database.url is required", missingUrl.message)
        assertFailsWith<IllegalArgumentException> { config(" ") { database { url = "jdbc:x" } } }
        assertFailsWith<IllegalArgumentException> {
            config("x") {
                database {
                    url = "jdbc:x"
                    poolSize = 0
                }
            }
        }
    }
}
