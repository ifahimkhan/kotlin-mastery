# Level 06 — Collections

> Lists, sets and maps, and the functions that turn ten-line loops into a
> single readable line: `map`, `filter`, `groupBy`, `fold` and friends.

## Lessons

| # | Lesson | You will learn |
|---|--------|----------------|
| 06.1 | [LambdaPrimer](lesson01_lambda_primer/LambdaPrimer.kt) | just enough lambdas: `{ x -> ... }`, `it`, trailing lambdas |
| 06.2 | [Lists](lesson02_lists/Lists.kt) | `List` vs `MutableList`, safe indexing, `buildList`, arrays |
| 06.3 | [Sets](lesson03_sets/Sets.kt) | uniqueness, `union`/`intersect`/`subtract`, which sets keep order |
| 06.4 | [Maps](lesson04_maps/Maps.kt) | `to`/`Pair`, nullable lookups, destructuring loops, `map + map`, `toMutableMap`, `filterValues`, `getOrPut` |
| 06.5 | [Transformations](lesson05_transformations/Transformations.kt) | `map`, `mapNotNull`, `flatMap`, `zip`, `associate*` |
| 06.6 | [Filtering](lesson06_filtering/Filtering.kt) | `filter`, `partition`, `any`/`all`/`none`, `firstOrNull`, `take`/`drop` |
| 06.7 | [Aggregation](lesson07_aggregation/Aggregation.kt) | `sumOf`, `maxBy`, `reduce` vs `fold`, `groupBy`, `eachCount`, `chunked`, `windowed` |
| 06.8 | [Sorting](lesson08_sorting/Sorting.kt) | `sorted`, `sortedBy`, `compareBy`/`thenBy`, stable sorting |
| 06.9 | [Sequences](lesson09_sequences/Sequences.kt) | lazy vs eager evaluation (with printed traces), `generateSequence` |
| 06.10 | [CollectionPitfalls](lesson10_pitfalls/CollectionPitfalls.kt) | read-only is not immutable, `ConcurrentModificationException`, `+`/`-` copies |

## Practice projects

| Project | Combines |
|---------|----------|
| [StudentGradeAnalytics](practice/grades/StudentGradeAnalytics.kt) | `sortedByDescending`, `groupBy` + `mapValues`, `eachCount`, `partition` |
| [WordFrequency](practice/words/WordFrequency.kt) | reading a resource file, normalising text, counting, top-N with tie-breaks |
| [InventoryReport](practice/inventory/InventoryReport.kt) | money in cents, per-category totals, immutable updates with `map` + `copy` |

## Exercises

1. Open [exercises/CollectionsExercises.kt](exercises/CollectionsExercises.kt).
2. Replace each `TODO(...)` with your answer. Try it without `for` loops.
3. Check: `./gradlew exercises --tests "l06_*"`. All 10 tests should pass.

## Cheat sheet

```kotlin
val list = listOf(3, 1, 2)            // read-only
val bag = mutableListOf(3, 1, 2)      // add/remove/set allowed
val set = setOf("a", "b")             // unique, keeps insertion order
val map = mapOf("a" to 1, "b" to 2)   // key -> value

list.getOrNull(9)                     // null instead of a crash
map["c"] ?: 0                         // lookups are nullable
for ((key, value) in map) { }         // destructuring

list.map { it * 2 }                   // transform each
list.filter { it > 1 }                // keep matching
list.partition { it > 1 }             // (matching, rest)
list.any { it > 2 }; list.all { it > 0 }; list.none { it < 0 }
list.firstOrNull { it > 5 }           // null when nothing matches
list.flatMap { listOf(it, it) }       // map, then flatten
list.zip(listOf("a", "b"))            // pairs, stops at the shorter list
words.associateWith { it.length }     // List -> Map

list.sum(); list.sumOf { it * 2 }; list.maxOrNull()
list.fold(0) { acc, n -> acc + n }    // safe on empty lists
words.groupBy { it.first() }          // key -> list of items
text.groupingBy { it }.eachCount()    // key -> count
list.chunked(2); list.windowed(2)     // batches / sliding windows

people.sortedWith(compareBy<Person> { it.city }.thenByDescending { it.age })
list.asSequence().map { }.filter { }.first()  // lazy: stops early

list + 4; list - 1                    // NEW lists, original unchanged
```

## Key ideas

- **Read-only by default.** Use `List`/`Set`/`Map` and keep `Mutable*` small and private.
- **Read-only is not immutable.** Hand out copies (`toList()`) of data you must protect.
- **Transform instead of mutating.** `map`/`filter`/`+` return new collections, so there are no surprises.
- **Lookups are nullable.** `map[key]` and `getOrNull` force you to handle "missing".
- **Prefer `fold` to `reduce`, and `...OrNull` to throwing versions,** when the collection might be empty.
- **Order matters.** `setOf`/`mapOf` keep insertion order. A plain `HashSet`/`HashMap` has no reliable order.
- **Sequences are for big or infinite data, or when you stop early.** Lists are simpler for small data.

**Next:** Level 07 — Functional programming
