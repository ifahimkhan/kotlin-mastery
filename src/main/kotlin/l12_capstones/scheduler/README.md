# Capstone 12.C — Task scheduler

> Build a small job runner, the heart of every background-work system.
> It runs tasks on a pool of workers with priorities, delays,
> repetitions, retries, cancellation and a clean shutdown.

## What you build

A class that implements [TaskScheduler](TaskScheduler.kt):

```kotlin
val scheduler = SchedulerStarter.create(scope, SchedulerConfig(maxConcurrent = 2))
val id = scheduler.schedule(TaskSpec("report", priority = 5, repeat = Repeat.Every(1_000, times = 3)) { sendReport() })
scheduler.statuses.collect { println(it) }   // Pending -> Running(1) -> Pending -> ... -> Succeeded(3)
scheduler.cancel(id)
scheduler.shutdown(ShutdownMode.DRAIN)
```

The model is already written in [Model.kt](Model.kt): `TaskSpec`,
`Repeat`, `TaskStatus`, `ShutdownMode`. Your class goes in
[starter/](starter/SchedulerStarter.kt).

This is the hardest capstone. It brings together nearly all of level 09
and the StateFlow part of level 10.

## Milestones

| # | Build | Uses | Check |
|---|-------|------|-------|
| 1 | `schedule`, `statuses`, worker pool, initial delay, `shutdown(DRAIN)` | l09 `launch`, channels, `delay`; l10 `MutableStateFlow.update` | `--tests "*SchedulerStarterTest.m1*"` |
| 2 | priorities (higher first, ties FIFO) and `Repeat.Every` | `java.util.PriorityQueue`, l06 `compareBy` | `m2*` |
| 3 | retries with exponential backoff, failures don't kill workers | l09 exceptions, `async`/`await`, `SupervisorJob` | `m3*` |
| 4 | `cancel(id)`, `CANCEL_PENDING`, `CANCEL_ALL` | l09 cancellation, `Job.cancel`, `ensureActive` | `m4*` |

```bash
./gradlew exercises --tests "*SchedulerStarterTest.m1*"
./gradlew exercises --tests "l12_capstones.scheduler*"   # everything
```

The tests pass your scheduler a test scope, so all timing is virtual: a
1-second delay takes no real time, and `currentTime` proves when each
task ran.

## Run the demo

```bash
./gradlew -q run -Plesson=l12_capstones.scheduler.MainKt
```

## Hints

- Workers should never sit inside a `delay` for a retry or a repetition.
  Give the task a small "timer" coroutine that puts it back in the queue
  later, and keep the worker free.
- Run each attempt in its own `async` inside a `SupervisorJob` scope.
  A failure stays in the `Deferred`, `cancel(id)` can stop just that
  run, and one bad task can't kill the scheduler.
- When `await()` throws `CancellationException`, there are three possible
  reasons. Check them in this order:
  1. The whole scheduler is stopping: `currentCoroutineContext().ensureActive()`
     rethrows, so the worker ends.
  2. `cancel(id)` stopped this task: its status is already Cancelled, so do nothing.
  3. Otherwise the task's own code threw it (for example a `withTimeout`
     inside the action). That is a failed attempt, so retry it or mark it Failed.
- Catch `Throwable`, not just `Exception`. A task calling `TODO()` throws an
  Error, and an uncaught Error kills the worker and leaves the task
  Running forever.
- `shutdown` can simply wait for `statuses.first { all final }`.
- Keep shared state behind one lock in short, non-suspending blocks. When
  you check something and then act on it (is it final? then start a timer),
  do both inside the same lock block. Otherwise `cancel()` can slip in between.

## Stretch goals

- Cron-like schedules ("every day at 09:00") using an injected clock.
- Task dependencies: B runs only after A succeeded.
- Persist pending tasks so a restart resumes them (see the library save file).
- Prune finished tasks from `statuses` after a while, so a long-running
  scheduler doesn't grow forever.
