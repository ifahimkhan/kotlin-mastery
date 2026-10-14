package l12_capstones.library

import java.io.IOException
import java.nio.file.AccessDeniedException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Saving and loading the library file. This is the only part of the
 * project that touches the disk, so it is kept tiny and separate from
 * the pure rules: the rules decide WHAT the file contains (encode/decode),
 * this class decides WHERE it lives.
 */
class LibraryFile(
    private val path: Path,
    private val rules: LibraryRules,
) {
    /** A missing file is a fresh, empty library - not an error. */
    fun load(): LoadResult {
        if (!Files.exists(path)) return LoadResult.Loaded(LibraryState())
        return try {
            rules.decode(Files.readString(path))
        } catch (e: IOException) {
            LoadResult.Corrupt("cannot read ${path.fileName}: ${e.message}")
        }
    }

    /**
     * Writes to a temporary file first, then moves it over the real one.
     * If the program dies half-way through WRITING, the old file is still
     * intact - you never end up with half a library.
     *
     * The move itself is atomic where the file system supports it
     * (ATOMIC_MOVE); otherwise it falls back to a plain replace, which is
     * very fast but not guaranteed all-or-nothing.
     *
     * Throws IOException when saving is impossible (disk full, no
     * permission, the path is a folder...). The temporary file is always
     * cleaned up.
     */
    fun save(state: LibraryState) {
        val parent = path.toAbsolutePath().parent
        Files.createDirectories(parent)
        val temp = Files.createTempFile(parent, "library", ".tmp")
        try {
            Files.writeString(temp, rules.encode(state))
            moveWithRetry(temp)
        } finally {
            // After a successful move the temp file is gone; this only
            // removes it when something went wrong.
            Files.deleteIfExists(temp)
        }
    }

    /**
     * On Windows a file that was just written can be locked for a moment
     * (antivirus scanning, search indexing), and replacing it then fails
     * with AccessDeniedException. A few short retries hide that blip; a
     * real permission problem still fails after the last attempt.
     */
    private fun moveWithRetry(temp: Path) {
        repeat(MOVE_ATTEMPTS - 1) {
            try {
                moveOnce(temp)
                return
            } catch (e: AccessDeniedException) {
                Thread.sleep(MOVE_RETRY_DELAY_MILLIS)
            }
        }
        moveOnce(temp)
    }

    private fun moveOnce(temp: Path) {
        try {
            Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE)
        } catch (e: AtomicMoveNotSupportedException) {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private companion object {
        const val MOVE_ATTEMPTS = 5
        const val MOVE_RETRY_DELAY_MILLIS = 50L
    }
}
