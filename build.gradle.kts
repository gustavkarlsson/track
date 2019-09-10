import org.gradle.kotlin.dsl.repositories

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${versions.androidGradle}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${versions.kotlin}")
        classpath("pl.allegro.tech.build:axion-release-plugin:${versions.axionRelease}")
    }
}

allprojects {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }
}
