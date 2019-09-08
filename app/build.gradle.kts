plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(versions.compileSdk)
    buildToolsVersion(versions.buildTools)

    compileOptions {
        sourceCompatibility = versions.java
        targetCompatibility = versions.java
    }

    packagingOptions {
        exclude("META-INF/LICENSE")
        exclude("META-INF/NOTICE")
    }

    defaultConfig {
        applicationId = "se.gustavkarlsson.nag.example"
        minSdkVersion(versions.minSdk)
        targetSdkVersion(versions.targetSdk)
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation(project(":lib"))

    implementation("androidx.appcompat:appcompat:${versions.androidAppcompat}")
    implementation("com.google.android.material:material:${versions.androidMaterial}")
}
