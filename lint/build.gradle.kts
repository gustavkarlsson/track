plugins {
    id("kotlin")
    `java-library`
    id("com.android.lint")
}

dependencies {
    compileOnly(kotlin("stdlib", version = Versions.kotlin))
    // Lint
    compileOnly("com.android.tools.lint:lint-api:${Versions.lint}")
    compileOnly("com.android.tools.lint:lint-checks:${Versions.lint}")

    // Lint testing
    testImplementation("junit:junit:${Versions.junit}")
    testImplementation("com.android.tools.lint:lint:${Versions.lint}")
    testImplementation("com.android.tools.lint:lint-tests:${Versions.lint}")
}
