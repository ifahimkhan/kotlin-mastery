package l12_capstones.library

import l12_capstones.library.starter.LibraryStarter
import org.junit.jupiter.api.Tag

/**
 * Checks YOUR library in starter/LibraryStarter.kt.
 *
 * Run: ./gradlew exercises --tests "l12_capstones.library*"
 * One milestone only: ./gradlew exercises --tests "*LibraryStarterTest.m1*"
 */
@Tag("exercise")
class LibraryStarterTest : LibraryRulesTest(LibraryStarter)
