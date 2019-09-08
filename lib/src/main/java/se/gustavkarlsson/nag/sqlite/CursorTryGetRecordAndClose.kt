package se.gustavkarlsson.nag.sqlite

import android.database.Cursor
import se.gustavkarlsson.nag.Record

internal fun Cursor.tryGetRecordAndClose(
	readExistingRecord: Cursor.() -> Record = Cursor::readExistingRecord
): Record? =
	use { cursor ->
		if (cursor.moveToNext()) {
			cursor.readExistingRecord()
		} else {
			null
		}
	}
