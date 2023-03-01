import org.gradle.api.JavaVersion

object Versions {
    // Config
    const val minLibSdk = 21
    const val minAppSdk = 26
    const val compileSdk = 33
    const val targetSdk = 33
    val java = JavaVersion.VERSION_1_8
    const val jvmTarget = "1.8"
    const val kotlin = "1.7.21"
    const val coroutines = "1.6.4"
    const val annotations = "1.5.0"
    const val androidGradle = "7.4.1"
    const val activityKtx = "1.6.1"
    const val ktlint = "11.2.0"

    // Test
    const val junit = "4.13.2"
    const val mockito = "3.3.3"
    const val mockitoKotlin = "2.2.0"
    const val strikt = "0.33.0"

    // Instrumentation Test
    const val androidTest = "1.5.2"

    // Lint
    val lint = androidGradle.add23ToMajorVersion()
}

// Needed to get the correct version of lint
// https://googlesamples.github.io/android-custom-lint-rules/api-guide.html#example:samplelintcheckgithubproject/lintversion?
private fun String.add23ToMajorVersion(): String {
    val majorVersion = takeWhile { it != '.' }.toInt()
    val rest = dropWhile { it != '.' }
    val lintMajorVersion = majorVersion + 23
    return "$lintMajorVersion$rest"
}
