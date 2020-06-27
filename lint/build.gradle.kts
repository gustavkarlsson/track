plugins {
    id("kotlin")
    `java-library`
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Lint-Registry-v2"] = "se.gustavkarlsson.track.lint.IssueRegistry"
    }
}

dependencies {
    compileOnly(kotlin("stdlib", version = Versions.kotlin))
    // Lint
    compileOnly("com.android.tools.lint:lint-api:${Versions.lint}")
    compileOnly("com.android.tools.lint:lint-checks:${Versions.lint}")

    // Lint testing
    testImplementation("com.android.tools.lint:lint-tests:${Versions.lint}")
}
