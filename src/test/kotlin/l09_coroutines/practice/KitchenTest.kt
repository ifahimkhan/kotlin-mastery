package l09_coroutines.practice

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import l09_coroutines.practice.kitchen.Dish
import l09_coroutines.practice.kitchen.Order
import l09_coroutines.practice.kitchen.runKitchen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

// currentTime, advanceTimeBy, runCurrent and UnconfinedTestDispatcher are
// still marked experimental in kotlinx-coroutines-test, so we opt in.
@OptIn(ExperimentalCoroutinesApi::class)
class KitchenTest {
    private fun soups(count: Int) = List(count) { Order(it + 1, Dish.SOUP) }

    @Test
    fun `every order is served exactly once`() = runTest {
        val orders = listOf(Order(1, Dish.STEAK), Order(2, Dish.SOUP), Order(3, Dish.PASTA), Order(4, Dish.SOUP))
        val report = runKitchen(orders, chefCount = 2) { testScheduler.currentTime }
        assertEquals(listOf(1, 2, 3, 4), report.served.map { it.orderId }.sorted())
    }

    @Test
    fun `chefs work in parallel, never more than chefCount`() = runTest {
        val report = runKitchen(soups(6), chefCount = 3) { testScheduler.currentTime }
        assertEquals(3, report.maxChefsCookingAtOnce)
        // 6 soups, 3 chefs: two rounds of SOUP cooking time (virtual time).
        assertEquals(2 * Dish.SOUP.cookMs, testScheduler.currentTime)
    }

    @Test
    fun `one chef cooks one order at a time`() = runTest {
        val report = runKitchen(soups(3), chefCount = 1) { testScheduler.currentTime }
        assertEquals(1, report.maxChefsCookingAtOnce)
        assertEquals(listOf(100L, 200L, 300L), report.served.map { it.readyAtMs })
        assertTrue(report.served.all { it.chef == "chef-1" })
    }

    @Test
    fun `no orders means a clean, instant shutdown`() = runTest {
        val report = runKitchen(emptyList(), chefCount = 2) { testScheduler.currentTime }
        assertEquals(0, report.served.size)
        assertEquals(0L, testScheduler.currentTime)
    }

    @Test
    fun `zero chefs is rejected`() = runTest {
        assertFailsWith<IllegalArgumentException> { runKitchen(soups(1), chefCount = 0) { 0L } }
    }
}
