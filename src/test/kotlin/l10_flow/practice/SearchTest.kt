package l10_flow.practice

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import l10_flow.practice.search.DEBOUNCE_MS
import l10_flow.practice.search.FakeSearchRepository
import l10_flow.practice.search.SearchState
import l10_flow.practice.search.SearchViewModel
import l10_flow.practice.search.searchStates
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class) // testScheduler.currentTime
class SearchTest {
    private val catalog = listOf("Kotlin", "Kotlin Flow", "Java")

    @Test
    fun `debounce skips queries typed quickly`() = runTest {
        val repository = FakeSearchRepository(catalog, latencyMs = 100)
        val typing = flow {
            emit("ko")
            delay(DEBOUNCE_MS / 3)
            emit("kot") // replaces "ko" before the quiet time is over
            delay(DEBOUNCE_MS * 2)
        }
        searchStates(typing, repository).test {
            assertEquals(SearchState.Loading, awaitItem())
            assertEquals(SearchState.Results("kot", listOf("Kotlin", "Kotlin Flow")), awaitItem())
            awaitComplete()
        }
        assertEquals(listOf("kot"), repository.calls, "\"ko\" must never be searched")
    }

    @Test
    fun `a newer query cancels the slow search`() = runTest {
        val repository = FakeSearchRepository(catalog, latencyMs = 1_000)
        val typing = flow {
            emit("kot")
            delay(DEBOUNCE_MS + 100) // "kot" is searched, but the search is slow...
            emit("java")             // ...and this arrives before it finishes
        }
        searchStates(typing, repository).test {
            assertEquals(SearchState.Loading, awaitItem()) // for "kot"
            assertEquals(SearchState.Loading, awaitItem()) // for "java": "kot" was cancelled
            assertEquals(SearchState.Results("java", listOf("Java")), awaitItem())
            awaitComplete()
        }
        assertEquals(listOf("kot", "java"), repository.calls)
    }

    @Test
    fun `short and repeated queries`() = runTest {
        val repository = FakeSearchRepository(catalog, latencyMs = 0)
        val typing = flow {
            emit("k") // too short
            delay(DEBOUNCE_MS * 2)
            emit("java")
            delay(DEBOUNCE_MS * 2)
            emit(" java ") // trimmed: same query again
            delay(DEBOUNCE_MS * 2)
        }
        searchStates(typing, repository).test {
            assertEquals(SearchState.Idle, awaitItem())
            assertEquals(SearchState.Loading, awaitItem())
            assertEquals(SearchState.Results("java", listOf("Java")), awaitItem())
            awaitComplete() // no second search for " java "
        }
        assertEquals(listOf("java"), repository.calls)
    }

    @Test
    fun `service failure becomes an Error state`() = runTest {
        val repository = FakeSearchRepository(catalog, latencyMs = 0)
        searchStates(flowOf("error"), repository).test {
            assertEquals(SearchState.Loading, awaitItem())
            assertEquals(SearchState.Error("error", "search service unavailable"), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `view model starts Idle and settles after the debounce`() = runTest {
        val repository = FakeSearchRepository(catalog, latencyMs = 100)
        val viewModel = SearchViewModel(backgroundScope, repository)
        viewModel.state.test {
            assertEquals(SearchState.Idle, awaitItem())
            viewModel.onQueryChanged("flow")
            assertEquals(SearchState.Loading, awaitItem())
            assertEquals(SearchState.Results("flow", listOf("Kotlin Flow")), awaitItem())
            assertEquals(DEBOUNCE_MS + 100, testScheduler.currentTime)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `negative debounce is rejected`() {
        val repository = FakeSearchRepository(catalog, latencyMs = 0)
        assertFailsWith<IllegalArgumentException> { searchStates(flowOf("x"), repository, debounceMs = -1) }
    }
}
