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
		Nag.add("activity_started")
		val result = Nag.query("activity_started").count()
		Toast.makeText(this, "Activity started $result times", Toast.LENGTH_LONG)
			.show()
	}
}
