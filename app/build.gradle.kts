plugins {
    id("com.android.application")
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
    // targetSdkVersion should be set to the highest value after having tested it on that api.
    // compileSdk should be the same as targetSdkVersion (unclear)

    defaultConfig {
        applicationId = "io.github.digorydoo.goigoi"
        minSdk = 31
        compileSdk = 36
        targetSdk = 36
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
            isShrinkResources = false
            buildConfigField("boolean", "ENABLE_CRASHLYTICS", "false")
        }
        named("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = false // enabling this would mean we need rules to keep custom assets
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.appcompat)
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.core.ktx)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.core.splashscreen)

    // implementation(libs.kstruct)

    implementation(project(":goigoi-core"))
    implementation(project(":kutils"))
}
