plugins {
    kotlin("jvm")
    application
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("io.github.digorydoo.goigoi.compiler.MainKt")
}

dependencies {
    testImplementation(libs.kotlin.test)

    implementation(libs.kokuban)
    implementation(libs.xmlparserv2)

    implementation(project(":goigoi-core"))
    implementation(project(":kutils"))
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = application.mainClass
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)

    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
}
