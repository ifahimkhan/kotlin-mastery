# Level 10 — Flow

> Streams of values over time, built on coroutines. Build cold flows,
> transform them with operators, control where and how fast they run,
> combine them, handle their errors, share them as hot StateFlow and
> SharedFlow, wrap callback APIs, and test all of it on a virtual clock.

## Lessons

| # | Lesson | You will learn |
|---|--------|----------------|
| 10.1 | [FlowBasics](lesson01_flow_basics/FlowBasics.kt) | `flow { }`, `emit`, `collect`, `flowOf`, `asFlow`, cold flows run per collector |
| 10.2 | [IntermediateOperators](lesson02_intermediate_operators/IntermediateOperators.kt) | `map`, `filter`, `transform`, `take`, `onEach`, `scan`, `withIndex`, `transformWhile`, `distinctUntilChanged` |
| 10.3 | [TerminalOperators](lesson03_terminal_operators/TerminalOperators.kt) | `collect`, `toList`, `first`, `single`, `reduce`, `fold`, `count`, `launchIn` |
| 10.4 | [FlowContext](lesson04_flow_context/FlowContext.kt) | context preservation, `flowOn`, why `withContext` around `emit` is wrong |
| 10.5 | [BufferingAndTiming](lesson05_buffering_timing/BufferingAndTiming.kt) | `buffer`, `conflate`, `collectLatest`, `debounce` |
| 10.6 | [Combining](lesson06_combining/Combining.kt) | `zip`, `combine`, `merge`, `flatMapConcat`, `flatMapMerge`, `flatMapLatest` |
| 10.7 | [ErrorsAndCompletion](lesson07_errors_completion/ErrorsAndCompletion.kt) | `catch` (upstream only), exception transparency, `retry`, `retryWhen`, `onCompletion`, `onEmpty` |
| 10.8 | [StateFlowBasics](lesson08_stateflow/StateFlowBasics.kt) | `MutableStateFlow`, `value`, conflation, equal values, `update { }`, `asStateFlow` |
| 10.9 | [SharedFlowBasics](lesson09_sharedflow/SharedFlowBasics.kt) | `MutableSharedFlow`, replay, buffers, `emit` vs `tryEmit`, events vs state vs Channel |
| 10.10 | [StateInShareIn](lesson10_statein_sharein/StateInShareIn.kt) | `stateIn`, `shareIn`, `SharingStarted.Eagerly`/`Lazily`/`WhileSubscribed`, owning the scope |
| 10.11 | [CallbackFlowBasics](lesson11_callbackflow/CallbackFlowBasics.kt) | `callbackFlow`, `trySend`, `awaitClose`, `channelFlow`, choosing a buffer |
| 10.12 | [TestingFlows](lesson12_testing_flows/TestingFlows.kt) | `toList` in `runTest`, Turbine, testing StateFlow and hot flows, virtual-time debounce |

## Practice projects

| Project | Combines |
|---------|----------|
| [StockTicker](practice/ticker/StockTicker.kt) | a price stream, moving averages and alerts with flow operators |
| [SearchAsYouType](practice/search/SearchAsYouType.kt) | `debounce`, `distinctUntilChanged`, `flatMapLatest`, a sealed state, `stateIn` |
| [ChatRoom](practice/chat/ChatRoom.kt) | SharedFlow with replayed history, StateFlow of members, typing events, `channelFlow` feeds |

## Exercises

1. Open [exercises/FlowExercises.kt](exercises/FlowExercises.kt).
2. Replace each `TODO(...)` with your answer.
3. Check: `./gradlew exercises --tests "l10_*"`. All 10 tests should pass.
   The tests use virtual time, so time-based operators finish instantly.

## Cheat sheet

```kotlin
val numbers = flow { emit(1); delay(100); emit(2) }  // cold: runs per collector
flowOf(1, 2, 3); listOf(1, 2).asFlow()

numbers.map { it * 2 }.filter { it > 1 }.take(3)     // intermediate: lazy, return a Flow
numbers.onEach { log(it) }.scan(0) { sum, n -> sum + n }
numbers.collect { println(it) }                      // terminal: runs the flow
numbers.toList(); numbers.first(); numbers.fold(0) { a, n -> a + n }

numbers.flowOn(Dispatchers.Default)                  // run UPSTREAM on another dispatcher
numbers.buffer(); numbers.conflate(); numbers.collectLatest { }
queries.debounce(300).distinctUntilChanged().flatMapLatest { search(it) }
combine(a, b) { x, y -> x + y }; a.zip(b) { x, y -> x to y }; merge(a, b)

numbers.catch { e -> emit(-1) }                      // upstream errors only
numbers.retry(3) { it is IOException }
numbers.onCompletion { cause -> }                    // like finally; cause == null on success

val state = MutableStateFlow(0)                      // hot, always has a value
state.update { it + 1 }; val readOnly: StateFlow<Int> = state.asStateFlow()
val events = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 16)
events.tryEmit("toast")
cold.stateIn(scope, SharingStarted.WhileSubscribed(5_000), initialValue)
cold.shareIn(scope, SharingStarted.Lazily, replay = 1)

callbackFlow { api.register(listener); awaitClose { api.unregister(listener) } }

@Test fun t() = runTest { flow.test { assertEquals(1, awaitItem()); awaitComplete() } }
```

### Cold vs hot

| | Cold `Flow` | Hot `StateFlow` / `SharedFlow` |
|---|---|---|
| Starts | when collected, once **per collector** | exists on its own (or via `stateIn`/`shareIn`) |
| Late collector | gets everything from the start | gets only new values (+ replay / current state) |
| Completes | yes, when the block ends | never |
| Typical use | a query, a request, a computation | app state, events, a shared connection |

### StateFlow vs SharedFlow vs Channel

| | `StateFlow` | `SharedFlow` | `Channel` |
|---|---|---|---|
| Holds | the current value (always one) | the last `replay` values (0 by default) | buffered values until received |
| Each value goes to | every collector (latest only) | every current collector | exactly **one** receiver |
| Equal values | skipped | delivered | delivered |
| Good for | state | broadcast events | one-shot events, work queues |

## Key ideas

- **Flows are cold.** Nothing runs until someone collects, and each collector gets its own run. Use `stateIn`/`shareIn` to share one upstream.
- **Operators build a pipeline; terminal operators run it.** Keep the collector simple and do the work in `map`/`onEach`/`filter`.
- **Exception transparency.** Never wrap `emit` in try/catch. Handle errors with `catch`, which only sees upstream errors.
- **Never emit from another coroutine or context in `flow { }`.** Use `flowOn` to change the upstream's dispatcher, and `channelFlow` to emit from many coroutines.
- **StateFlow is for state.** It conflates and skips equal values, so don't use it for events that must not be lost or merged.
- **Every hot stream lives in a scope.** Cancel that scope, or the upstream keeps running.
- **Test on virtual time.** `runTest` and Turbine make debounce, delay and retry tests instant and deterministic.

**Next:** Level 11 — Advanced (delegation, DSLs, operator overloading, interop).
