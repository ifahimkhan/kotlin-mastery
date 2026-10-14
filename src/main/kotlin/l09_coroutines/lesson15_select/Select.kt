package l09_coroutines.lesson15_select

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select

/**
 * =====================================================================
 *  LESSON 09.15 — select: wait for whichever is first
 * =====================================================================
 *  GOAL   : Wait on several channels or deferred results at once and
 *           react to the first one that is ready.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.lesson15_select.SelectKt
 *  OUTPUT :
 *           [news]    news #1
 *           [news]    news #2
 *           [weather] weather #1
 *           [news]    news #3
 *           [news]    news #4
 *           first answer: file from mirror-us
 *           losing request cancelled: true
 *  NEXT   : lesson16_testing/TestingCoroutines.kt
 * =====================================================================
 */

private const val NEWS_EVERY_MS = 300L
private const val WEATHER_EVERY_MS = 750L // every arrival is at least 150 ms from the next
private const val MESSAGES_TO_READ = 5
private const val FAST_MIRROR_MS = 80L
private const val SLOW_MIRROR_MS = 200L

@OptIn(ExperimentalCoroutinesApi::class)
fun CoroutineScope.feed(name: String, everyMs: Long): ReceiveChannel<String> = produce {
    var n = 1
    while (true) { // runs until the consumer cancels it
        delay(everyMs)
        send("$name #${n++}")
    }
}

/** Pretend download from a mirror that answers after [latencyMs]. */
fun CoroutineScope.downloadFrom(mirror: String, latencyMs: Long): Deferred<String> = async {
    delay(latencyMs)
    "file from $mirror"
}

fun main() = runBlocking {
    // ── 1. CONCEPT: select over channels ─────────────────────────────
    // select { } suspends until ONE of its clauses can run, runs it, and
    // returns its value. Here: whichever feed has a message first.
    coroutineScope {
        val news = feed("news", NEWS_EVERY_MS)       // at 300, 600, 900 ...
        val weather = feed("weather", WEATHER_EVERY_MS) // at 750, 1500 ...
        repeat(MESSAGES_TO_READ) {
            val message = select {
                news.onReceive { "[news]    $it" }
                weather.onReceive { "[weather] $it" }
            }
            println(message)
        }
        news.cancel() // stop the endless producers, or coroutineScope waits forever
        weather.cancel()
    }

    // ── 2. CONCEPT: select over deferred results ─────────────────────
    // Ask two mirrors for the same file, keep the first answer, cancel
    // the other request.
    coroutineScope {
        val slow = downloadFrom("mirror-eu", SLOW_MIRROR_MS)
        val fast = downloadFrom("mirror-us", FAST_MIRROR_MS)
        val winner = select {
            slow.onAwait { it }
            fast.onAwait { it }
        }
        println("first answer: $winner")
        slow.cancel()
        fast.cancel()
        println("losing request cancelled: ${slow.isCancelled}")
    }

    // ── 3. PITFALL: select is biased ─────────────────────────────────
    // If SEVERAL clauses are ready at the same moment, select picks the
    // FIRST one in the list. A busy first channel can starve the others.
    // selectUnbiased { } picks randomly among the ready clauses.

    // ── 4. WHY select? ───────────────────────────────────────────────
    // Without it you would need a coroutine per source plus extra
    // channels just to find out which one answered first.

    // ── 5. TRY IT ────────────────────────────────────────────────────
    // a) See the bias for real: make two Channel<String>(Channel.UNLIMITED),
    //    send three messages into EACH before selecting, then run select
    //    six times. Both channels are always ready, so which one wins?
    //    Swap the two onReceive clauses, then try selectUnbiased { }.
    // b) Add a third clause `onTimeout(250) { "timeout" }` (needs
    //    @OptIn(ExperimentalCoroutinesApi::class)) to section 2 and make
    //    both mirrors slower than 250 ms.
}
