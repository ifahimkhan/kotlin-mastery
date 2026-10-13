package l06_collections.practice.grades

import kotlin.math.roundToLong

/**
 * =====================================================================
 *  PRACTICE 06.A — Student grade analytics
 * =====================================================================
 *  GOAL   : Answer real questions about a class with collection
 *           functions: top students, averages, distributions.
 *  RUN    : ./gradlew -q run -Plesson=l06_collections.practice.grades.StudentGradeAnalyticsKt
 *  OUTPUT :
 *           Top 3: Mia (97), Ola (91), Ben (88)
 *           Average per class: 10A=77.8, 10B=70.8
 *           Grade distribution: A=2, B=2, C=1, D=1, F=2
 *           Passed (6): Mia, Ben, Ivy, Ola, Kai, Lea
 *           Failed (2): Tom, Zoe
 *  DESIGN : Every question is a small pure function that takes a
 *           List<Student> and returns data. `main` only formats output.
 *  NEW    : `data class` (level 05) bundles related values with a
 *           readable toString(), equals() and copy().
 *  EXTEND : Add "students above their class average" and a per-class
 *           best student using groupBy + maxBy.
 * =====================================================================
 */

data class Student(val name: String, val className: String, val score: Int)

const val PASS_MARK = 60
private const val A_MIN = 90
private const val B_MIN = 80
private const val C_MIN = 70
private const val D_MIN = PASS_MARK
private const val ROUNDING = 10.0
private const val TOP_COUNT = 3

/** Grade letters in display order, so the distribution prints A..F. */
val GRADE_LETTERS = listOf('A', 'B', 'C', 'D', 'F')

fun letterGrade(score: Int): Char =
    when {
        score >= A_MIN -> 'A'
        score >= B_MIN -> 'B'
        score >= C_MIN -> 'C'
        score >= D_MIN -> 'D'
        else -> 'F'
    }

/** The [n] best students; equal scores keep their original order (stable sort). */
fun topStudents(students: List<Student>, n: Int): List<Student> {
    require(n >= 0) { "n must not be negative, was $n" }
    return students.sortedByDescending { it.score }.take(n)
}

/** Average score per class, rounded to one decimal, in first-seen class order. */
fun averageByClass(students: List<Student>): Map<String, Double> =
    students
        .groupBy { it.className }
        .mapValues { (_, members) -> roundTo1Decimal(members.map { it.score }.average()) }

/** How many students got each grade. Every letter appears, even with count 0. */
fun gradeDistribution(students: List<Student>): Map<Char, Int> {
    val counts = students.groupingBy { letterGrade(it.score) }.eachCount()
    return GRADE_LETTERS.associateWith { counts[it] ?: 0 }
}

/** (passed, failed) in the original order. */
fun passFail(students: List<Student>): Pair<List<Student>, List<Student>> =
    students.partition { it.score >= PASS_MARK }

private fun roundTo1Decimal(value: Double): Double = (value * ROUNDING).roundToLong() / ROUNDING

val SAMPLE_STUDENTS =
    listOf(
        Student("Mia", "10A", 97),
        Student("Tom", "10A", 52),
        Student("Ben", "10A", 88),
        Student("Ivy", "10A", 74),
        Student("Ola", "10B", 91),
        Student("Zoe", "10B", 45),
        Student("Kai", "10B", 83),
        Student("Lea", "10B", 64),
    )

fun main() {
    val students = SAMPLE_STUDENTS

    println("Top $TOP_COUNT: " + topStudents(students, TOP_COUNT).joinToString { "${it.name} (${it.score})" })
    println("Average per class: " + averageByClass(students).entries.joinToString { "${it.key}=${it.value}" })
    println("Grade distribution: " + gradeDistribution(students).entries.joinToString { "${it.key}=${it.value}" })

    val (passed, failed) = passFail(students)
    println("Passed (${passed.size}): " + passed.joinToString { it.name })
    println("Failed (${failed.size}): " + failed.joinToString { it.name })
}
