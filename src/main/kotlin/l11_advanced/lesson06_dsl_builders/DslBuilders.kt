package l11_advanced.lesson06_dsl_builders

/**
 * =====================================================================
 *  LESSON 11.6 — Type-safe builders (DSLs)
 * =====================================================================
 *  GOAL   : Build your own mini-language with lambdas with receiver:
 *           nested builders, an immutable result, and @DslMarker to
 *           stop inner blocks from calling outer builders by mistake.
 *  RUN    : ./gradlew -q run -Plesson=l11_advanced.lesson06_dsl_builders.DslBuildersKt
 *  OUTPUT :
 *           Menu(Cafe Kotlin)
 *             Drinks: Tea 2.50, Coffee 3.00
 *             Food: Bagel 4.00
 *           items in total: 3
 *           built by plain calls: Menu(Cafe Java) with 1 section(s)
 *  NEXT   : lesson07_annotations_reflection/AnnotationsAndReflection.kt
 * =====================================================================
 */

// ── The RESULT: plain immutable data ────────────────────────────────
// The builder is mutable while you build; what it returns is not.
data class MenuItem(
    val name: String,
    val cents: Long,
)

data class Section(
    val title: String,
    val items: List<MenuItem>,
)

data class Menu(
    val name: String,
    val sections: List<Section>,
)

// ── 1. CONCEPT: @DslMarker ──────────────────────────────────────────
// Mark every builder class with the same marker annotation. Inside an
// inner block, the outer builder's methods are then NOT available
// implicitly (see the pitfall in main).
@DslMarker
annotation class MenuDsl

// ── 2. CONCEPT: a builder per level of nesting ──────────────────────
@MenuDsl
class SectionBuilder(
    private val title: String,
) {
    private val items = mutableListOf<MenuItem>()

    fun item(
        name: String,
        cents: Long,
    ) {
        require(cents >= 0) { "price must be >= 0, was $cents" }
        items += MenuItem(name, cents)
    }

    fun build() = Section(title, items.toList()) // toList(): a copy, not our buffer
}

@MenuDsl
class MenuBuilder(
    private val name: String,
) {
    private val sections = mutableListOf<Section>()

    // `block: SectionBuilder.() -> Unit` = a lambda with receiver
    // (l07_functional/lesson08_receivers): inside it, `this` is the builder.
    fun section(
        title: String,
        block: SectionBuilder.() -> Unit,
    ) {
        sections += SectionBuilder(title).apply(block).build()
    }

    fun build() = Menu(name, sections.toList())
}

// The entry point: a top-level function that starts the DSL.
fun menu(
    name: String,
    block: MenuBuilder.() -> Unit,
): Menu = MenuBuilder(name).apply(block).build()

private const val CENTS_PER_UNIT = 100

fun formatCents(cents: Long): String {
    val fraction = (cents % CENTS_PER_UNIT).toString().padStart(2, '0')
    return "${cents / CENTS_PER_UNIT}.$fraction"
}

fun render(menu: Menu): String =
    buildString {
        appendLine("Menu(${menu.name})")
        menu.sections.forEach { section ->
            val items = section.items.joinToString { "${it.name} ${formatCents(it.cents)}" }
            appendLine("  ${section.title}: $items")
        }
    }.trimEnd()

private const val TEA = 250L
private const val COFFEE = 300L
private const val BAGEL = 400L
private const val ESPRESSO = 200L

fun main() {
    // ── 3. CONCEPT: using the DSL ────────────────────────────────────
    // It reads like a description, but it is ordinary Kotlin: function
    // calls with trailing lambdas.
    val cafe =
        menu("Cafe Kotlin") {
            section("Drinks") {
                item("Tea", TEA)
                item("Coffee", COFFEE)
            }
            section("Food") {
                item("Bagel", BAGEL)
            }
        }
    println(render(cafe))
    println("items in total: ${cafe.sections.sumOf { it.items.size }}")

    // ── 4. PITFALL: scope leakage, stopped by @DslMarker ─────────────
    // Without the marker, this would compile and quietly add a SECTION
    // from inside a section (the outer `this` is still reachable):
    //   menu("x") {
    //       section("Drinks") {
    //           section("Oops") { }
    //       }
    //   }
    // With @DslMarker on both builders it is rejected:
    //   ❌ 'fun section(title: String, block: SectionBuilder.() -> Unit): Unit' cannot be called in
    //      this context with an implicit receiver. Use an explicit receiver if necessary.
    // (If you really mean the outer builder, write this@menu.section(...).)

    // ── 5. WHY: a DSL is just sugar over normal calls ────────────────
    // The same kind of menu, built with no lambdas at all:
    val drinks = SectionBuilder("Drinks")
    drinks.item("Espresso", ESPRESSO)
    val javaMenu = Menu("Cafe Java", listOf(drinks.build()))
    println("built by plain calls: Menu(${javaMenu.name}) with ${javaMenu.sections.size} section(s)")

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Add a `note("...")` function to SectionBuilder and print notes.
    // b) Remove @MenuDsl from SectionBuilder. Does the pitfall compile now?
    // c) Make item() reject duplicate names with require.
}
