package l10_flow.practice.search

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import java.io.IOException

/**
 * =====================================================================
 *  PRACTICE 10.B — Search as you type
 * =====================================================================
 *  GOAL   : Build the classic search box: wait until the user stops
 *           typing, skip repeated queries, cancel a slow search when a
 *           newer query arrives, and expose the result as a StateFlow.
 *  RUN    : ./gradlew -q run -Plesson=l10_flow.practice.search.SearchAsYouTypeKt
 *  OUTPUT :
 *           typed "k", "ko", "kot" quickly -> Results(query=kot, items=[Kotlin, Kotlin Flow])
 *           typed "zz" -> Results(query=zz, items=[])
 *           typed "error" -> Error(query=error, message=search service unavailable)
 *           typed "k" -> Idle
 *           searches run: 3 (kot, zz, error; none for "k" or "ko")
 *  DESIGN : searchStates() is a pure pipeline from a Flow of queries to a
 *           Flow of states. Every step is one operator:
 *             trim -> debounce -> distinctUntilChanged -> flatMapLatest
 *           SearchViewModel only connects it to a MutableStateFlow input
 *           and stateIn. The repository is an interface, so tests inject
 *           a fake and run everything on virtual time.
 *  EXTEND : Show "Loading" only if the search takes longer than 100 ms.
 * =====================================================================
 */

const val DEBOUNCE_MS = 300L
const val MIN_QUERY_LENGTH = 2
private const val DEMO_LATENCY_MS = 100L
private const val FAILING_QUERY = "error"

sealed interface SearchState {
    data object Idle : SearchState
    data object Loading : SearchState
    data class Results(val query: String, val items: List<String>) : SearchState
    data class Error(val query: String, val message: String) : SearchState
}

interface SearchRepository {
    /** Throws IOException when the service is down. */
    suspend fun search(query: String): List<String>
}

/** A fake search service with a fixed catalog and a fixed latency. */
class FakeSearchRepository(
    private val catalog: List<String>,
    private val latencyMs: Long,
) : SearchRepository {
    private val _calls = mutableListOf<String>()
    val calls: List<String> get() = _calls.toList()

    override suspend fun search(query: String): List<String> {
        _calls += query
        delay(latencyMs)
        if (query == FAILING_QUERY) throw IOException("search service unavailable")
        return catalog.filter { it.contains(query, ignoreCase = true) }
    }
}

/**
 * The whole feature as one pipeline. Queries shorter than
 * [MIN_QUERY_LENGTH] reset the screen to Idle instead of searching.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class) // debounce, flatMapLatest
fun searchStates(
    queries: Flow<String>,
    repository: SearchRepository,
    debounceMs: Long = DEBOUNCE_MS,
): Flow<SearchState> {
    require(debounceMs >= 0) { "debounceMs must not be negative, was $debounceMs" }
    return queries
        .map { it.trim() }
        .debounce(debounceMs)          // wait until typing pauses
        .distinctUntilChanged()        // "kot" -> "kot " -> "kot" is one search
        .flatMapLatest { query ->      // a new query CANCELS the running search
            if (query.length < MIN_QUERY_LENGTH) flowOf(SearchState.Idle) else searchOnce(query, repository)
        }
}

private fun searchOnce(query: String, repository: SearchRepository): Flow<SearchState> = flow {
    emit(SearchState.Loading)
    val state = try {
        SearchState.Results(query, repository.search(query))
    } catch (e: IOException) { // only I/O failures; cancellation passes through
        SearchState.Error(query, e.message ?: "unknown error")
    }
    emit(state)
}

/** Connects the pipeline to a text box: call onQueryChanged on every key press. */
class SearchViewModel(scope: CoroutineScope, repository: SearchRepository, debounceMs: Long = DEBOUNCE_MS) {
    private val queries = MutableStateFlow("")

    val state: StateFlow<SearchState> =
        searchStates(queries, repository, debounceMs).stateIn(scope, SharingStarted.Eagerly, SearchState.Idle)

    fun onQueryChanged(text: String) {
        queries.value = text
    }
}

private val DEMO_CATALOG = listOf("Kotlin", "Kotlin Flow", "Java", "Coroutines")

fun main() =
    runBlocking {
        val screenScope = CoroutineScope(coroutineContext + Job())
        val repository = FakeSearchRepository(DEMO_CATALOG, DEMO_LATENCY_MS)
        val viewModel = SearchViewModel(screenScope, repository)

        // Three key presses with no pause: only the last one is searched.
        viewModel.onQueryChanged("k")
        viewModel.onQueryChanged("ko")
        viewModel.onQueryChanged("kot")
        val kot = viewModel.state.first { it is SearchState.Results && it.query == "kot" }
        println("typed \"k\", \"ko\", \"kot\" quickly -> $kot")

        viewModel.onQueryChanged("zz")
        println("typed \"zz\" -> ${viewModel.state.first { it is SearchState.Results && it.query == "zz" }}")

        viewModel.onQueryChanged(FAILING_QUERY)
        println("typed \"$FAILING_QUERY\" -> ${viewModel.state.first { it is SearchState.Error }}")

        viewModel.onQueryChanged("k")
        println("typed \"k\" -> ${viewModel.state.first { it == SearchState.Idle }}")

        println("searches run: ${repository.calls.size} (kot, zz, error; none for \"k\" or \"ko\")")
        screenScope.cancel()
    }
