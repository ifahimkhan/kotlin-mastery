/**
 * Welcome screen shown by `./gradlew -q run` when no lesson is chosen.
 *
 * Every lesson in this repository is a small, self-contained program with its
 * own `main` function. Open any lesson file and click the green ▶ next to
 * `fun main`, or run it from the terminal (see below).
 */
fun main() {
    println(
        """
        |==============================================================
        |  Kotlin Mastery - learn Kotlin from zero to coroutines
        |==============================================================
        |
        |  1. Read the ROADMAP.md file and start at level 01.
        |  2. Open a lesson, read it top to bottom, then run it.
        |  3. Solve the level's exercises and check them:
        |
        |       ./gradlew exercises --tests "l01_*"
        |
        |  Run a lesson from the terminal:
        |
        |       ./gradlew -q run -Plesson=l01_basics.lesson01_hello.HelloWorldKt
        |
        |  (Class name = package + file name + "Kt")
        |==============================================================
        """.trimMargin()
    )
}
