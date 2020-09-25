@file:Suppress("UnstableApiUsage")

import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("kotlin-android-extensions")
    id("org.jetbrains.dokka") version Versions.dokka
    `maven-publish`
}

kotlin {
    android("libAndroid") { // Renamed because of https://youtrack.jetbrains.com/issue/KT-34650
        compilations.all { kotlinOptions.jvmTarget = Versions.jvmTarget }
    }
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val libAndroidMain by getting {
            dependencies {
                implementation("androidx.annotation:annotation:${Versions.annotations}")
            }
        }
        val libAndroidTest by getting {
            dependencies {
                implementation("junit:junit:${Versions.junit}")
                implementation("io.strikt:strikt-core:${Versions.strikt}")
                implementation("org.mockito:mockito-core:${Versions.mockito}")
                implementation("com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockitoKotlin}")
            }
        }
        val libAndroidAndroidTest by getting {
            dependencies {
                implementation("io.strikt:strikt-core:${Versions.strikt}")
                implementation("androidx.test:runner:${Versions.androidTest}")
            }
        }
    }
}

task<DokkaTask>("dokkaJavadoc") {
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/javadoc"
}

task<Jar>("javadocJar") {
    from((tasks["dokkaJavadoc"] as DokkaTask).outputDirectory)
    archiveClassifier.set("javadoc")
}

task<Jar>("sourcesJar") {
    from(android.sourceSets["main"].java.srcDirs)
    archiveClassifier.set("sources")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "track"
            artifact(tasks["javadocJar"])
            artifact(tasks["sourcesJar"])
            artifact("$buildDir/outputs/aar/${project.name}-release.aar") {
                builtBy(tasks["assemble"])
            }
        }
    }
}

android {
    compileSdkVersion(Versions.compileSdk)

    sourceSets["main"].manifest.srcFile("src/libAndroidMain/AndroidManifest.xml")
    sourceSets["test"].resources.srcDir("src/libAndroidTest/resources")
    sourceSets["androidTest"].java.setSrcDirs(listOf("src/libAndroidAndroidTest/kotlin"))

    compileOptions {
        sourceCompatibility = Versions.java
        targetCompatibility = Versions.java
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    defaultConfig {
        minSdkVersion(Versions.minLibSdk)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    lintOptions {
        baseline("lint-baseline.xml")
    }
}
