package se.gustavkarlsson.nag

import android.app.AlertDialog
import android.content.Context
import se.gustavkarlsson.nag.sqlite.Helper
import se.gustavkarlsson.nag.sqlite.SqliteMultiRecords
import kotlin.system.exitProcess

interface Nag {
	val single: SingletonRecords
	val multi: MultiRecords
	fun clearDatabase()

	companion object : Nag {
		private var helper: Helper? = null
		private var innerSingle: SingletonRecords? = null
		private var innerMulti: MultiRecords? = null

		fun initialize(context: Context) {
			val newHelper = Helper(context)
			helper = newHelper
			innerSingle = TODO()
			innerMulti = SqliteMultiRecords(newHelper)
		}

		override val single: SingletonRecords
			get() = requireNotNull(innerSingle) { "Nag is not yet initialized. Run initialize() first" }

		override val multi: MultiRecords
			get() = requireNotNull(innerMulti) { "Nag is not yet initialized. Run initialize() first" }

		override fun clearDatabase() {
			requireNotNull(helper).deleteDatabase()
		}
	}
}

fun firstLaunchOnboarding() {
	val isFirstLaunch = Nag.multi.query("app_launched").firstOrNull() == null
	Nag.multi.add("app_launched")
	if (isFirstLaunch) {
		// Show welcome screen
	}
}

fun newTermsOfService() {
	val acceptedTosVersion = Nag.single.get("accepted_tos")?.appVersion ?: -1
	if (acceptedTosVersion < 54) {
		showTosScreen()
	}
}

fun showTosScreen() {
	AlertDialog.Builder(context)
		.setTitle("New TOS")
		.setMessage("Bla bla bla bla...")
		.setCancelable(false)
		.setPositiveButton("Accept") { _, _ ->
			Nag.single.set("accepted_tos")
		}
		.setNegativeButton("Close app") { _, _ ->
			exitProcess(1)
		}
		.show()
}

fun rateOurApp() {
	val usageCount = Nag.multi.query("used_feature_x").count()
	val thirtyDaysAgo = System.currentTimeMillis() - 2592000000
	val ratedApp = Nag.single.get("rated_app")
	when {
		// User still hasn't used the app enough
		usageCount < 5 -> return
		// User never saw the rating screen
		ratedApp == null -> showRatingScreen()
		// User has previously clicked "later" and it's been over 30 days
		ratedApp.value == "later" && ratedApp.timestamp < thirtyDaysAgo -> showRatingScreen()
	}
}

fun featureChanged() {
	val usageCount = Nag.multi.query("used_feature_x").count()
	val thirtyDaysAgo = System.currentTimeMillis() - 2592000000
	val ratedApp = Nag.single.get("rated_app")
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
			Nag.single.set("rated_app", "rated")
			// Open play store
		}
		.setNegativeButton("Never") { _, _ ->
			Nag.single.set("rated_app", "never")
		}
		.setNeutralButton("Later") { _, _ ->
			Nag.single.set("rated_app", "later")
		}
		.setOnCancelListener {
			Nag.single.set("rated_app", "later")
		}
		.show()
}

val context: Context = TODO()
