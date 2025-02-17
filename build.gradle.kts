plugins {
    kotlin("jvm") version "1.9.24"
    application
}

group = "me.mibers"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.minestom:minestom-snapshots:7589b3b655")
    implementation("org.luaj:luaj-jse:3.0.1")
    implementation("party.iroiro.luajava:luajava:4.0.2")
    implementation("party.iroiro.luajava:luajit:4.0.2")
    runtimeOnly("party.iroiro.luajava:luajit-platform:4.0.2:natives-desktop")
}

tasks.test { useJUnitPlatform() }

kotlin { jvmToolchain(21) }
