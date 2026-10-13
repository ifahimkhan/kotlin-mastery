# Kotlin Mastery

[![Build](https://github.com/ifahimkhan/kotlin-mastery/actions/workflows/build.yml/badge.svg)](https://github.com/ifahimkhan/kotlin-mastery/actions/workflows/build.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A free, hands-on course that takes you from **your first `println`** to
**coroutines and Flow**. Every concept is a small program you can run, read
and break.

- **Runnable**: every lesson has its own `main`. Click ▶ and see the result.
- **Visible**: each lesson prints what happens and documents the exact output in its header.
- **Logical**: comments explain *why*, not just *what*, and show the common pitfall next to the fix.
- **Checked**: every level has exercises with unit tests that tell you when your answer is right.

## Quick start

**Requirements:** JDK 17 or newer, and [IntelliJ IDEA](https://www.jetbrains.com/idea/) (the Community edition is fine).

```bash
git clone https://github.com/ifahimkhan/kotlin-mastery.git
cd kotlin-mastery
./gradlew test        # Windows: gradlew.bat test
```

Then open the folder in IntelliJ (**File ▸ Open**, and pick the folder) and wait for the Gradle sync to finish.

## How to study

1. Follow [ROADMAP.md](ROADMAP.md) top to bottom and tick lessons off as you go.
2. Open a lesson and **predict the output before you run it**.
3. Run it with the ▶ next to `fun main`, or from the terminal:
   ```bash
   ./gradlew -q run -Plesson=l01_basics.lesson01_hello.HelloWorldKt
   ```
4. Do the **TRY IT** tasks at the end of each lesson. Change the code and break it on purpose.
5. Solve the level's exercises, then check them:
   ```bash
   ./gradlew exercises --tests "l01_*"
   ```
   A red test shows a hint. Make all of them green before moving on.
6. Build the level's **practice projects**. They combine everything in the level.

## Levels

| Level | Topic | Status |
|------:|-------|:------:|
| 01 | [Basics](src/main/kotlin/l01_basics) — variables, types, strings, operators, input | ✅ |
| 02 | [Control flow](src/main/kotlin/l02_control_flow) — if, when, ranges, loops, labels | ✅ |
| 03 | [Functions](src/main/kotlin/l03_functions) — defaults, named args, vararg, extensions, tailrec | ✅ |
| 04 | [Null safety](src/main/kotlin/l04_null_safety) — `?.`, `?:`, `!!`, smart casts, lateinit, lazy | ✅ |
| 05 | [OOP](src/main/kotlin/l05_oop) — classes, interfaces, data/sealed/enum classes, objects | ✅ |
| 06 | [Collections](src/main/kotlin/l06_collections) — List/Set/Map, transformations, sequences | ✅ |
| 07 | Functional — lambdas, higher-order functions, scope functions, inline | 🔜 |
| 08 | Generics — variance, constraints, reified | 🔜 |
| 09 | Coroutines — builders, dispatchers, structured concurrency, cancellation, channels | 🔜 |
| 10 | Flow — operators, StateFlow, SharedFlow, testing | 🔜 |
| 11 | Advanced — delegation, DSLs, operator overloading, interop | 🔜 |
| 12 | Capstone projects | 🔜 |

## Repository layout

```
src/main/kotlin/lNN_topic/
├── README.md         theory summary + cheat sheet for the level
├── lessonNN_name/    one runnable lesson per folder
├── exercises/        your answers go here (TODOs)
├── solutions/        reference answers (try first!)
└── practice/         mini-projects
src/test/kotlin/      tests for exercises, solutions and practice projects
```

| Command | What it does |
|---------|--------------|
| `./gradlew test` | Checks the reference solutions, practice projects, and that every lesson prints what its header says |
| `./gradlew exercises` | Checks **your** answers in every `exercises/` folder |
| `./gradlew exercises --tests "l02_*"` | Checks your answers for one level only |
| `./gradlew -q run -Plesson=<package>.<File>Kt` | Runs one lesson |

## Contributing

Found a typo, an unclear explanation or a better example? Issues and pull
requests are welcome. Read [docs/CONVENTIONS.md](docs/CONVENTIONS.md) first,
so new lessons look like the existing ones.

## License

[MIT](LICENSE). Use it, share it, teach with it.
