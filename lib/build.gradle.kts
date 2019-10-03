import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.jetbrains.dokka") version Versions.dokka
    id("pl.allegro.tech.build.axion-release") version Versions.axionRelease
    `maven-publish`
}

scmVersion.tag.prefix = ""

// Enables specifying version using argument
version = version.takeUnless { it == "unspecified" } ?: scmVersion.version

// Enables specifying group using argument
group = group.takeIf { it.toString().contains('.') } ?: "se.gustavkarlsson.track"

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
            groupId = project.group.toString()
            artifactId = "track"
            version = project.version.toString()
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
