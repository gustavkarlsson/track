package se.gustavkarlsson.track.demo

import android.app.Activity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import se.gustavkarlsson.track.Track

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            Track.add("activity_started")
        }
        val result = Track.query("activity_started") { it.count() }
        val text = resources.getQuantityString(R.plurals.activity_started_n_times, result, result)
        mainTextView.text = text
    }
}
