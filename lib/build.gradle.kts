@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    `maven-publish`
}

android {
    namespace = "se.gustavkarlsson.track"
    compileSdk = Versions.compileSdk

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
        minSdk = Versions.minLibSdk
        aarMetadata {
            minCompileSdk = Versions.compileSdk
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    lint {
        @Suppress("UnstableApiUsage")
        checkDependencies = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "track"
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

dependencies {
    implementation(kotlin("stdlib", Versions.kotlin))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    implementation("androidx.annotation:annotation:${Versions.annotations}")

    implementation(project(":lint"))
    lintPublish(project(":lint"))

    testImplementation("junit:junit:${Versions.junit}")
    testImplementation("io.strikt:strikt-core:${Versions.strikt}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockitoKotlin}")

    androidTestImplementation("io.strikt:strikt-core:${Versions.strikt}")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}")
    androidTestImplementation("androidx.test:runner:${Versions.androidTest}")
}
