# Repository conventions

Every level looks the same, so learners always know where to find things.
Follow these rules when adding lessons.

## Level layout

```
src/main/kotlin/lNN_topic/
├── README.md                     # theory summary + lesson list + cheat sheet
├── <Topic>Tasks.kt               # interface: one function per exercise, KDoc = task
├── lessonNN_name/<Name>.kt       # one lesson per package (each has its own main)
├── exercises/<Topic>Exercises.kt # object implementing Tasks, every body = TODO("...")
├── solutions/<Topic>Solutions.kt # object implementing Tasks, commented answers
└── practice/<project>/<Name>.kt  # runnable mini-projects with testable functions

src/test/kotlin/lNN_topic/
├── <Topic>TasksTest.kt           # abstract tests, constructor takes the Tasks
├── <Topic>SolutionsTest.kt       # runs on ./gradlew test (keeps CI green)
├── <Topic>ExercisesTest.kt       # @Tag("exercise"), runs on ./gradlew exercises
└── practice/<Name>Test.kt        # tests for practice projects
```

Why one package per lesson? Two top-level `main` functions in the same
package clash. A package per lesson also gives a clean run command:
`./gradlew -q run -Plesson=<package>.<FileName>Kt`.

## Lesson header

```kotlin
/**
 * =====================================================================
 *  LESSON 02.3 — Title
 * =====================================================================
 *  GOAL   : One or two sentences: what the learner can do afterwards.
 *  RUN    : ./gradlew -q run -Plesson=l02_control_flow.lesson03_ranges.RangesKt
 *  OUTPUT :
 *           exact output, line by line
 *  NEXT   : lesson04_loops/Loops.kt
 * =====================================================================
 */
```

- `LessonOutputTest` runs every lesson and practice project and compares stdout with the OUTPUT
  block. Keep the output **deterministic**.
- If output depends on input, time or threads, put text in parentheses on
  the OUTPUT line itself, e.g. `OUTPUT : (example session: you type 7)`.
  That opts the file out of the check. A file with no OUTPUT line fails the test.
- Header labels: GOAL, RUN, OUTPUT, NEXT, NEW, DESIGN, EXTEND, NOTE. The
  output block ends at the next one of these labels.
- Every test has a 5-second timeout (`src/test/resources/junit-platform.properties`),
  so a learner's endless loop fails instead of hanging.

## Lesson body sections

Use these numbered banners, in this order, and skip the ones that don't apply:

```kotlin
// ── 1. CONCEPT: ... ───────────────────────────────────────────────
// ── 2. WHY ... ───────────────────────────────────────────────────
// ── 3. PITFALL: ... ──────────────────────────────────────────────
// ── 4. TRY IT ────────────────────────────────────────────────────
```

- Show, then explain: print the result so the learner **sees** it.
- Show a pitfall as commented-out code, with the exact compiler error or
  exception name.
- End with 2–3 "TRY IT" tasks.
- Only use features from the current or earlier levels. If you must use a
  later feature, add a short comment such as "(level 04)".

## Exercises

- 6–10 exercises per level, numbered `Ex 1`..`Ex N` in the Tasks KDoc.
- Exercise bodies are expression-bodied: `= TODO("Ex 3: short instruction")`.
- Put a `// Hint: lessonNN_x (...)` comment above each exercise.
- Tests are named `` `exN what it checks` `` and include edge cases (empty,
  zero, negative) with hint messages in the assertions.

## Style

- Official Kotlin style, 4 spaces, max ~120 chars.
- Prefer `val`, use no `!!` except in lessons that teach why it's dangerous,
  and no magic numbers in solutions or practice code (use `const val`).
- Keep printed text ASCII. Windows consoles garble characters like `°` and `—`.
  Unicode in comments is fine.
- Never put real secrets (passwords, API keys, tokens) in examples.
