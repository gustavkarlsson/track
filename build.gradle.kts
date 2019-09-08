import org.gradle.kotlin.dsl.repositories

buildscript {
	repositories {
		jcenter()
		mavenCentral()
		google()
		maven { setUrl("https://maven.fabric.io/public") }
	}
	dependencies {
		classpath("com.android.tools.build:gradle:${versions.androidGradle}")
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${versions.kotlin}")
		classpath("com.google.gms:google-services:${versions.googleServices}")
		classpath("io.fabric.tools:gradle:${versions.fabric}")
		classpath("com.akaita.android:easylauncher:${versions.easylauncher}")
		classpath("org.eclipse.jgit:org.eclipse.jgit:${versions.jgit}")
		classpath("pl.allegro.tech.build:axion-release-plugin:${versions.axionRelease}")
		classpath("com.github.triplet.gradle:play-publisher:${versions.playPublisher}")
		classpath("com.squareup.sqldelight:gradle-plugin:${versions.sqldelight}")
	}
}

allprojects {
	repositories {
		jcenter()
		mavenCentral()
		google()
		maven { setUrl("https://s3.amazonaws.com/repo.commonsware.com") }
		maven { setUrl("https://jitpack.io") }
		maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
	}
}
