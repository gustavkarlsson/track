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
}

dependencies {
    implementation(project(":lib"))
    lintChecks(project(":lint"))
}
