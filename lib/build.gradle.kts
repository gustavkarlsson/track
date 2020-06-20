import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("org.jetbrains.dokka") version Versions.dokka
    `maven-publish`
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
    implementation(kotlin("stdlib", version = Versions.kotlin))
    implementation("com.android.support:support-annotations:${Versions.supportAnnotations}")

    testImplementation("junit:junit:${Versions.junit}")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:${Versions.assertk}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockitoKotlin}")

    androidTestImplementation("com.willowtreeapps.assertk:assertk-jvm:${Versions.assertk}")
    androidTestImplementation("androidx.test:runner:${Versions.androidTest}")
}
