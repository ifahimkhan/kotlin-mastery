package l04_null_safety.lesson04_let_smart_cast

import kotlin.math.roundToLong

/**
 * =====================================================================
 *  LESSON 04.4 — ?.let and smart casts
 * =====================================================================
 *  GOAL   : Run code only when a value is present (?.let), and let the
 *           compiler "smart cast" a value after you checked it.
 *  RUN    : ./gradlew -q run -Plesson=l04_null_safety.lesson04_let_smart_cast.LetAndSmartCastsKt
 *  OUTPUT :
 *           Sending email to ada@example.com
 *           (no email for user 2, nothing sent)
 *           letter count = 4
 *           after the null check: KIWI has 4 letters
 *           describe(42)      -> Int, doubled = 84
 *           describe("hey")   -> String of length 3
 *           describe(2.5)     -> Double, rounded = 3
 *           describe(null)    -> nothing at all
 *           describe(true)    -> something else
 *           profile bio length = 12
 *  NEXT   : lesson05_lateinit_lazy/LateinitAndLazy.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: ?.let { } ────────────────────────────────────────
    // The block runs ONLY if the value is not null. Inside, `it` is the
    // non-null value. If the value is null, the whole thing is skipped.
    findEmail(1)?.let { sendEmail(it) }

    // "Do this, otherwise do that"? Prefer a plain if/else: clear, no surprises.
    val secondEmail = findEmail(2)
    if (secondEmail != null) sendEmail(secondEmail) else println("(no email for user 2, nothing sent)")
    // You'll also see `x?.let { ... } ?: otherAction()` in real code.
    // Careful: the `?:` part ALSO runs when the let block itself returns null.

    // let also returns the block's last expression, so it can transform:
    val letterCount: Int = findFruit()?.let { fruit -> fruit.length } ?: 0
    println("letter count = $letterCount")

    // ── 2. CONCEPT: smart cast after a null check ────────────────────
    // After `if (x != null)`, the compiler KNOWS x is not null inside
    // the block, and treats String? as String automatically.
    val fruit: String? = findFruit()
    if (fruit != null) {
        println("after the null check: ${fruit.uppercase()} has ${fruit.length} letters")
    }
    // Same with an early exit:  if (fruit == null) return

    // ── 3. CONCEPT: smart cast with `is` ─────────────────────────────
    // `is` checks the type; after it, the value is treated as that type.
    println("describe(42)      -> ${describe(42)}")
    println("describe(\"hey\")   -> ${describe("hey")}")
    println("describe(2.5)     -> ${describe(2.5)}")
    println("describe(null)    -> ${describe(null)}")
    println("describe(true)    -> ${describe(true)}")

    // ── 4. PITFALL: smart casts don't work on mutable properties ─────
    // (Profile is a tiny class with one property. Classes are level 05.)
    val profile = Profile()
    profile.bio = "Loves Kotlin"
    // if (profile.bio != null) println(profile.bio.length)
    //   ❌ Smart cast to 'String' is impossible, because 'bio' is a
    //      mutable property that could be mutated concurrently.
    // Another thread (or a custom getter) could set bio to null between
    // the check and the use. Fix: copy it into a local val first.
    val bio = profile.bio
    if (bio != null) {
        println("profile bio length = ${bio.length}") // smart cast works on the local val
    }

    // ── 5. WHY ───────────────────────────────────────────────────────
    // Smart casts remove the casting noise Java needs:
    //   if (obj instanceof String) { String s = (String) obj; ... }
    // ?.let keeps "do X only if present" in a single readable line.

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Use ?.let to print "Fruit: <name>" only when findFruit() has one.
    // b) Add an `is Boolean ->` branch to describe() and run again.
    // c) Uncomment the pitfall line in section 4 and read the error.
}

// `when` with `is` branches is covered in level 02; here it shows smart casts.
private fun describe(value: Any?): String =
    when (value) {
        null -> "nothing at all"
        is Int -> "Int, doubled = ${value * 2}" // value is an Int here
        is String -> "String of length ${value.length}" // value is a String here
        is Double -> "Double, rounded = ${value.roundToLong()}"
        else -> "something else"
    }

private class Profile {
    var bio: String? = null
}

private fun findEmail(userId: Int): String? = if (userId == 1) "ada@example.com" else null

private fun findFruit(): String? = "kiwi"

private fun sendEmail(address: String) {
    println("Sending email to $address")
}
