package l05_oop.lesson09_enums_sealed

/**
 * =====================================================================
 *  LESSON 05.9 — Enum classes and sealed types
 * =====================================================================
 *  GOAL   : Model "one of a fixed set" safely: enums for fixed VALUES,
 *           sealed types for fixed KINDS that carry different data,
 *           both checked by an exhaustive `when`.
 *  RUN    : ./gradlew -q run -Plesson=l05_oop.lesson09_enums_sealed.EnumsAndSealedKt
 *  OUTPUT :
 *           NORTH: dx=0, dy=1, turn right -> EAST
 *           EAST: dx=1, dy=0, turn right -> SOUTH
 *           SOUTH: dx=0, dy=-1, turn right -> WEST
 *           WEST: dx=-1, dy=0, turn right -> NORTH
 *           entries: [NORTH, EAST, SOUTH, WEST]
 *           valueOf("SOUTH").ordinal = 2
 *           Walk N, N, E from (0, 0) -> (1, 2)
 *           Loading -> Loading...
 *           Success(items=[a, b, c]) -> Got 3 items
 *           Failure(reason=timeout, canRetry=true) -> Failed: timeout (you can retry)
 *           Failure(reason=not found, canRetry=false) -> Failed: not found
 *  NEXT   : lesson10_nested_value_java/NestedValueAndJava.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: enum class = a fixed list of values ──────────────
    // Each value can carry properties (dx, dy) and the enum can have
    // methods (turnRight). `entries` lists them all in order.
    for (direction in Direction.entries) {
        println("$direction: dx=${direction.dx}, dy=${direction.dy}, turn right -> ${direction.turnRight()}")
    }
    println("entries: ${Direction.entries}")
    println("valueOf(\"SOUTH\").ordinal = ${Direction.valueOf("SOUTH").ordinal}")

    // Using the data on each value:
    var x = 0
    var y = 0
    for (step in listOf(Direction.NORTH, Direction.NORTH, Direction.EAST)) {
        x += step.dx
        y += step.dy
    }
    println("Walk N, N, E from (0, 0) -> ($x, $y)")

    // ── 2. CONCEPT: sealed interface = a fixed list of KINDS ─────────
    // Every kind can hold DIFFERENT data: Loading has none, Success has
    // items, Failure has a reason. The compiler knows ALL the kinds.
    val states: List<LoadState> =
        listOf(
            LoadState.Loading,
            LoadState.Success(listOf("a", "b", "c")),
            LoadState.Failure("timeout", canRetry = true),
            LoadState.Failure("not found", canRetry = false),
        )
    for (state in states) {
        println("$state -> ${render(state)}")
    }

    // ── 3. WHY exhaustive `when` without `else`? ─────────────────────
    // render() has NO else branch. If someone adds a new kind (e.g.
    // `Empty`) later, every `when` that forgot it stops compiling. The
    // compiler finds the places you must update for you. An `else` branch
    // would silently swallow the new case.

    // ── 4. ENUM OR SEALED? ───────────────────────────────────────────
    //   enum   : every value has the SAME shape (dx, dy). Fixed instances.
    //   sealed : each kind has its OWN shape (items vs reason). Many instances.

    // ── 5. PITFALL: a missing branch ─────────────────────────────────
    // Remove the Failure branch from render() and you get:
    //   ❌ 'when' expression must be exhaustive. Add the 'is Failure' branch or an 'else' branch.
    // Remove WEST from turnRight() and you get:
    //   ❌ 'when' expression must be exhaustive. Add the 'WEST' branch or an 'else' branch.

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Add `fun turnLeft(): Direction` to the enum.
    // b) Add `data object Empty : LoadState` and fix the compiler error.
    // c) Print every direction whose dx is 0.
}

enum class Direction(val dx: Int, val dy: Int) {
    NORTH(0, 1),
    EAST(1, 0),
    SOUTH(0, -1),
    WEST(-1, 0),
    ; // the semicolon separates the values from the members below

    fun turnRight(): Direction =
        when (this) {
            NORTH -> EAST
            EAST -> SOUTH
            SOUTH -> WEST
            WEST -> NORTH
        }
}

sealed interface LoadState {
    // `data object`: a single instance with a nice toString ("Loading").
    data object Loading : LoadState

    data class Success(val items: List<String>) : LoadState

    data class Failure(val reason: String, val canRetry: Boolean) : LoadState
}

fun render(state: LoadState): String =
    when (state) {
        LoadState.Loading -> "Loading..."
        is LoadState.Success -> "Got ${state.items.size} items" // smart cast: state is Success here
        is LoadState.Failure ->
            if (state.canRetry) "Failed: ${state.reason} (you can retry)" else "Failed: ${state.reason}"
    }
