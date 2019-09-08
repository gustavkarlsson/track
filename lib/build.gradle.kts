plugins {
	id("com.android.library")
	id("kotlin-android")
}

android {
	compileSdkVersion(versions.compileSdk)

	compileOptions {
		sourceCompatibility = versions.java
		targetCompatibility = versions.java
	}

	defaultConfig {
		minSdkVersion(versions.minSdk)
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${versions.kotlin}")
}
