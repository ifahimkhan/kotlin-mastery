package l06_collections.practice.inventory

import kotlin.math.abs

/**
 * =====================================================================
 *  PRACTICE 06.C — Inventory report
 * =====================================================================
 *  GOAL   : Build a small shop report: low stock, stock value per
 *           category, restock suggestions, and immutable updates.
 *  RUN    : ./gradlew -q run -Plesson=l06_collections.practice.inventory.InventoryReportKt
 *  OUTPUT :
 *           Low stock (< 5): Laptop (2), USB cable (0), Desk lamp (4)
 *           Value per category:
 *             electronics   $2,512.00
 *             office           $76.00
 *             home            $139.80
 *           Total value: $2,727.80
 *           Restock: EL-1 +8, EL-2 +10, HO-1 +6
 *           After selling 3 notebooks: Notebook qty 17 (original list still 20)
 *  DESIGN : Money is stored in whole cents (Long), never in a Double:
 *           Doubles can't store 0.10 exactly (level 01).
 *           Updates return NEW lists; the original inventory never changes.
 *  NEW    : `data class` + `copy()` (level 05) create a changed copy of
 *           one item without touching the original.
 *  EXTEND : Add a "most valuable item per category" section with
 *           groupBy + maxBy.
 * =====================================================================
 */

data class Item(
    val sku: String,
    val name: String,
    val category: String,
    val qty: Int,
    val priceCents: Long,
)

const val LOW_STOCK_LIMIT = 5
const val TARGET_STOCK = 10
private const val CENTS_PER_DOLLAR = 100
private const val CENT_DIGITS = 2
private const val THOUSANDS_GROUP = 3
private const val VALUE_COLUMN_WIDTH = 10
private const val CATEGORY_COLUMN_WIDTH = 12

fun lowStock(items: List<Item>): List<Item> = items.filter { it.qty < LOW_STOCK_LIMIT }

/** Total stock value (qty * price) per category, in first-seen order. */
fun valueByCategory(items: List<Item>): Map<String, Long> =
    items
        .groupBy { it.category }
        .mapValues { (_, group) -> group.sumOf { it.qty * it.priceCents } }

fun totalValue(items: List<Item>): Long = items.sumOf { it.qty * it.priceCents }

/**
 * SKU -> units to order so each low-stock item reaches [TARGET_STOCK].
 * Keyed by the unique SKU, not the name: two products may share a name,
 * and associate silently overwrites duplicate keys (lesson05_transformations).
 */
fun restockSuggestions(items: List<Item>): Map<String, Int> =
    lowStock(items).associate { it.sku to TARGET_STOCK - it.qty }

/**
 * Returns a NEW list where the item with [sku] has [amount] fewer units.
 * Unknown SKUs leave the list unchanged. Stock never goes below zero.
 */
fun sell(items: List<Item>, sku: String, amount: Int): List<Item> {
    require(amount > 0) { "amount must be positive, was $amount" }
    return items.map { item ->
        if (item.sku == sku) item.copy(qty = maxOf(0, item.qty - amount)) else item
    }
}

/** 251200 -> "$2,512.00", -5 -> "-$0.05" */
fun formatCents(cents: Long): String {
    // Format the absolute value, then put the sign in front: % and / on a
    // negative number would otherwise give "-0" dollars and "-5" cents.
    val sign = if (cents < 0) "-" else ""
    val absolute = abs(cents)
    val dollars = absolute / CENTS_PER_DOLLAR
    val rest = (absolute % CENTS_PER_DOLLAR).toString().padStart(CENT_DIGITS, '0')
    val grouped =
        dollars
            .toString()
            .reversed()
            .chunked(THOUSANDS_GROUP)
            .joinToString(",")
            .reversed()
    return "$sign\$$grouped.$rest"
}

val SAMPLE_INVENTORY =
    listOf(
        Item("EL-1", "Laptop", "electronics", 2, 89_900),
        Item("EL-2", "USB cable", "electronics", 0, 799),
        Item("EL-3", "Headset", "electronics", 6, 11_900),
        Item("OF-1", "Notebook", "office", 20, 380),
        Item("HO-1", "Desk lamp", "home", 4, 3_495),
    )

fun main() {
    val inventory = SAMPLE_INVENTORY

    println("Low stock (< $LOW_STOCK_LIMIT): " + lowStock(inventory).joinToString { "${it.name} (${it.qty})" })

    println("Value per category:")
    for ((category, cents) in valueByCategory(inventory)) {
        println("  ${category.padEnd(CATEGORY_COLUMN_WIDTH)} ${formatCents(cents).padStart(VALUE_COLUMN_WIDTH)}")
    }
    println("Total value: ${formatCents(totalValue(inventory))}")

    println("Restock: " + restockSuggestions(inventory).entries.joinToString { "${it.key} +${it.value}" })

    val afterSale = sell(inventory, "OF-1", 3)
    val notebookNow = afterSale.first { it.sku == "OF-1" }.qty
    val notebookBefore = inventory.first { it.sku == "OF-1" }.qty
    println("After selling 3 notebooks: Notebook qty $notebookNow (original list still $notebookBefore)")
}
