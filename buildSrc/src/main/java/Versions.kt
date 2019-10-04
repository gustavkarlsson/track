import org.gradle.api.JavaVersion

object Versions {
    // Config
    val minSdk = 21
    val compileSdk = 29
    val targetSdk = 29
    val buildTools = "29.0.2"
    val java = JavaVersion.VERSION_1_8
    val jvmTarget = "1.8"
    val kotlin = "1.3.50"
    val supportAnnotations = "28.0.0"
    val androidGradle = "3.5.0"
    val dokka = "0.9.18"
    val ktlint = "9.0.0"

    // Test
    val junit = "4.12"
    val assertk = "0.19"
    val mockito = "3.0.0"
    val mockitoKotlin = "2.2.0"

    // Instrumentation Test
    val androidTest = "1.2.0"
}
