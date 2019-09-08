package se.gustavkarlsson.nag.sqlite

import android.database.Cursor
import se.gustavkarlsson.nag.Record

internal fun Cursor.tryGetRecordAndClose(
	readRecord: Cursor.() -> Record = Cursor::readExistingRecord
): Record? =
	use { cursor ->
		if (cursor.moveToNext()) {
			cursor.readRecord()
		} else {
			null
		}
	}
