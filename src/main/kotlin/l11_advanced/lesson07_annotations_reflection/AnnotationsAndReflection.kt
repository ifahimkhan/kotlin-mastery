package l11_advanced.lesson07_annotations_reflection

/**
 * =====================================================================
 *  LESSON 11.7 — Annotations and (light) reflection
 * =====================================================================
 *  GOAL   : Declare your own annotations, choose where they may go and
 *           whether they survive to runtime, and read them back. Use
 *           ::class, property references and function references as
 *           values, and know what needs the kotlin-reflect library.
 *  RUN    : ./gradlew -q run -Plesson=l11_advanced.lesson07_annotations_reflection.AnnotationsAndReflectionKt
 *  OUTPUT :
 *           User has @Table: users
 *           Order has @Table: none
 *           validate(User(name=, age=-1)) -> [age: must be >= 0, name: must not be blank]
 *           validate(User(name=Ada, age=36)) -> []
 *           class name: User, qualified: l11_advanced.lesson07_annotations_reflection.User
 *           property reference: name -> Ada
 *           names via ::name: [Ada, Linus]
 *           function reference: ::isAdult(36) = true
 *  NEXT   : lesson08_state_machines/StateMachines.kt
 * =====================================================================
 */

// ── 1. CONCEPT: declaring annotations ───────────────────────────────
// @Target: where it may be used. @Retention(RUNTIME): keep it in the
// compiled class so code can read it while the program runs. (SOURCE
// = gone after compiling; BINARY = in the class file, but invisible to
// reflection.) RUNTIME is the default for Kotlin annotations.
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Table(
    val name: String,
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class NotBlank

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Min(
    val value: Int,
)

// `@field:` puts the annotation on the Java field behind the property,
// where Java reflection (below) can find it.
@Table("users")
data class User(
    @field:NotBlank val name: String,
    @field:Min(0) val age: Int,
)

data class Order(
    val id: Int,
)

// ── 2. CONCEPT: reading annotations at runtime ──────────────────────
// This project does NOT depend on kotlin-reflect (a ~3 MB library), so
// we use Java reflection through `::class.java`. It is enough for
// annotations on classes, fields and methods.
fun tableName(type: Class<*>): String = type.getAnnotation(Table::class.java)?.name ?: "none"

// A tiny validator: looks at every field and checks its annotations.
// declaredFields has NO guaranteed order (the JVM spec doesn't promise
// one), so we sort by name to keep the messages stable.
fun validate(target: Any): List<String> =
    target.javaClass.declaredFields.sortedBy { it.name }.flatMap { field ->
        field.isAccessible = true // fields of a data class are private
        val value = field.get(target)
        listOfNotNull(
            if (field.isAnnotationPresent(NotBlank::class.java) && value is String && value.isBlank()) {
                "${field.name}: must not be blank"
            } else {
                null
            },
            field.getAnnotation(Min::class.java)?.let { min ->
                if (value is Int && value < min.value) "${field.name}: must be >= ${min.value}" else null
            },
        )
    }

private const val ADULT_AGE = 18

fun isAdult(age: Int): Boolean = age >= ADULT_AGE

fun main() {
    println("User has @Table: ${tableName(User::class.java)}")
    println("Order has @Table: ${tableName(Order::class.java)}")

    println("validate(User(name=, age=-1)) -> ${validate(User("", -1))}")
    println("validate(User(name=Ada, age=36)) -> ${validate(User("Ada", 36))}")

    // ── 3. CONCEPT: KClass, property and function references ─────────
    // These work WITHOUT kotlin-reflect:
    val ada = User("Ada", 36)
    val type = ada::class // a KClass<User>
    println("class name: ${type.simpleName}, qualified: ${type.qualifiedName}")
    val nameProperty = User::name // a KProperty1<User, String>
    println("property reference: ${nameProperty.name} -> ${nameProperty.get(ada)}")
    println("names via ::name: ${listOf(ada, User("Linus", 54)).map(User::name)}")
    val check: (Int) -> Boolean = ::isAdult // l07_functional/lesson04_references
    println("function reference: ::isAdult(36) = ${check(36)}")

    // ── 4. WHY: what needs kotlin-reflect ────────────────────────────
    // Listing members (`User::class.memberProperties`), reading Kotlin
    // annotations via `findAnnotation<T>()`, calling constructors by
    // reflection: all need the dependency
    //   implementation("org.jetbrains.kotlin:kotlin-reflect")
    // Without it they fail at runtime with KotlinReflectionNotSupportedError.

    // ── 5. PITFALL: reflection is slow and fragile ───────────────────
    // It skips compile-time checks: rename a field and the code above
    // still compiles but quietly stops validating it. Libraries such as
    // kotlinx.serialization (level 12) generate code at compile time
    // instead.

    // ── 6. TRY IT ────────────────────────────────────────────────────
    // a) Add @Max(value) and check it in validate().
    // b) Remove @Retention(RUNTIME) and use SOURCE. What does tableName print?
    // c) Print all field names of Order with Order::class.java.declaredFields.
}
