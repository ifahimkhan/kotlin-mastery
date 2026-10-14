# Capstone 12.A — Library manager

> Build a complete command-line program: a catalogue of books, members,
> loans with due dates, fines, a save file, and a prompt that reads
> commands.

## What you build

A text CLI that a librarian can use:

```
> add-book 978-1 | Kotlin in Action | Jemerov | 2017 | 2
added Kotlin in Action
> add-member m1 | Ada
welcome Ada
> borrow 978-1 m1
borrowed 978-1, due 2026-10-28
> list
978-1  Kotlin in Action by Jemerov (2017)  1/2 available
> quit
bye
```

The rules live in **pure functions** (state in, new state out), described in
[LibraryRules.kt](LibraryRules.kt). You implement them in
[starter/LibraryStarter.kt](starter/LibraryStarter.kt). Everything else is
already written and works with any `LibraryRules`:
- the model ([Model.kt](Model.kt), [Commands.kt](Commands.kt))
- the app layer ([LibraryApp.kt](LibraryApp.kt))
- the save file ([LibraryFile.kt](LibraryFile.kt))
- the console loop ([Main.kt](Main.kt))

## Rules

| Rule | Value |
|------|-------|
| Loan length (and one renewal) | 14 days |
| Books per member | 3 |
| Fine | 25 cents per late day, at most $10.00 per loan |
| Renewals | once per loan, never when already late |
| Borrowing with an overdue loan | refused |

Invalid **arguments** (blank isbn, copies < 1, duplicate member id) throw
via `require`. **Business refusals** (no copy left, limit reached...) are
`LoanResult` values. That's the same rule as level 05.

## Milestones

| # | Build | Uses | Check |
|---|-------|------|-------|
| 1 | `addBook`, `addMember`, `availableCopies`, `search` | l05 data classes + `copy`, l06 `filter`/`map`/`count` | `--tests "*LibraryStarterTest.m1*"` |
| 2 | `borrow`, `returnBook`, `renew` | l04 `?:` early returns, l05 sealed results | `m2*` |
| 3 | `fineFor`, `overdue` | `java.time.ChronoUnit`, l06 `sortedByDescending` | `m3*` |
| 4 | `encode`, `decode` | kotlinx.serialization `Json`, catching errors into a result | `m4*` |
| 5 | `parseCommand` | l02 `when`, l01 string functions, l06 destructuring | `m5*` |

Run a milestone's tests with:

```bash
./gradlew exercises --tests "*LibraryStarterTest.m1*"
./gradlew exercises --tests "l12_capstones.library*"     # everything
```

When they're all green, open [Main.kt](Main.kt), switch `LibraryReference`
to `LibraryStarter`, and drive **your** library from the keyboard:

```bash
./gradlew -q run -Plesson=l12_capstones.library.MainKt
```

## Hints

- `LocalDate` has no built-in serializer. [Model.kt](Model.kt) shows how
  `@Serializable(with = ...)` plugs one in, so you don't have to write it.
- Read the check order for `borrow` in the KDoc carefully. The tests check it.
- A member can borrow two copies of the same book on the same day. Those
  two `Loan`s are equal (`==`), so `renew` must replace one loan **by
  position** (`indexOf` + `mapIndexed`), not with `map { if (it == loan) ... }`.
- `decode` has to reject JSON that is well-formed but impossible: unknown
  books in loans, duplicate ids, more loans than copies, and so on.
  Parsing successfully doesn't make the data valid.
- `parseCommand` should split on any whitespace (`Regex("""\s+""")`), so tabs
  work too.
- The app saves **before** it updates its state ([LibraryApp.kt](LibraryApp.kt)).
  If the disk is full, the user sees "could not save" and nothing changes.
- Want to see it done? [reference/](reference/) has one answer. Try first!

## Stretch goals

- `history <memberId>`: every loan a member ever had (needs a new list in the state).
- Reservations: a member can reserve a book that is out, and is first in line when it's returned.
- An `undo` command: keep the previous states in a list. This is easy because states are immutable.
