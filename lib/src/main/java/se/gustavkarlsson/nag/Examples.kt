package se.gustavkarlsson.nag

import android.app.AlertDialog
import android.content.Context
import kotlin.system.exitProcess

private val context: Context = TODO()

private fun firstLaunchOnboarding() {
	val isFirstLaunch = Nag.query("app_launched").none()
	Nag.add("app_launched")
	if (isFirstLaunch) {
		// Show welcome screen
	}
}

private fun newTermsOfService() {
	val acceptedTosVersion = Nag.getSingle("accepted_tos")?.appVersion ?: -1
	if (acceptedTosVersion < 54) {
		showTosScreen()
	}
}

private fun showTosScreen() {
	AlertDialog.Builder(context)
		.setTitle("New TOS")
		.setMessage("Bla bla bla bla...")
		.setCancelable(false)
		.setPositiveButton("Accept") { _, _ ->
			Nag.setSingle("accepted_tos")
		}
		.setNegativeButton("Close app") { _, _ ->
			exitProcess(1)
		}
		.show()
}

private fun rateOurApp() {
	val usageCount = Nag.query("used_feature_x").count()
	val thirtyDaysAgo = System.currentTimeMillis() - 2592000000
	val ratedApp = Nag.getSingle("rated_app")
	when {
		// User still hasn't used the app enough
		usageCount < 5 -> return
		// User never saw the rating screen
		ratedApp == null -> showRatingScreen()
		// User has previously clicked "later" and it's been over 30 days
		ratedApp.value == "later" && ratedApp.timestamp < thirtyDaysAgo -> showRatingScreen()
	}
}

private fun showRatingScreen() {
	AlertDialog.Builder(context)
		.setTitle("Rate our app")
		.setMessage("If you like our app, please take a second and rate it on the Play Store!")
		.setPositiveButton("Rate") { _, _ ->
			Nag.setSingle("rated_app", "rated")
			// Open play store
		}
		.setNegativeButton("Never") { _, _ ->
			Nag.setSingle("rated_app", "never")
		}
		.setNeutralButton("Later") { _, _ ->
			Nag.setSingle("rated_app", "later")
		}
		.setOnCancelListener {
			Nag.setSingle("rated_app", "later")
		}
		.show()
}
