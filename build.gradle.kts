import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    id("com.android.application") version Versions.androidGradle apply false
    id("com.android.library") version Versions.androidGradle apply false
    id("org.jetbrains.kotlin.android") version Versions.kotlin apply false
    id("org.jlleitschuh.gradle.ktlint") version Versions.ktlint apply false
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
