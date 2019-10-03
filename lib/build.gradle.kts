plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdkVersion(Versions.compileSdk)

    compileOptions {
        sourceCompatibility = Versions.java
        targetCompatibility = Versions.java
    }

    kotlinOptions {
        jvmTarget = Versions.jvmTarget
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    defaultConfig {
        minSdkVersion(Versions.minSdk)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
    implementation("com.android.support:support-annotations:${Versions.supportAnnotations}")

    testImplementation("junit:junit:${Versions.junit}")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:${Versions.assertk}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockitoKotlin}")

    androidTestImplementation("com.willowtreeapps.assertk:assertk-jvm:${Versions.assertk}")
    androidTestImplementation("androidx.test:runner:${Versions.androidTest}")
}
