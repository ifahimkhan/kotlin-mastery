package l09_coroutines.practice.kitchen

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger

/**
 * =====================================================================
 *  PRACTICE 09.B — A restaurant kitchen with channels
 * =====================================================================
 *  GOAL   : Model a kitchen as a pipeline of coroutines:
 *           orders -> chefs (fan-out) -> pass -> waiter -> served log.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.practice.kitchen.KitchenKt
 *  OUTPUT :
 *           served order 2 (SOUP) by chef-2
 *           served order 3 (SOUP) by chef-2
 *           served order 1 (STEAK) by chef-1
 *           served order 4 (PASTA) by chef-2
 *           chefs cooking at once (max): 2
 *           all orders served once: true
 *  DESIGN : Every stage is a coroutine and every hand-off is a Channel,
 *           so no state is shared between chefs. The order channel is
 *           closed by produce { }, the pass is closed when ALL chefs are
 *           done, so the waiter's loop ends and runKitchen returns: a
 *           clean shutdown with no hanging coroutines. Time is only ever
 *           spent in delay(), so tests run on virtual time.
 *  EXTEND : Give each chef a speciality; add a second waiter; make an
 *           order fail (burnt dish) and report it with a result type.
 * =====================================================================
 */

private const val SOUP_MS = 100L
private const val PASTA_MS = 200L
private const val STEAK_MS = 300L
private const val DEMO_CHEFS = 2

enum class Dish(val cookMs: Long) {
    SOUP(SOUP_MS),
    PASTA(PASTA_MS),
    STEAK(STEAK_MS),
}

data class Order(val id: Int, val dish: Dish)

data class Meal(val orderId: Int, val dish: Dish, val chef: String, val readyAtMs: Long)

data class KitchenReport(
    val served: List<Meal>,          // in the order the waiter served them
    val maxChefsCookingAtOnce: Int,
)

/**
 * Cooks every order with [chefCount] chefs working in parallel and
 * returns what the waiter served. [now] reports the current time in ms
 * (real clock in main, virtual clock in tests).
 */
@OptIn(ExperimentalCoroutinesApi::class) // produce is still marked experimental
suspend fun runKitchen(orders: List<Order>, chefCount: Int, now: () -> Long): KitchenReport {
    require(chefCount > 0) { "chefCount must be positive, was $chefCount" }
    val startedAt = now()
    // Chefs may run on several threads (that depends on the caller's
    // dispatcher), so the shared counters are atomic (lesson13_shared_state).
    val cookingNow = AtomicInteger(0)
    val maxCooking = AtomicInteger(0)
    val served = mutableListOf<Meal>() // touched only by the waiter coroutine

    coroutineScope {
        val tickets = produce { orders.forEach { send(it) } }
        val pass = Channel<Meal>() // the counter between chefs and waiter

        val chefs = List(chefCount) { index ->
            val name = "chef-${index + 1}"
            launch {
                for (order in tickets) { // each order goes to exactly one chef
                    val cooking = cookingNow.incrementAndGet()
                    maxCooking.accumulateAndGet(cooking, ::maxOf)
                    delay(order.dish.cookMs)
                    cookingNow.decrementAndGet()
                    pass.send(Meal(order.id, order.dish, name, now() - startedAt))
                }
            }
        }
        launch {
            chefs.joinAll()
            pass.close() // no chef left: tell the waiter the kitchen is closed
        }
        for (meal in pass) served.add(meal) // the waiter
    }
    return KitchenReport(served.toList(), maxCooking.get())
}

fun main() = runBlocking {
    val orders = listOf(
        Order(1, Dish.STEAK),
        Order(2, Dish.SOUP),
        Order(3, Dish.SOUP),
        Order(4, Dish.PASTA),
    )
    val report = runKitchen(orders, DEMO_CHEFS, now = System::currentTimeMillis)

    // The real clock jitters, so main prints no times; the tests check
    // exact times on the virtual clock (KitchenTest).
    report.served.forEach { meal -> println("served order ${meal.orderId} (${meal.dish}) by ${meal.chef}") }
    println("chefs cooking at once (max): ${report.maxChefsCookingAtOnce}")
    println("all orders served once: ${report.served.map { it.orderId }.sorted() == orders.map { it.id }}")
}
