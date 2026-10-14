// currentTime (virtual time) is still marked experimental in kotlinx-coroutines-test.
@file:OptIn(ExperimentalCoroutinesApi::class)

package l09_coroutines.practice

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import l09_coroutines.practice.downloader.DownloadResult
import l09_coroutines.practice.downloader.FakeFileSource
import l09_coroutines.practice.downloader.RemoteFile
import l09_coroutines.practice.downloader.downloadAll
import l09_coroutines.practice.downloader.formatResult
import l09_coroutines.practice.downloader.summarize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DownloaderTest {
    private val fourFiles = (1..4).associate { "f$it" to RemoteFile(sizeBytes = it * 10, delayMillis = 100) }

    @Test
    fun `never runs more than maxConcurrent downloads`() =
        runTest {
            val source = FakeFileSource(fourFiles)
            downloadAll(source, fourFiles.keys.toList(), maxConcurrent = 2)
            assertEquals(2, source.peakConcurrency)
            assertEquals(200, currentTime, "4 files of 100 ms, 2 at a time -> 2 rounds")
        }

    @Test
    fun `a high limit downloads everything at once`() =
        runTest {
            val source = FakeFileSource(fourFiles)
            downloadAll(source, fourFiles.keys.toList(), maxConcurrent = 10)
            assertEquals(4, source.peakConcurrency)
            assertEquals(100, currentTime)
        }

    @Test
    fun `failures are results and keep the input order`() =
        runTest {
            val source =
                FakeFileSource(
                    mapOf(
                        "slow" to RemoteFile(sizeBytes = 5, delayMillis = 300),
                        "bad" to RemoteFile(sizeBytes = 1, delayMillis = 100, failure = "server error"),
                    ),
                )
            val results = downloadAll(source, listOf("slow", "bad", "nope"), maxConcurrent = 3)
            assertEquals(
                listOf(
                    DownloadResult.Success("slow", 5),
                    DownloadResult.Failure("bad", "server error"),
                    DownloadResult.Failure("nope", "not found"),
                ),
                results,
            )
        }

    @Test
    fun `progress is reported once per file in finishing order`() =
        runTest {
            val source =
                FakeFileSource(
                    mapOf(
                        "a" to RemoteFile(sizeBytes = 1, delayMillis = 200),
                        "b" to RemoteFile(sizeBytes = 2, delayMillis = 100),
                    ),
                )
            val progress = mutableListOf<String>()
            downloadAll(source, listOf("a", "b"), maxConcurrent = 2) { done, total, result ->
                progress += "$done/$total ${result.name}"
            }
            assertEquals(listOf("1/2 b", "2/2 a"), progress)
        }

    @Test
    fun `invalid limit and formatting`() =
        runTest {
            assertFailsWith<IllegalArgumentException> { downloadAll(FakeFileSource(emptyMap()), listOf("x"), 0) }
            assertEquals("OK   a (3 bytes)", formatResult(DownloadResult.Success("a", 3)))
            assertEquals("FAIL b (oops)", formatResult(DownloadResult.Failure("b", "oops")))
            val results = listOf(DownloadResult.Success("a", 3), DownloadResult.Failure("b", "x"))
            assertEquals("Downloaded 1 of 2 files (3 bytes)", summarize(results))
            assertEquals("Downloaded 0 of 0 files (0 bytes)", summarize(emptyList()))
        }
}
