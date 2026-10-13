package l06_collections.practice

import l06_collections.practice.inventory.Item
import l06_collections.practice.inventory.SAMPLE_INVENTORY
import l06_collections.practice.inventory.formatCents
import l06_collections.practice.inventory.lowStock
import l06_collections.practice.inventory.restockSuggestions
import l06_collections.practice.inventory.sell
import l06_collections.practice.inventory.totalValue
import l06_collections.practice.inventory.valueByCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InventoryReportTest {
    @Test
    fun `low stock and restock suggestions`() {
        assertEquals(listOf("EL-1", "EL-2", "HO-1"), lowStock(SAMPLE_INVENTORY).map { it.sku })
        assertEquals(mapOf("EL-1" to 8, "EL-2" to 10, "HO-1" to 6), restockSuggestions(SAMPLE_INVENTORY))
    }

    @Test
    fun `restock is keyed by sku so same-named items are not lost`() {
        val twins = listOf(Item("A-1", "Cable", "c", 1, 100), Item("A-2", "Cable", "c", 3, 100))
        assertEquals(mapOf("A-1" to 9, "A-2" to 7), restockSuggestions(twins))
    }

    @Test
    fun `values are summed in cents`() {
        assertEquals(
            mapOf("electronics" to 251_200L, "office" to 7_600L, "home" to 13_980L),
            valueByCategory(SAMPLE_INVENTORY),
        )
        assertEquals(272_780L, totalValue(SAMPLE_INVENTORY))
        assertEquals(0L, totalValue(emptyList()))
    }

    @Test
    fun `cents are formatted with thousands separators`() {
        assertEquals("$0.05", formatCents(5))
        assertEquals("$76.00", formatCents(7_600))
        assertEquals("$1,234,567.89", formatCents(123_456_789))
        assertEquals("$0.00", formatCents(0))
        assertEquals("-$0.05", formatCents(-5))
        assertEquals("-$1,234.50", formatCents(-123_450))
    }

    @Test
    fun `selling returns a new list and never goes below zero`() {
        val items = listOf(Item("A", "a", "c", 2, 100), Item("B", "b", "c", 5, 100))
        val after = sell(items, "A", 3)
        assertEquals(0, after.first { it.sku == "A" }.qty)
        assertEquals(2, items.first { it.sku == "A" }.qty, "The original list must not change")
        assertEquals(items, sell(items, "unknown", 1))
        assertFailsWith<IllegalArgumentException> { sell(items, "A", 0) }
    }
}
