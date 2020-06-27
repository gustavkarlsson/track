import org.gradle.api.JavaVersion

object Versions {
    // Config
    const val minLibSdk = 21
    const val minAppSdk = 26
    const val compileSdk = 29
    const val targetSdk = 29
    const val buildTools = "29.0.3"
    val java = JavaVersion.VERSION_1_8
    const val jvmTarget = "1.8"
    const val kotlin = "1.3.72"
    const val annotations = "1.1.0"
    const val androidGradle = "4.0.0"
    const val dokka = "0.10.1"
    const val ktlint = "9.2.1"

    // Test
    const val junit = "4.12"
    const val mockito = "3.3.3"
    const val mockitoKotlin = "2.2.0"
    const val strikt = "0.26.1"

    // Instrumentation Test
    const val androidTest = "1.2.0"

    // Lint
    const val lint = "27.0.0"
}
