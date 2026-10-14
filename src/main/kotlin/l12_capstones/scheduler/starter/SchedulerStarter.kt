package l12_capstones.scheduler.starter

import kotlinx.coroutines.CoroutineScope
import l12_capstones.scheduler.SchedulerConfig
import l12_capstones.scheduler.SchedulerFactory
import l12_capstones.scheduler.TaskScheduler

/**
 * =====================================================================
 *  CAPSTONE 12.C — Task scheduler: YOUR implementation
 * =====================================================================
 *  HOW    : Read ../README.md. Write a class that implements
 *           TaskScheduler (../TaskScheduler.kt) - in this file or a new
 *           one in this package - and return it from create() below.
 *  CHECK  : ./gradlew exercises --tests "l12_capstones.scheduler*"
 *  STUCK? : ../reference/ReferenceScheduler.kt has one finished answer.
 *           This is the hardest capstone: build it milestone by
 *           milestone, and read the reference's "How it works" comment
 *           only if you are truly stuck.
 * =====================================================================
 */
object SchedulerStarter : SchedulerFactory {
    override fun create(scope: CoroutineScope, config: SchedulerConfig): TaskScheduler =
        TODO("Return your TaskScheduler implementation, e.g. MyScheduler(scope, config)")
}
