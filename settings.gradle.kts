pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

rootProject.name = "track"

include(
    ":lib",
    ":app",
    ":lint"
)
