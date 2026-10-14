package l12_capstones.library.reference

import l12_capstones.library.Book
import l12_capstones.library.Command
import l12_capstones.library.Member

private const val BOOK_FIELDS = 5
private const val MEMBER_FIELDS = 2

/**
 * Turns one typed line into a Command. Never throws: every problem
 * becomes Command.Invalid with a message the user can act on.
 *
 * Split in two steps: the first word picks the command, the rest is
 * parsed by a small function per command. `when` on a String is the
 * l02_control_flow tool for this.
 */
internal fun parseLibraryCommand(line: String): Command {
    val trimmed = line.trim()
    if (trimmed.isEmpty()) return Command.Invalid("empty line, type 'help'")
    // Split on any run of whitespace (spaces AND tabs): "borrow<TAB>1 m1" works too.
    val parts = trimmed.split(WHITESPACE, limit = 2)
    val word = parts[0].lowercase()
    val rest = parts.getOrElse(1) { "" }.trim()
    return when (word) {
        "add-book" -> parseAddBook(rest)
        "add-member" -> parseAddMember(rest)
        "borrow" -> parseIsbnAndMember(rest, word) { isbn, member -> Command.Borrow(isbn, member) }
        "return" -> parseIsbnAndMember(rest, word) { isbn, member -> Command.Return(isbn, member) }
        "renew" -> parseIsbnAndMember(rest, word) { isbn, member -> Command.Renew(isbn, member) }
        "search" -> Command.Search(rest)
        "list" -> noArguments(rest, word, Command.ListBooks)
        "overdue" -> noArguments(rest, word, Command.Overdue)
        "help" -> noArguments(rest, word, Command.Help)
        "quit", "exit" -> noArguments(rest, word, Command.Quit)
        else -> Command.Invalid("unknown command '$word', type 'help'")
    }
}

private val WHITESPACE = Regex("""\s+""")

// "quit now" is probably a typo or a misunderstanding: say so instead of guessing.
private fun noArguments(rest: String, word: String, command: Command): Command =
    if (rest.isEmpty()) command else Command.Invalid("'$word' takes no arguments")

private fun parseAddBook(rest: String): Command {
    val fields = rest.split('|').map { it.trim() }
    if (fields.size != BOOK_FIELDS || fields.any { it.isEmpty() }) {
        return Command.Invalid("usage: add-book <isbn> | <title> | <author> | <year> | <copies>")
    }
    val (isbn, title, author, yearText, copiesText) = fields
    val year = yearText.toIntOrNull()?.takeIf { it > 0 }
        ?: return Command.Invalid("year must be a positive number, was '$yearText'")
    val copies = copiesText.toIntOrNull()?.takeIf { it >= 1 }
        ?: return Command.Invalid("copies must be a whole number >= 1, was '$copiesText'")
    return Command.AddBook(Book(isbn, title, author, year, copies))
}

private fun parseAddMember(rest: String): Command {
    val fields = rest.split('|').map { it.trim() }
    if (fields.size != MEMBER_FIELDS || fields.any { it.isEmpty() }) {
        return Command.Invalid("usage: add-member <id> | <name>")
    }
    return Command.AddMember(Member(id = fields[0], name = fields[1]))
}

// The three loan commands share one shape, so the constructor is passed
// in as a function (l07_functional higher-order functions).
private fun parseIsbnAndMember(
    rest: String,
    word: String,
    build: (isbn: String, memberId: String) -> Command,
): Command {
    val parts = rest.split(WHITESPACE).filter { it.isNotBlank() }
    if (parts.size != 2) return Command.Invalid("usage: $word <isbn> <memberId>")
    return build(parts[0], parts[1])
}
