import org.gradle.kotlin.dsl.repositories

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.androidGradle}")
        classpath(kotlin("gradle-plugin", version = Versions.kotlin))
    }
}

allprojects {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }
}

plugins {
    base
}
