plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain(17)
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/java"))
        }
    }
}

dependencies {
    testImplementation(libs.kotlin.test)
    implementation(libs.kotlinx.datetime)
}
