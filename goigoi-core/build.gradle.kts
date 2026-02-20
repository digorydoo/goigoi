plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // testImplementation(libs.kotlin.test)

    // implementation(libs.kokuban)
    // implementation(project(":kutils"))
}
