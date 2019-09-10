package se.gustavkarlsson.track.demo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import se.gustavkarlsson.track.Track

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		Track.initialize(this)
		Track.add("activity_started")
		val result = Track.query("activity_started").count()
		Toast.makeText(this, "Activity started $result times", Toast.LENGTH_LONG)
			.show()
	}
}
