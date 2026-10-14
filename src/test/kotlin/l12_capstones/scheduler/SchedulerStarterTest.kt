package l12_capstones.scheduler

import l12_capstones.scheduler.starter.SchedulerStarter
import org.junit.jupiter.api.Tag

/**
 * Checks YOUR scheduler, created by starter/SchedulerStarter.
 *
 * Run: ./gradlew exercises --tests "l12_capstones.scheduler*"
 * One milestone only: ./gradlew exercises --tests "*SchedulerStarterTest.m1*"
 */
@Tag("exercise")
class SchedulerStarterTest : SchedulerTest(SchedulerStarter)
