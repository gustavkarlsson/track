import com.android.build.gradle.internal.dsl.TestOptions

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdkVersion(versions.compileSdk)

    compileOptions {
        sourceCompatibility = versions.java
        targetCompatibility = versions.java
    }

    kotlinOptions {
        jvmTarget = versions.jvmTarget
    }

    testOptions {
        unitTests(delegateClosureOf<TestOptions.UnitTestOptions> {
            isReturnDefaultValues = true
        })
    }

    defaultConfig {
        minSdkVersion(versions.minSdk)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${versions.kotlin}")
    implementation("com.android.support:support-annotations:${versions.supportAnnotations}")
    testImplementation("junit:junit:${versions.junit}")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:${versions.assertk}")
    testImplementation("org.mockito:mockito-core:${versions.mockito}")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:${versions.mockitoKotlin}")
}
