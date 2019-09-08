package se.gustavkarlsson.nag.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import se.gustavkarlsson.nag.Nag


class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		Nag.initialize(this)
		Nag.deleteDatabase()
		Nag.add("app_started")
		val stored = Nag.query("app_started") {
			after(1567959287960)
		}.count()
		Toast.makeText(this, "stored and retrieved value: $stored", Toast.LENGTH_LONG)
			.show()
	}
}
