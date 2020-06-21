package se.gustavkarlsson.track.demo

import android.app.Application
import se.gustavkarlsson.track.Track

@Suppress("unused")
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Track.initialize(this)
    }
}
