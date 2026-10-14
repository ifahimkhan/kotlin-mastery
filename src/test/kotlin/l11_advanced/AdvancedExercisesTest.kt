package l11_advanced

import l11_advanced.exercises.AdvancedExercises
import org.junit.jupiter.api.Tag

/**
 * Checks YOUR answers in exercises/AdvancedExercises.kt.
 *
 * Run: ./gradlew exercises --tests "l11_*"   (or click ▶ next to the class)
 * Every test fails with "NotImplementedError" until you replace its TODO().
 */
@Tag("exercise")
class AdvancedExercisesTest : AdvancedTasksTest(AdvancedExercises)
