package se.gustavkarlsson.track.demo

import android.app.Activity
import android.os.Bundle
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.android.synthetic.main.activity_main.*
import se.gustavkarlsson.track.Record
import se.gustavkarlsson.track.Track

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            Track.add("activity_started")
        }
        val records = Track.query("activity_started") { it.toList() }
        mainTextView.text = createText(records)
    }
}

private fun createText(records: List<Record>) = buildString {
    append("Activity started ${records.count()} ")
    if (records.count() == 1) {
        appendln("time")
    } else {
        appendln("times")
    }
    appendln()
    appendRecord("First", records.first())
    appendln()
    appendln()
    appendRecord("Last", records.last())
}

private fun StringBuilder.appendRecord(indexName: String, record: Record) {
    val instant = Instant.ofEpochMilli(record.timestamp)
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val timeString = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime)
    appendln("$indexName record was:")
    appendln("At $timeString")
    append("With app version ${record.appVersion}")
}
