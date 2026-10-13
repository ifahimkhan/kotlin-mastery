# Level 04 — Null safety

> Kotlin's most famous feature: the compiler forces you to deal with
> missing values *before* your program runs, not after it crashes.

## Lessons

| # | Lesson | You will learn |
|---|--------|----------------|
| 04.1 | [NullableTypes](lesson01_nullable/NullableTypes.kt) | `String` vs `String?`, what the compiler forbids, the billion-dollar mistake |
| 04.2 | [SafeCallAndElvis](lesson02_safe_call/SafeCallAndElvis.kt) | `?.`, call chains, `?:` defaults, `?: return`, `?: throw` |
| 04.3 | [NotNullAssertion](lesson03_not_null/NotNullAssertion.kt) | `!!` and why to avoid it, `requireNotNull`, `checkNotNull` |
| 04.4 | [LetAndSmartCasts](lesson04_let_smart_cast/LetAndSmartCasts.kt) | `?.let { }`, smart casts after `!= null` and `is`, the mutable-property limit |
| 04.5 | [LateinitAndLazy](lesson05_lateinit_lazy/LateinitAndLazy.kt) | `lateinit var`, `::x.isInitialized`, `by lazy` |
| 04.6 | [PlatformTypes](lesson06_platform/PlatformTypes.kt) | `String!` from Java, protecting the Java/Kotlin border |
| 04.7 | [SafeCastsAndCollections](lesson07_safe_casts/SafeCastsAndCollections.kt) | `as?`, `listOfNotNull`, `filterNotNull`, `mapNotNull` |

The Java class used by lesson 04.6 is in
[`src/main/java/l04_null_safety/lesson06_platform`](../../java/l04_null_safety/lesson06_platform/LegacyUserDirectory.java).

## Practice projects

| Project | Combines |
|---------|----------|
| [SafeInputParser](practice/safeinput/SafeInputParser.kt) | nullable return types meaning "invalid", re-asking until valid, `readlnOrNull()` |
| [CrashMuseum](practice/crashmuseum/CrashMuseum.kt) | five classic crashes (NPE, NumberFormat, lateinit, ClassCast, substring) and their safe fixes |

## Exercises

1. Open [exercises/NullSafetyExercises.kt](exercises/NullSafetyExercises.kt).
2. Replace each `TODO(...)` with your answer. **No `!!` allowed.**
3. Check: `./gradlew exercises --tests "l04_*"`. All 8 tests should pass.

## Cheat sheet

```kotlin
val name: String = "Ada"        // never null
val nick: String? = null        // may be null

nick?.length                    // Int?  - null if nick is null
nick?.length ?: 0               // Int   - default when null
val n = nick ?: return          // leave early when null
val m = nick ?: throw IllegalArgumentException("nick required")

nick?.let { println(it) }       // run only when not null
if (nick != null) nick.length   // smart cast to String

requireNotNull(x) { "msg" }     // IllegalArgumentException when null (bad input)
checkNotNull(x) { "msg" }       // IllegalStateException when null (bad state)
x!!                             // NullPointerException when null - avoid!

lateinit var url: String        // assigned later, non-null type
val report by lazy { build() }  // computed on first use, then cached

value as? String                // null instead of ClassCastException
list.filterNotNull()            // List<T?> -> List<T>
texts.mapNotNull { it.toIntOrNull() }
```

## Key ideas

- **Non-null is the default.** Add `?` only when "missing" is a real, valid state.
- **Decide at the border.** When data enters (user input, Java, files), give it an explicit `?` type once.
- **Prefer defaults over crashes.** `?:` first, `require`/`check` when stopping really is right, and `!!` almost never.
- **Smart casts need stable values.** Copy a `var` property into a local `val` before checking it.
- **`List<String?>` and `List<String>?` are different types.** In the first the items may be null; in the second the list itself may be null.

**Next:** Level 05 — Object-oriented programming
