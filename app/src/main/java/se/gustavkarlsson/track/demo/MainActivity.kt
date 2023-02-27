package se.gustavkarlsson.track.demo

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import se.gustavkarlsson.track.Record
import se.gustavkarlsson.track.Track

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycleScope.launch {
            if (savedInstanceState == null) {
                Track.add("activity_started")
            }
            val records = Track.query("activity_started") { it.toList() }
            findViewById<TextView>(R.id.mainTextView).text = createText(records)
        }
    }
}

private fun createText(records: List<Record>) = buildString {
    append("Activity started ${records.count()} ")
    if (records.count() == 1) {
        appendLine("time")
    } else {
        appendLine("times")
    }
    appendLine()
    appendRecord("First", records.first())
    appendLine()
    appendLine()
    appendRecord("Last", records.last())
}

private fun StringBuilder.appendRecord(indexName: String, record: Record) {
    val instant = Instant.ofEpochMilli(record.timestamp)
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val timeString = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime)
    appendLine("$indexName record was:")
    appendLine("At $timeString")
    append("With app version ${record.appVersion}")
}
