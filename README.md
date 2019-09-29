# Track

Simple on-device event tracking and querying for Android

---

## Usage

Initialize in Application `onCreate`

```kotlin
Track.initialize(this)
```

Track events using a single variable with `set`

```kotlin
Track.set("used_feature_x", "value")
```

... or track multiple events per key with `add`

```kotlin
Track.add("used_feature_x", "value")
```

Read a single variable record back with `get`

```kotlin
val record = Track.get("used_feature_x")
```

... or use a `query` for multiple events

```kotlin
val recordSequence = Track.query("used_feature_x")
```

You can also do more advanced queries

```kotlin
val recordSequence = Track.query("used_feature_x", order = Order.Descending) {
    appVersion isGreaterThan 12
    value isNotEqualTo "forbidden"
}
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

## Some use cases

Track every app launch and show onboarding screen first time

```kotlin
val isFirstLaunch = Track.query("app_launched") { it.none() }
Track.add("app_launched")
if (isFirstLaunch) {
    showOnboardingScreen()
}
```

New terms of service introduced in version 54.
Show dialog if user hasn't accepted them yet

```kotlin
val acceptedTosVersion = Track.get("accepted_tos")?.appVersion ?: -1
if (acceptedTosVersion < 54) {
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
            exitProcess(1)
        }
        .show()
}

```

Ask user to rate app if certain conditions are met

```kotlin
val usageCount = Track.query("used_feature_x") { it.count() }
val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
val ratedApp = Track.get("rated_app")
when {
    // User still hasn't used the app enough
    usageCount < 5 -> return
    // User never saw the rating screen
    ratedApp == null -> showRatingScreen()
    // User has previously clicked "later" and it's been over 30 days
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
