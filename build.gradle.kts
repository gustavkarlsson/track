import org.gradle.kotlin.dsl.repositories

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.androidGradle}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("pl.allegro.tech.build:axion-release-plugin:${Versions.axionRelease}")
    }
}

allprojects {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }
}
