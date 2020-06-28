[![Verify](https://github.com/gustavkarlsson/track/workflows/Verify/badge.svg)](https://github.com/gustavkarlsson/track/actions)
[![Version](https://jitpack.io/v/gustavkarlsson/track.svg)](https://jitpack.io/#gustavkarlsson/track)
[![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/gustavkarlsson/track/blob/master/LICENSE.md)

# Track

Simple on-device event tracking for Android.

## Usage

Initialize the built in singleton in `Application.onCreate()`.

```kotlin
Track.initialize(this)
```

Or create a new instance.

```kotlin
val track = Track.create(context, "my_records.db")
```

Track a single value (overwriting the existing one) with `set()`.

```kotlin
Track.set("show_intro", "false")
```

Read it back as a record with `get()`.

```kotlin
val record = Track.get("show_intro")
```

Records look like this:

```kotlin
data class Record(
    val id: Long,
    val key: String,
    val timestamp: Long,
    val appVersion: Long,
    val value: String
)
```

Track multiple events per key with `add()`.

```kotlin
Track.add("screen_visited", "settings")
```

Use `query()` to find all records of any given key.

```kotlin
val allScreenVisits = Track.query("screen_visited")
```

You don't have to read all query results into memory.

```kotlin
val firstFiveVisitsToAboutScreen = Track.query("screen_visited") { records ->
    records
        .filter { it.value == "about" }
        .take(5)
        .toList() // Consume the sequence before returning
}
```

## Use cases

Track app launches and show onboarding screen only the first time.

```kotlin
val isFirstLaunch = Track.query("app_launched") { it.none() }
Track.add("app_launched")
if (isFirstLaunch) {
    // Show onboarding screen
}
```

New terms of service was introduced in version 54.
Show a dialog if user hasn't accepted them yet.

```kotlin
val lastAcceptedTosVersion = Track.get("accepted_tos")?.appVersion ?: -1
if (lastAcceptedTosVersion < 54) {
    showTosScreen()
}

private fun showTosScreen() {
    AlertDialog.Builder(context)
        .setTitle("New TOS")
        .setMessage("Bla bla bla bla...")
        .setCancelable(false)
        .setPositiveButton("Accept") { _, _ ->
            Track.set("accepted_tos")
        }
        .setNegativeButton("Close app") { _, _ ->
            exitProcess(0)
        }
        .show()
}
```

Ask user to rate the app under certain conditions.

```kotlin
val usageCount = Track.query("used_feature_x") { it.count() }
val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
val ratedApp = Track.get("rated_app")
when {
    // User still hasn't used the app much
    usageCount < 5 -> return
    // User never saw the rating screen
    ratedApp == null -> showRatingScreen()
    // User previously clicked "later" and it's been over 30 days
    ratedApp.value == "later" && ratedApp.timestamp < thirtyDaysAgo -> showRatingScreen()
}

private fun showRatingScreen() {
    AlertDialog.Builder(context)
        .setTitle("Rate our app")
        .setMessage("If you like our app, please take a second and rate it on the Play Store!")
        .setPositiveButton("Rate") { _, _ ->
            Track.set("rated_app", "rated")
            // Open play store
        }
        .setNegativeButton("Never") { _, _ ->
            Track.set("rated_app", "never")
        }
        .setNeutralButton("Later") { _, _ ->
            Track.set("rated_app", "later")
        }
        .setOnCancelListener {
            Track.set("rated_app", "later")
        }
        .show()
}
```

## Download

Track is hosted on JitPack. Here's how you include it in your gradle project:

**Step 1.** Add the JitPack repository to your `build.gradle` or `build.gradle.kts` file:

Groovy
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

Kotlin
```kotlin
repositories {
    maven { setUrl("https://jitpack.io") }
}
```

**Step 2.** Add the dependency:

Groovy
```groovy
dependencies {
    implementation 'com.github.gustavkarlsson:track:<latest_version>'
}
```

Kotlin
```kotlin
dependencies {
    implementation("com.github.gustavkarlsson:track:<latest_version>")
}
```
