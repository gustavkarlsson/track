import org.jlleitschuh.gradle.ktlint.KtlintExtension

buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.androidGradle}")
        classpath(kotlin("gradle-plugin", version = Versions.kotlin))
        classpath("org.jlleitschuh.gradle:ktlint-gradle:${Versions.ktlint}")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extensions.getByType(typeOf<KtlintExtension>()).apply {
        android.set(true)
    }
}

plugins {
    base
}
