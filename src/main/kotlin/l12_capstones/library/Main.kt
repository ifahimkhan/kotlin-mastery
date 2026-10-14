package l12_capstones.library

import l12_capstones.library.reference.LibraryReference
import java.nio.file.Path
import java.time.LocalDate
import kotlin.system.exitProcess

/**
 * =====================================================================
 *  CAPSTONE 12.A — Library manager (interactive CLI)
 * =====================================================================
 *  GOAL   : A small but complete program: a catalogue, members, loans,
 *           fines, a save file and a command line.
 *  RUN    : ./gradlew -q run -Plesson=l12_capstones.library.MainKt
 *           (optional: --args="my-library.json" to pick the save file)
 *  TRY    : add-book 978-1 | Kotlin in Action | Jemerov | 2017 | 2
 *           add-member m1 | Ada
 *           borrow 978-1 m1
 *           list
 *           quit
 *  NOTE   : Main uses reference/LibraryReference. Switch it to
 *           starter.LibraryStarter once your tests pass to drive YOUR
 *           implementation from the keyboard.
 *  NEXT   : ../weather/README.md
 * =====================================================================
 */

private const val DEFAULT_FILE = "library.json"

fun main(args: Array<String>) {
    val rules: LibraryRules = LibraryReference
    val file = LibraryFile(Path.of(args.firstOrNull() ?: DEFAULT_FILE), rules)

    val initial =
        when (val loaded = file.load()) {
            is LoadResult.Loaded -> loaded.state
            is LoadResult.Corrupt -> {
                // Refuse to overwrite a file we could not read: the user's data matters.
                // Exit code 1 tells scripts that called us that something went wrong.
                println("Cannot start: ${loaded.message}")
                exitProcess(1)
            }
        }

    val app = LibraryApp(rules, today = { LocalDate.now() }, initial = initial, onChange = file::save)
    println("Library: ${initial.books.size} books, ${initial.members.size} members. Type 'help'.")

    while (true) {
        print("> ")
        // readlnOrNull() is null at end of input (Ctrl+D / piped file): stop cleanly.
        val line = readlnOrNull() ?: break
        val command = rules.parseCommand(line)
        app.handle(command).forEach { println(it) }
        if (command == Command.Quit) break
    }
}
