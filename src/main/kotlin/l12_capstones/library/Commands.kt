package l12_capstones.library

/**
 * Everything a user can type at the library prompt, already parsed.
 * The CLI loop only ever sees these values, never raw text, so the
 * parser can be tested on its own.
 *
 * Syntax (fields that may contain spaces are separated by "|"):
 *   add-book <isbn> | <title> | <author> | <year> | <copies>
 *   add-member <id> | <name>
 *   borrow <isbn> <memberId>
 *   return <isbn> <memberId>
 *   renew <isbn> <memberId>
 *   search <text>
 *   list
 *   overdue
 *   help
 *   quit
 */
sealed interface Command {
    data class AddBook(val book: Book) : Command

    data class AddMember(val member: Member) : Command

    data class Borrow(val isbn: String, val memberId: String) : Command

    data class Return(val isbn: String, val memberId: String) : Command

    data class Renew(val isbn: String, val memberId: String) : Command

    data class Search(val query: String) : Command

    data object ListBooks : Command

    data object Overdue : Command

    data object Help : Command

    data object Quit : Command

    /** The line could not be understood; [reason] tells the user why. */
    data class Invalid(val reason: String) : Command
}

/** Shown by the `help` command and the README. */
val COMMAND_HELP: List<String> =
    listOf(
        "add-book <isbn> | <title> | <author> | <year> | <copies>",
        "add-member <id> | <name>",
        "borrow <isbn> <memberId>",
        "return <isbn> <memberId>",
        "renew <isbn> <memberId>",
        "search <text>",
        "list",
        "overdue",
        "help",
        "quit",
    )
