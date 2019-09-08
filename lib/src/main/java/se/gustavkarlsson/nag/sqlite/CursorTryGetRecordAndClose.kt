package se.gustavkarlsson.nag.sqlite

import android.database.Cursor
import se.gustavkarlsson.nag.Record

fun Cursor.tryGetRecordAndClose(): Record? =
	use { cursor ->
		if (cursor.moveToNext()) {
			cursor.readRecord()
		} else {
			null
		}
	}
