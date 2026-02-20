pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

plugins {
    // The following is necessary, because the implicit toolchain resolving mechanism has been deprecated in Gradle 8.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" // can't access libs.versions.toml from here
}

rootProject.name = "goigoi"
include("app", "goigoi-compiler", "goigoi-core", "kutils")
