# Track

Simple on-device event tracking and querying for Android

---

## Some examples

Track every app launch and show onboarding screen first time

```kotlin
val isFirstLaunch = Track.query("app_launched").none()
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
val usageCount = Track.query("used_feature_x").count()
val thirtyDaysAgo = System.currentTimeMillis() - 2592000000
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