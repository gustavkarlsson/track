plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(Versions.compileSdk)
    buildToolsVersion(Versions.buildTools)

    compileOptions {
        sourceCompatibility = Versions.java
        targetCompatibility = Versions.java
    }

    packagingOptions {
        exclude("META-INF/LICENSE")
        exclude("META-INF/NOTICE")
    }

    defaultConfig {
        applicationId = "se.gustavkarlsson.track.demo"
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation(project(":lib"))
}
