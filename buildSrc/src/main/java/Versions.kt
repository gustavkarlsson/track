import org.gradle.api.JavaVersion

object Versions {
    // Config
    const val minLibSdk = 21
    const val minAppSdk = 26
    const val compileSdk = 33
    const val targetSdk = 33
    val java = JavaVersion.VERSION_1_8
    const val jvmTarget = "1.8"
    const val kotlin = "1.7.20"
    const val annotations = "1.5.0"
    const val androidGradle = "7.4.1"
    const val dokka = "0.10.1"
    const val ktlint = "9.2.1"

    // Test
    const val junit = "4.13.2"
    const val mockito = "3.3.3"
    const val mockitoKotlin = "2.2.0"
    const val strikt = "0.33.0"

    // Instrumentation Test
    const val androidTest = "1.5.2"

    // Lint
    const val lint = "27.2.2"
}
