package l06_collections.practice

import l06_collections.practice.grades.SAMPLE_STUDENTS
import l06_collections.practice.grades.Student
import l06_collections.practice.grades.averageByClass
import l06_collections.practice.grades.gradeDistribution
import l06_collections.practice.grades.letterGrade
import l06_collections.practice.grades.passFail
import l06_collections.practice.grades.topStudents
import kotlin.test.Test
import kotlin.test.assertEquals

class StudentGradeAnalyticsTest {
    @Test
    fun `letter grades use lower-inclusive limits`() {
        assertEquals(listOf('A', 'B', 'C', 'D', 'F', 'F'), listOf(90, 89, 70, 60, 59, 0).map { letterGrade(it) })
    }

    @Test
    fun `top students keep original order on equal scores`() {
        val tied = listOf(Student("A", "x", 80), Student("B", "x", 90), Student("C", "x", 80))
        assertEquals(listOf("B", "A"), topStudents(tied, 2).map { it.name })
        assertEquals(emptyList(), topStudents(emptyList(), 3))
    }

    @Test
    fun `averages are rounded per class`() {
        assertEquals(mapOf("10A" to 77.8, "10B" to 70.8), averageByClass(SAMPLE_STUDENTS))
    }

    @Test
    fun `distribution lists every grade even when zero`() {
        assertEquals(mapOf('A' to 0, 'B' to 0, 'C' to 0, 'D' to 0, 'F' to 0), gradeDistribution(emptyList()))
        assertEquals(8, gradeDistribution(SAMPLE_STUDENTS).values.sum())
    }

    @Test
    fun `pass fail split keeps everyone`() {
        val (passed, failed) = passFail(SAMPLE_STUDENTS)
        assertEquals(listOf("Tom", "Zoe"), failed.map { it.name })
        assertEquals(SAMPLE_STUDENTS.size, passed.size + failed.size)
    }
}
