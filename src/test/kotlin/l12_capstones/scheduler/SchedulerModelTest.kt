package l12_capstones.scheduler

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The shared model (not your scheduler) rejects nonsense up front. */
class SchedulerModelTest {
    @Test
    fun `invalid specs and configs are rejected`() {
        assertFailsWith<IllegalArgumentException> { TaskSpec("x", maxAttempts = 0) {} }
        assertFailsWith<IllegalArgumentException> { TaskSpec(" ") {} }
        assertFailsWith<IllegalArgumentException> { Repeat.Every(periodMillis = 0, times = 1) }
        assertFailsWith<IllegalArgumentException> { SchedulerConfig(maxConcurrent = 0) }
    }

    @Test
    fun `only Succeeded, Failed and Cancelled are final`() {
        assertTrue(TaskStatus.Succeeded(1).isFinal)
        assertTrue(TaskStatus.Failed("x", 1).isFinal)
        assertTrue(TaskStatus.Cancelled.isFinal)
        assertFalse(TaskStatus.Pending.isFinal)
        assertFalse(TaskStatus.Running(1).isFinal)
    }
}
