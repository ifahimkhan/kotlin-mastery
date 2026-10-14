# Level 12 — Capstone projects

> Three real projects that combine everything from levels 01–11. No more
> small exercises: each capstone is a program you design, build milestone
> by milestone, test, and run.

## The projects

| # | Project | You build | Main levels used |
|---|---------|-----------|------------------|
| 12.A | [Library manager](library/README.md) | a CLI with catalogue, members, loans, fines and a JSON save file | 01–07, serialization |
| 12.B | [Weather aggregator](weather/README.md) | concurrent API calls with timeouts, retries, partial results and live refresh, using a real web API (Ktor) | 05, 09, 10 |
| 12.C | [Task scheduler](scheduler/README.md) | a worker pool with priorities, repetition, retries, cancellation and shutdown | 06, 09, 10 |

**Suggested order:** A, then B, then C. Each one is harder than the last.

## How each capstone works

Every project folder has the same shape:

```
<project>/
├── README.md        the brief: rules, milestones, hints, stretch goals
├── <Contract>.kt    the interface you implement, with KDoc for every function
├── starter/         YOUR implementation: every body is TODO()
├── reference/       one finished answer (peek only when stuck)
└── Main.kt          a runnable program (uses the reference until you switch it)
```

The same **acceptance tests** check the reference (in `./gradlew test`) and
your starter (in `./gradlew exercises`). They're grouped by milestone, so
you can go one step at a time:

```bash
./gradlew exercises --tests "*LibraryStarterTest.m1*"        # one milestone
./gradlew exercises --tests "l12_capstones.library*"         # one project
./gradlew exercises --tests "l12_capstones*"                 # all capstones
```

## Run them

```bash
./gradlew -q run -Plesson=l12_capstones.library.MainKt
./gradlew -q run -Plesson=l12_capstones.weather.MainKt                   # offline, fake data
./gradlew -q run -Plesson=l12_capstones.weather.MainKt --args="--live"   # real weather
./gradlew -q run -Plesson=l12_capstones.scheduler.MainKt
```

## Where to go from here

You now know the language, the standard library, coroutines and Flow. Pick
a direction:

- **Android with Jetpack Compose:** your StateFlow and coroutine skills map
  directly onto ViewModels and Compose state.
  Start with [developer.android.com/courses](https://developer.android.com/courses).
- **Backend with Ktor server:** you've used the Ktor *client*; the server
  uses the same coroutine-first style. Turn the library capstone into a
  REST API. See [ktor.io](https://ktor.io/docs/).
- **Kotlin Multiplatform:** share the rules of a capstone (pure functions!)
  between Android, iOS, desktop and web.
  See [kotlinlang.org/docs/multiplatform](https://kotlinlang.org/docs/multiplatform.html).
- **Go deeper:** read the source of `kotlinx.coroutines`, try the Kotlin
  Koans, and contribute to an open-source Kotlin project.

Congratulations on reaching the end of the roadmap.
