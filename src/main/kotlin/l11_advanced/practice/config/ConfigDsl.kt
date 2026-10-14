package l11_advanced.practice.config

/**
 * =====================================================================
 *  PRACTICE 11.B — A type-safe configuration DSL
 * =====================================================================
 *  GOAL   : Describe app settings in Kotlin instead of a text file:
 *           defaults, nested sections, validation when the config is
 *           built, and an immutable result.
 *  RUN    : ./gradlew -q run -Plesson=l11_advanced.practice.config.ConfigDslKt
 *  OUTPUT :
 *           app: shop
 *           server: ServerConfig(host=0.0.0.0, port=9090)
 *           database: DatabaseConfig(url=jdbc:h2:mem:shop, poolSize=10)
 *           defaults: ServerConfig(host=localhost, port=8080), poolSize=5
 *           invalid: IllegalArgumentException: port must be in 1..65535, was 70000
 *  DESIGN : Builders hold `var`s with defaults; build() validates with
 *           require (a bad config is a programming error, so it fails
 *           fast) and returns data classes with only `val`s. A typo in a
 *           setting name is a compile error, not a silent ignore.
 *           The server section has defaults; the database url is
 *           required. Calling server { } twice keeps only the LAST block.
 *           require fits here because this config is written in code by
 *           the programmer. Config read from a file or the environment is
 *           external input: return a result or a list of errors instead.
 *  EXTEND : Add a `features { enable("search") }` section that builds a
 *           Set<String>, and reject unknown feature names.
 * =====================================================================
 */

private const val DEFAULT_PORT = 8080
private const val MIN_PORT = 1
private const val MAX_PORT = 65535
private const val DEFAULT_POOL = 5
private const val MAX_POOL = 100

data class ServerConfig(
    val host: String,
    val port: Int,
)

data class DatabaseConfig(
    val url: String,
    val poolSize: Int,
)

data class AppConfig(
    val name: String,
    val server: ServerConfig,
    val database: DatabaseConfig,
)

@DslMarker
annotation class ConfigDsl

@ConfigDsl
class ServerBuilder {
    var host: String = "localhost"
    var port: Int = DEFAULT_PORT

    fun build(): ServerConfig {
        require(host.isNotBlank()) { "host must not be blank" }
        require(port in MIN_PORT..MAX_PORT) { "port must be in $MIN_PORT..$MAX_PORT, was $port" }
        return ServerConfig(host, port)
    }
}

@ConfigDsl
class DatabaseBuilder {
    var url: String = ""
    var poolSize: Int = DEFAULT_POOL

    fun build(): DatabaseConfig {
        require(url.isNotEmpty()) { "database.url is required" }
        require(url.startsWith("jdbc:")) { "url must start with jdbc:, was '$url'" }
        require(poolSize in 1..MAX_POOL) { "poolSize must be in 1..$MAX_POOL, was $poolSize" }
        return DatabaseConfig(url, poolSize)
    }
}

@ConfigDsl
class AppBuilder(
    private val name: String,
) {
    private var server = ServerBuilder()
    private var database = DatabaseBuilder()

    /** Replaces any earlier server { } block: the last one wins. */
    fun server(block: ServerBuilder.() -> Unit) {
        server = ServerBuilder().apply(block)
    }

    fun database(block: DatabaseBuilder.() -> Unit) {
        database = DatabaseBuilder().apply(block)
    }

    fun build(): AppConfig {
        require(name.isNotBlank()) { "app name must not be blank" }
        return AppConfig(name, server.build(), database.build())
    }
}

/** Builds and validates an [AppConfig]. Throws IllegalArgumentException for bad values. */
fun config(
    name: String,
    block: AppBuilder.() -> Unit,
): AppConfig = AppBuilder(name).apply(block).build()

private const val SHOP_PORT = 9090
private const val SHOP_POOL = 10
private const val BAD_PORT = 70_000

fun main() {
    val shop =
        config("shop") {
            server {
                host = "0.0.0.0"
                port = SHOP_PORT
            }
            database {
                url = "jdbc:h2:mem:shop"
                poolSize = SHOP_POOL
            }
        }
    println("app: ${shop.name}")
    println("server: ${shop.server}")
    println("database: ${shop.database}")

    val minimal = config("tiny") { database { url = "jdbc:h2:mem:tiny" } }
    println("defaults: ${minimal.server}, poolSize=${minimal.database.poolSize}")

    try {
        config("broken") {
            server { port = BAD_PORT }
            database { url = "jdbc:h2:mem:x" }
        }
    } catch (e: IllegalArgumentException) {
        println("invalid: ${e::class.simpleName}: ${e.message}")
    }
}
