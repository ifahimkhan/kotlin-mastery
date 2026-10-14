import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

group = "io.github.kotlinmastery"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(platform(libs.junit.bom))
    testRuntimeOnly(libs.junit.platform.launcher)
}

// ---------------------------------------------------------------------------
// Run any lesson from the terminal:
//   ./gradlew -q run -Plesson=l01_basics.lesson01_hello.HelloWorldKt
// Without -Plesson, a short welcome screen is shown.
// ---------------------------------------------------------------------------
application {
    mainClass.set(providers.gradleProperty("lesson").orElse("StartKt"))
}

tasks.named<JavaExec>("run") {
    // Lets lessons that call readln() receive keyboard input.
    standardInput = System.`in`
}

// Any JDK 17+ can build this project; bytecode targets Java 17.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

// ---------------------------------------------------------------------------
// Testing
//
// Tests tagged "exercise" check the learner's answers. They fail until the
// learner replaces every TODO(), so the default `test` task skips them and
// verifies the reference solutions + practice projects instead (keeps CI green).
//
//   ./gradlew test                         -> solutions + practice projects
//   ./gradlew exercises                    -> every exercise test
//   ./gradlew exercises --tests "l01_*"    -> one level only
// ---------------------------------------------------------------------------
val exerciseTag = "exercise"

tasks.test {
    useJUnitPlatform {
        excludeTags(exerciseTag)
    }
}

tasks.register<Test>("exercises") {
    description = "Checks the learner's answers in the exercises packages."
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform {
        includeTags(exerciseTag)
    }
    testLogging {
        events("passed", "failed")
        showStandardStreams = false
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT
    }
    // Always re-run so learners see fresh results after editing.
    outputs.upToDateWhen { false }
}
