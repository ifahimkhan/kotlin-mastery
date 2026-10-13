package l02_control_flow.practice.grades

/**
 * =====================================================================
 *  PRACTICE 02.C — Grade calculator
 * =====================================================================
 *  GOAL   : Validate scores, compute an average with a loop and map it
 *           to a letter grade with `when`.
 *  RUN    : ./gradlew -q run -Plesson=l02_control_flow.practice.grades.GradeCalculatorKt
 *  OUTPUT :
 *           Scores : 78, 92, 85, 64
 *           Average: 79.75
 *           Grade  : C
 *           Best   : 92 (A)
 *  DESIGN : Small pure functions + a tiny main that prints. Lists are
 *           covered in level 06; here we only loop over one.
 *  EXTEND : Read the scores from the user, one per line, until "done".
 * =====================================================================
 */

const val MIN_SCORE = 0
const val MAX_SCORE = 100

private const val A_FROM = 90.0
private const val B_FROM = 80.0
private const val C_FROM = 70.0
private const val D_FROM = 60.0

/** Letter grade for a score, which may have decimals (an average). */
fun letterGrade(score: Double): Char {
    require(score in MIN_SCORE.toDouble()..MAX_SCORE.toDouble()) {
        "score must be in $MIN_SCORE..$MAX_SCORE, was $score"
    }
    return when {
        score >= A_FROM -> 'A'
        score >= B_FROM -> 'B'
        score >= C_FROM -> 'C'
        score >= D_FROM -> 'D'
        else -> 'F'
    }
}

/** Average of the scores. Fails for an empty list or a score outside 0..100. */
fun averageScore(scores: List<Int>): Double {
    require(scores.isNotEmpty()) { "need at least one score" }
    var total = 0
    for (score in scores) {
        require(score in MIN_SCORE..MAX_SCORE) { "score must be in $MIN_SCORE..$MAX_SCORE, was $score" }
        total += score
    }
    return total.toDouble() / scores.size
}

/** Highest score, found with a plain loop (level 06 shows `max()`). */
fun bestScore(scores: List<Int>): Int {
    require(scores.isNotEmpty()) { "need at least one score" }
    var best = scores[0]
    for (score in scores) {
        if (score > best) best = score
    }
    return best
}

fun main() {
    val scores = listOf(78, 92, 85, 64)
    val average = averageScore(scores)
    val best = bestScore(scores)

    println("Scores : ${scores.joinToString(", ")}")
    println("Average: $average")
    println("Grade  : ${letterGrade(average)}")
    println("Best   : $best (${letterGrade(best.toDouble())})")
}
