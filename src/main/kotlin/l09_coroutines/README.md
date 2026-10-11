# Level 09 — Coroutines

> Do many things at once without drowning in threads. Write `suspend`
> functions, start coroutines, run work concurrently, cancel it, time it
> out, handle its failures, share state safely, let coroutines talk
> through channels, and test all of it on a virtual clock.

## Lessons

| # | Lesson | You will learn |
|---|--------|----------------|
| 09.1 | [ThreadsVsCoroutines](lesson01_threads_vs_coroutines/ThreadsVsCoroutines.kt) | why threads are expensive and coroutines cheap |
| 09.2 | [SuspendFunctions](lesson02_suspend/SuspendFunctions.kt) | `suspend`, where it may be called, suspending vs blocking |
| 09.3 | [CoroutineBuilders](lesson03_builders/CoroutineBuilders.kt) | `runBlocking`, `launch`, `async` and what each returns |
| 09.4 | [JobLifecycle](lesson04_job_lifecycle/JobLifecycle.kt) | `Job` states, `join`, `isActive`/`isCompleted`/`isCancelled` |
| 09.5 | [AsyncAwait](lesson05_async_await/AsyncAwait.kt) | concurrent `async`/`await`, sequential vs concurrent timing |
| 09.6 | [Dispatchers](lesson06_dispatchers/Dispatchers.kt) | `Default`, `IO`, `Unconfined`, `limitedParallelism`, `Thread.sleep` vs `delay` |
| 09.7 | [WithContext](lesson07_with_context/WithContext.kt) | switch dispatcher for a block and get its result back |
| 09.8 | [StructuredConcurrency](lesson08_structured_concurrency/StructuredConcurrency.kt) | parent/child rules, `coroutineScope`, why no work leaks |
| 09.9 | [Cancellation](lesson09_cancellation/Cancellation.kt) | cooperative cancellation, `ensureActive`, `yield`, `finally`, `NonCancellable`, never swallow `CancellationException` |
| 09.10 | [Timeouts](lesson10_timeouts/Timeouts.kt) | `withTimeout`, `withTimeoutOrNull`, fallbacks, leaking resources |
| 09.11 | [Exceptions](lesson11_exceptions/Exceptions.kt) | `try`/`catch`/`finally`, `launch` vs `async` failures, handlers, `supervisorScope`, the crash matrix |
| 09.12 | [CoroutineContextBasics](lesson12_context/CoroutineContextBasics.kt) | context elements, `+`, `CoroutineName`, what children inherit |
| 09.13 | [SharedState](lesson13_shared_state/SharedState.kt) | race conditions, `Mutex.withLock`, atomics, confinement |
| 09.14 | [Channels](lesson14_channels/Channels.kt) | send/receive/close, capacities, `produce`, pipelines, fan-out, fan-in |
| 09.15 | [Select](lesson15_select/Select.kt) | `select` over channels and deferred results, bias |
| 09.16 | [TestingCoroutines](lesson16_testing/TestingCoroutines.kt) | `runTest`, virtual time, test dispatchers, injecting dispatchers |
| 09.17 | [Parallelism](lesson17_parallelism/Parallelism.kt) | concurrency vs parallelism, CPU work on all cores, why `async` alone is not parallel |

## Practice projects

| Project | Combines |
|---------|----------|
| [Downloader](practice/downloader/Downloader.kt) | many downloads at once with a concurrency limit, progress and failures |
| [Kitchen](practice/kitchen/Kitchen.kt) | a channel pipeline: orders -> chefs (fan-out) -> waiter, clean shutdown |
| [RateLimitedClient](practice/ratelimit/RateLimitedClient.kt) | rate limiter, per-call timeout, retries with exponential backoff, failures as values |

## Exercises

1. Open [exercises/CoroutinesExercises.kt](exercises/CoroutinesExercises.kt).
2. Replace each `TODO(...)` with your answer.
3. Check: `./gradlew exercises --tests "l09_*"`. All 10 tests (9 exercises) should pass.
   The tests use virtual time, so they finish in milliseconds even when your
   code waits for seconds.

## Cheat sheet

```kotlin
suspend fun load(): Data { delay(100); return Data() }   // may pause, never blocks

runBlocking { }                    // bridge from normal code (main, tests) - blocks the thread
coroutineScope { }                 // inside suspend code: waits for all children
val job = scope.launch { }         // fire and forget -> Job
val d = scope.async { compute() }  // a future result -> Deferred<T>
d.await()                          // get it (rethrows its failure)

withContext(Dispatchers.IO) { readFile() }  // switch thread, return a result
Dispatchers.Default                          // CPU work
Dispatchers.IO                               // blocking I/O
Dispatchers.Default.limitedParallelism(1)    // one task at a time
withContext(Dispatchers.Default) {           // CPU work on every core:
    chunks.map { async { work(it) } }.awaitAll()  // split, run in parallel, combine
}

job.cancelAndJoin()                // ask to stop, wait until stopped
ensureActive(); yield()            // let long loops notice cancellation
withContext(NonCancellable) { }    // suspending cleanup in finally
withTimeoutOrNull(500) { call() } ?: fallback

supervisorScope { }                // one child failing doesn't cancel the others
CoroutineExceptionHandler { _, e -> log(e) }  // ROOT launch only; not for async (use await)

mutex.withLock { counter++ }       // one coroutine at a time
val ch = Channel<Int>(capacity)    // send / receive / close; for (x in ch)
produce { send(x) }                // a coroutine that owns its channel
select { a.onReceive { it }; b.onAwait { it } }

@Test fun t() = runTest { delay(60_000); assertEquals(60_000, testScheduler.currentTime) }
```

## Key ideas

- **Suspending is not blocking.** `delay` frees the thread for other coroutines, while `Thread.sleep` holds it. Never block inside a coroutine; switch to `Dispatchers.IO` for blocking calls.
- **Structured concurrency.** Every coroutine has a parent scope. The scope waits for its children, cancels them when it is cancelled, and receives their failures. Nothing leaks, and nothing fails silently.
- **Cancellation is cooperative.** It only happens at suspension points and at explicit checks (`ensureActive`, `yield`, `isActive`). Always rethrow `CancellationException`.
- **Every remote call needs a time limit.** Use `withTimeoutOrNull` and fall back to a sensible value.
- **Shared mutable state needs protection.** A `Mutex`, an atomic or confinement all work. Better still, avoid shared state: return results with `async`, or pass values through channels.
- **Concurrency is not parallelism.** `async` only starts work and returns its result later. On one thread, CPU tasks take turns; they run at the same instant only on a multi-threaded dispatcher such as `Dispatchers.Default`.
- **Inject dispatchers and waits.** Code that takes its dispatcher as a parameter can be tested with `runTest` on virtual time: fast and deterministic.

**Next:** Level 10 — Flow
