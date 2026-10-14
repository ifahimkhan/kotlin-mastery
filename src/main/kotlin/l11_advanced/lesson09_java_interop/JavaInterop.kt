package l11_advanced.lesson09_java_interop

import l11_advanced.lesson09_java_interop.api.Thermostat
import l11_advanced.lesson09_java_interop.api.describe
import l11_advanced.lesson09_java_interop.api.findRoom
import l11_advanced.lesson09_java_interop.api.toFahrenheit

/**
 * =====================================================================
 *  LESSON 11.9 — Calling Kotlin from Java
 * =====================================================================
 *  GOAL   : Make a Kotlin API pleasant for Java callers with
 *           @file:JvmName, @JvmName, @JvmOverloads, @JvmStatic,
 *           @JvmField and @Throws, and understand nullability at the
 *           boundary. Read api/Thermostats.kt (Kotlin) and
 *           src/main/java/l11_advanced/lesson09_java_interop/JavaClient.java.
 *  RUN    : ./gradlew -q run -Plesson=l11_advanced.lesson09_java_interop.JavaInteropKt
 *  OUTPUT :
 *           Kotlin: toFahrenheit(100.0) = 212.0, describe("lab") = room lab
 *           Kotlin: Thermostat("hall") = Thermostat(room=hall, target=21, eco=false)
 *           Java: Thermostats.toFahrenheit(100) = 212.0
 *           Java: Thermostats.describeRoom("lab") = room lab
 *           Java: new Thermostat("hall") = Thermostat(room=hall, target=21, eco=false)
 *           Java: new Thermostat("hall", 19) = Thermostat(room=hall, target=19, eco=false)
 *           Java: Thermostat.forOffice() = Thermostat(room=office, target=21, eco=true)
 *           Java: Thermostat.MAX_TARGET = 30
 *           Java: findRoom(3) = null, Java had to check
 *           Java: caught IOException: no profile called missing
 *           Kotlin sees findRoom(3) as String?: no room
 *  NEXT   : lesson10_contracts_and_more/ContractsAndMore.kt
 * =====================================================================
 */
fun main() {
    // ── 1. CONCEPT: Kotlin callers don't see the annotations ─────────
    // Inside Kotlin, nothing changes: Kotlin names, default arguments.
    println("Kotlin: toFahrenheit(100.0) = ${toFahrenheit(100.0)}, describe(\"lab\") = ${describe("lab")}")
    println("Kotlin: Thermostat(\"hall\") = ${Thermostat("hall")}")

    // ── 2. CONCEPT: what Java sees ───────────────────────────────────
    // JavaClient.run() is ordinary Java code calling the same API.
    JavaClient.run().forEach(::println)

    // ── 3. WHY each annotation ───────────────────────────────────────
    // Without them, Java would have to write:
    //   ThermostatsKt.toFahrenheit(100.0)       (no @file:JvmName)
    //   Thermostat.Companion.forOffice()        (no @JvmStatic)
    //   Thermostat.Companion.getMAX_TARGET()    (no @JvmField)
    //   new Thermostat("hall", 21, false)       (no @JvmOverloads: all args)
    // and could not `catch (IOException e)` around loadProfile at all:
    // javac rejects catching a checked exception the method doesn't declare.

    // ── 4. CONCEPT: nullability at the boundary ──────────────────────
    // Kotlin's String? becomes a Java String plus a @Nullable annotation.
    // Java ignores it unless a tool checks, so Java code must test for
    // null itself. The other direction (Java -> Kotlin) gives platform
    // types: l04_null_safety/lesson06_platform.
    println("Kotlin sees findRoom(3) as String?: ${findRoom(3) ?: "no room"}")

    // ── 5. TRY IT ────────────────────────────────────────────────────
    // a) Remove @JvmStatic from forOffice(). Fix JavaClient.java so it
    //    compiles again (hint: Thermostat.Companion).
    // b) Remove @Throws. Read javac's error about the catch block.
    // c) Add a Kotlin `object` with a @JvmStatic function and call it from Java.
}
