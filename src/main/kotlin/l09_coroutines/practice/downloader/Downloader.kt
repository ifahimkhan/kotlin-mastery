package l09_coroutines.practice.downloader

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

/**
 * =====================================================================
 *  PRACTICE 09.A — A parallel file downloader (simulated)
 * =====================================================================
 *  GOAL   : Download many files at the same time, but never more than
 *           N at once; report progress; keep results in input order;
 *           let one failure NOT stop the others.
 *  RUN    : ./gradlew -q run -Plesson=l09_coroutines.practice.downloader.DownloaderKt
 *  OUTPUT :
 *           Downloading 5 files, at most 2 at a time...
 *           [1/5] OK   photo.jpg (4800 bytes)
 *           [2/5] OK   notes.txt (1200 bytes)
 *           [3/5] FAIL report.pdf (server error)
 *           [4/5] FAIL missing.txt (not found)
 *           [5/5] OK   song.mp3 (3000 bytes)
 *           Results in the order requested:
 *             OK   notes.txt (1200 bytes)
 *             OK   photo.jpg (4800 bytes)
 *             FAIL report.pdf (server error)
 *             OK   song.mp3 (3000 bytes)
 *             FAIL missing.txt (not found)
 *           Downloaded 3 of 5 files (9000 bytes)
 *           Most downloads running at once: 2
 *  NEW    : Semaphore(n) + withPermit { } lets at most n coroutines
 *           into a block at the same time; the others suspend (they do
 *           NOT block a thread) until a permit is free.
 *  DESIGN : The "network" is a fake with fixed delays, so the program is
 *           repeatable and testable with virtual time. A failed download
 *           is a normal outcome, so it is a DownloadResult.Failure value,
 *           not an exception (l05_oop rule: business refusal -> result).
 *           maxConcurrent < 1 is a caller bug -> require.
 *  EXTEND : Add a retry for failed files (see the Ex 5 retryWithBackoff),
 *           or a timeout per file (lesson10_timeouts).
 * =====================================================================
 */

private const val NOT_FOUND = "not found"
private const val MAX_CONCURRENT_DOWNLOADS = 2

/** A file on the fake server. [failure] != null means the server will refuse it. */
data class RemoteFile(
    val sizeBytes: Int,
    val delayMillis: Long,
    val failure: String? = null,
)

sealed interface DownloadResult {
    val name: String

    data class Success(
        override val name: String,
        val sizeBytes: Int,
    ) : DownloadResult

    data class Failure(
        override val name: String,
        val reason: String,
    ) : DownloadResult
}

/**
 * A pretend server. It also measures how many downloads run at the same
 * time, so tests can check the limit. AtomicInteger keeps the counters
 * correct even if fetch is called from several threads (lesson13_shared_state).
 */
class FakeFileSource(
    private val files: Map<String, RemoteFile>,
) {
    private val active = AtomicInteger(0)
    private val peak = AtomicInteger(0)

    val peakConcurrency: Int get() = peak.get()

    suspend fun fetch(name: String): DownloadResult {
        val file = files[name] ?: return DownloadResult.Failure(name, NOT_FOUND)
        val runningNow = active.incrementAndGet()
        peak.updateAndGet { max(it, runningNow) }
        try {
            delay(file.delayMillis) // the "network" time
        } finally {
            active.decrementAndGet() // also runs if the download is cancelled
        }
        return file.failure?.let { DownloadResult.Failure(name, it) }
            ?: DownloadResult.Success(name, file.sizeBytes)
    }
}

/**
 * Downloads all [names] with at most [maxConcurrent] at the same time.
 * [onProgress] is called after each download with (done, total, result),
 * in the order downloads FINISH. The returned list is in the order of [names].
 */
suspend fun downloadAll(
    source: FakeFileSource,
    names: List<String>,
    maxConcurrent: Int,
    onProgress: (done: Int, total: Int, result: DownloadResult) -> Unit = { _, _, _ -> },
): List<DownloadResult> {
    require(maxConcurrent >= 1) { "maxConcurrent must be at least 1, was $maxConcurrent" }
    val permits = Semaphore(maxConcurrent)
    val finished = AtomicInteger(0)
    return coroutineScope {
        names
            .map { name ->
                async {
                    val result = permits.withPermit { source.fetch(name) }
                    onProgress(finished.incrementAndGet(), names.size, result)
                    result
                }
            }.awaitAll() // same order as `names`
    }
}

fun formatResult(result: DownloadResult): String =
    when (result) {
        is DownloadResult.Success -> "OK   ${result.name} (${result.sizeBytes} bytes)"
        is DownloadResult.Failure -> "FAIL ${result.name} (${result.reason})"
    }

fun summarize(results: List<DownloadResult>): String {
    val successes = results.filterIsInstance<DownloadResult.Success>()
    return "Downloaded ${successes.size} of ${results.size} files (${successes.sumOf { it.sizeBytes }} bytes)"
}

// Delays are chosen so that, with 2 downloads at a time, finishing times
// are far apart: photo 300 ms, notes 750, report 900, song 1050 (missing
// fails at once when report frees its permit). Every gap is >= 150 ms, so
// the progress order is the same even on a slow machine.
private val SERVER =
    mapOf(
        "notes.txt" to RemoteFile(sizeBytes = 1_200, delayMillis = 750),
        "photo.jpg" to RemoteFile(sizeBytes = 4_800, delayMillis = 300),
        "report.pdf" to RemoteFile(sizeBytes = 9_000, delayMillis = 600, failure = "server error"),
        "song.mp3" to RemoteFile(sizeBytes = 3_000, delayMillis = 300),
    )

fun main() = runBlocking {
    val wanted = listOf("notes.txt", "photo.jpg", "report.pdf", "song.mp3", "missing.txt")
    val source = FakeFileSource(SERVER)
    println("Downloading ${wanted.size} files, at most $MAX_CONCURRENT_DOWNLOADS at a time...")
    val results =
        downloadAll(source, wanted, MAX_CONCURRENT_DOWNLOADS) { done, total, result ->
            println("[$done/$total] ${formatResult(result)}")
        }
    println("Results in the order requested:")
    results.forEach { println("  ${formatResult(it)}") }
    println(summarize(results))
    println("Most downloads running at once: ${source.peakConcurrency}")
}
