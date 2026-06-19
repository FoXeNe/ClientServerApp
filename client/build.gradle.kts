plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    id("com.gradleup.shadow") version "8.3.0"
}

repositories {
    mavenCentral()
}

val jfxVersion = "21.0.5"
val jfxClassifier =
    run {
        val os = System.getProperty("os.name").lowercase()
        when {
            os.contains("win") -> "win"
            os.contains("mac") -> if (System.getProperty("os.arch").contains("aarch64")) "mac-aarch64" else "mac"
            else -> "linux"
        }
    }

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(libs.junit.jupiter.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(libs.guava)
    implementation(project(":common"))

    implementation("org.openjfx:javafx-base:$jfxVersion:$jfxClassifier")
    implementation("org.openjfx:javafx-graphics:$jfxVersion:$jfxClassifier")
    implementation("org.openjfx:javafx-controls:$jfxVersion:$jfxClassifier")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass = "MainKt"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    mergeServiceFiles()
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
}
