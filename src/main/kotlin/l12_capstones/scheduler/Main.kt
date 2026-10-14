package l12_capstones.scheduler

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import l12_capstones.scheduler.reference.SchedulerReference

/**
 * =====================================================================
 *  CAPSTONE 12.C — Task scheduler (demo)
 * =====================================================================
 *  GOAL   : Run tasks with priorities, delays, repetitions and retries
 *           on a small pool of workers; cancel one; shut down cleanly.
 *  RUN    : ./gradlew -q run -Plesson=l12_capstones.scheduler.MainKt
 *  NOTE   : Only the FINAL statuses are printed: the order in which
 *           tasks run in between depends on timing. Switch the factory
 *           to starter.SchedulerStarter once your tests pass.
 *  NEXT   : ../README.md (where to go from here)
 * =====================================================================
 */

private const val SHORT_WORK_MILLIS = 50L
private const val LONG_WORK_MILLIS = 5_000L
private const val REPORT_PERIOD_MILLIS = 100L
private const val CANCEL_AFTER_MILLIS = 120L

fun main() =
    runBlocking {
        val scheduler = SchedulerReference.create(this, SchedulerConfig(maxConcurrent = 2))
        val names = mutableMapOf<TaskId, String>()
        fun add(spec: TaskSpec): TaskId = scheduler.schedule(spec).also { names[it] = spec.name }
        var flakyCalls = 0

        // The workers are children of this runBlocking scope. If anything
        // below throws before shutdown, the finally block still stops them;
        // without it, runBlocking would wait for the workers forever.
        try {
            add(TaskSpec("backup", priority = 1, initialDelayMillis = 100) { delay(SHORT_WORK_MILLIS) })
            add(TaskSpec("report", priority = 5, repeat = Repeat.Every(REPORT_PERIOD_MILLIS, times = 3)) {})
            add(
                TaskSpec("flaky-sync", maxAttempts = 3, retryBackoffMillis = 50) {
                    flakyCalls++
                    check(flakyCalls > 1) { "network hiccup" }
                },
            )
            add(TaskSpec("doomed", maxAttempts = 2, retryBackoffMillis = 50) { error("disk full") })
            val slow = add(TaskSpec("slow-export") { delay(LONG_WORK_MILLIS) })

            delay(CANCEL_AFTER_MILLIS)
            println("cancel slow-export: ${scheduler.cancel(slow)}")

            scheduler.shutdown(ShutdownMode.DRAIN)
        } finally {
            // NonCancellable: clean up even if we got here by cancellation.
            // After DRAIN everything is final, so this returns at once.
            withContext(NonCancellable) { scheduler.shutdown(ShutdownMode.CANCEL_ALL) }
        }

        println("Final statuses:")
        scheduler.statuses.value.toSortedMap(compareBy { it.value }).forEach { (id, status) ->
            println("  #${id.value} ${names[id]?.padEnd(12)} $status")
        }
    }
