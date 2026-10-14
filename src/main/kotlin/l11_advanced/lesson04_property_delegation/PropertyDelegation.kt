package l11_advanced.lesson04_property_delegation

import kotlin.properties.Delegates
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * =====================================================================
 *  LESSON 11.4 — Property delegation: lazy, observable, your own
 * =====================================================================
 *  GOAL   : Hand a property's get/set logic to a reusable object with
 *           `by`: lazy values, observable and vetoable changes, values
 *           stored in a map, and delegates you write yourself.
 *  RUN    : ./gradlew -q run -Plesson=l11_advanced.lesson04_property_delegation.PropertyDelegationKt
 *  OUTPUT :
 *           before first access
 *           computing config...
 *           config = port=8080
 *           config again = port=8080 (computed once)
 *           theme: light -> dark
 *           theme: dark -> blue
 *           volume after setting 7: 7
 *           volume after setting 42 (vetoed): 7
 *           user from map: Ada, 36
 *           trimmed: [Grace Hopper]
 *           read 'title' -> Draft
 *           title = Draft
 *           newName = Kim, oldName = Kim
 *           registered property: city
 *  NEXT   : lesson05_operator_overloading/OperatorOverloading.kt
 * =====================================================================
 */

private const val DEFAULT_PORT = 8080
private const val MAX_VOLUME = 10
private const val NORMAL_VOLUME = 7
private const val LOUD_VOLUME = 42

class Settings {
    // ── 1. CONCEPT: lazy ─────────────────────────────────────────────
    // Computed on first access, then cached. By default lazy is
    // thread-safe (SYNCHRONIZED): even if many threads read it at once,
    // the block runs once. LazyThreadSafetyMode.NONE skips the locking
    // when you KNOW only one thread uses it; PUBLICATION lets several
    // threads compute it but keeps the first finished value.
    // If the block THROWS, nothing is cached: the next read tries again.
    val config: String by lazy {
        println("computing config...")
        "port=$DEFAULT_PORT"
    }

    // ── 2. CONCEPT: observable and vetoable ──────────────────────────
    // observable: run code AFTER every change.
    var theme: String by Delegates.observable("light") { _, old, new ->
        println("theme: $old -> $new")
    }

    // vetoable: run code BEFORE a change; return false to reject it.
    var volume: Int by Delegates.vetoable(0) { _, _, new -> new in 0..MAX_VOLUME }
}

// ── 3. CONCEPT: properties stored in a map ──────────────────────────
// Handy for JSON-like data: the property name is the map key.
// A `val` needs only a Map; a `var` needs a MutableMap (writes go into it).
class User(
    map: Map<String, Any?>,
) {
    val name: String by map
    val age: Int by map
}

// ── 4. CONCEPT: your own delegate ───────────────────────────────────
// A delegate is any object with getValue (and setValue for var).
// ReadWriteProperty is the interface that spells those out for you.
class Trimmed : ReadWriteProperty<Any?, String> {
    private var value = ""

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): String = value

    override fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: String,
    ) {
        this.value = value.trim()
    }
}

// KProperty tells the delegate WHICH property it serves (its name).
fun logged(initial: String): ReadOnlyProperty<Any?, String> =
    ReadOnlyProperty { _, property ->
        println("read '${property.name}' -> $initial")
        initial
    }

class Profile {
    var fullName: String by Trimmed()
    val title: String by logged("Draft")

    // ── 5. CONCEPT: delegate to another property ─────────────────────
    // Useful when renaming: the old name keeps working.
    var newName: String = "Kim"

    @Deprecated("Use newName", ReplaceWith("newName"))
    var oldName: String by this::newName
}

// ── 6. CONCEPT (brief): provideDelegate ─────────────────────────────
// `operator fun provideDelegate(thisRef, property)` runs once per
// instance, when the owning object is constructed (before any read).
// PropertyDelegateProvider is the ready-made functional interface for it.
// Good for registering or validating properties by name.
class Registry {
    val names = mutableListOf<String>()

    fun register(): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, String>> =
        PropertyDelegateProvider { _, property ->
            names += property.name
            ReadOnlyProperty { _, _ -> "value of ${property.name}" }
        }
}

class Address(
    registry: Registry,
) {
    val city: String by registry.register()
}

fun main() {
    val settings = Settings()
    println("before first access")
    println("config = ${settings.config}")
    println("config again = ${settings.config} (computed once)")

    settings.theme = "dark"
    settings.theme = "blue"

    settings.volume = NORMAL_VOLUME
    println("volume after setting $NORMAL_VOLUME: ${settings.volume}")
    settings.volume = LOUD_VOLUME
    println("volume after setting $LOUD_VOLUME (vetoed): ${settings.volume}")

    val user = User(mapOf("name" to "Ada", "age" to 36))
    println("user from map: ${user.name}, ${user.age}")
    // PITFALL: a missing key throws NoSuchElementException on read, and a
    // wrong value type throws ClassCastException. The map is not checked
    // when the object is created.

    val profile = Profile()
    profile.fullName = "   Grace Hopper  "
    println("trimmed: [${profile.fullName}]")
    println("title = ${profile.title}") // the read itself prints the log line first
    @Suppress("DEPRECATION") // we call the old name on purpose, to show it still works
    println("newName = ${profile.newName}, oldName = ${profile.oldName}")

    val registry = Registry()
    Address(registry)
    println("registered property: ${registry.names.joinToString()}")

    // ── 7. TRY IT ────────────────────────────────────────────────────
    // a) Write a delegate that counts how often a property is read.
    // b) Make volume observable AND clamp values (hint: write your own).
    // c) Remove "age" from the map in main. Which line crashes, and when?
}
