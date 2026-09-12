plugins {
    id("com.android.application")
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(17)
}

android {
    // The namespace is the package name where the R resource is imported from.
    // This is *not* necessarily the same as applicationId, although identical in my case.
    namespace = "io.github.digorydoo.goigoi"

    // api level 23 == Android 6 == e.g. Samsung Galaxy S5
    // api level 31 == Android 12 == e.g. Samsung Galaxy S10
    // targetSdk should be set to the highest value after having tested it on that api.
    // compileSdk should be the same as targetSdk (unclear)

    defaultConfig {
        applicationId = "io.github.digorydoo.goigoi"
        minSdk = 31
        compileSdk = 37
        targetSdk = 37
        versionCode = 50
        versionName = "2.5.3"
    }

    flavorDimensions += "version"

    productFlavors {
        create("japanese_free") {
            dimension = "version"
            applicationIdSuffix = ".ja_free"
        }
    }

    buildTypes {
        named("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            // noinspection NotShrinkingResources
            isShrinkResources = false
            buildConfigField("boolean", "ENABLE_CRASHLYTICS", "false")
        }
        named("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true // Android Resource Shrinker ignores our custom assets in assets/ directory
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    tasks.whenTaskAdded {
        // Android linting takes a long time, so let's disable it by default.
        // You can still call it manually with ./gradlew lint
        if (name.startsWith("lint")) {
            val lintExplicitlyWanted = gradle.startParameter.taskNames.any {
                it.contains("lint", ignoreCase = true)
            }
            if (!lintExplicitlyWanted) {
                enabled = false
            }
        }
    }
}

tasks.named("preBuild") {
    dependsOn(":goigoi-core:test")
    dependsOn(":kutils:test")
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.core.ktx)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.core.splashscreen)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    // implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)

    implementation(project(":goigoi-core"))
    implementation(project(":kutils"))
}
