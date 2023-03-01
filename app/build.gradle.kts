plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "se.gustavkarlsson.track.demo"
    compileSdk = Versions.compileSdk

    compileOptions {
        sourceCompatibility = Versions.java
        targetCompatibility = Versions.java
    }

    defaultConfig {
        applicationId = "se.gustavkarlsson.track.demo"
        minSdk = Versions.minAppSdk
        targetSdk = Versions.targetSdk
        versionCode = 1
        versionName = "1.0.0"
    }
    lint {
        @Suppress("UnstableApiUsage")
        checkDependencies = true
    }
}

dependencies {
    implementation(project(":lib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")
    implementation("androidx.activity:activity-ktx:${Versions.activityKtx}")
}
