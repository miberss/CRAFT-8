plugins {
    kotlin("jvm") version "1.9.24"
    application
}

group = "me.mibers"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.minestom:minestom-snapshots:7589b3b655")
    implementation("org.luaj:luaj-jse:3.0.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}