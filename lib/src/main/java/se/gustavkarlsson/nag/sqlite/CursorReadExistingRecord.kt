package se.gustavkarlsson.nag.sqlite

import android.database.Cursor
import se.gustavkarlsson.nag.Record

internal fun Cursor.readExistingRecord(): Record = Record(
	id = getLong(getColumnIndex(Table.COLUMN_ID)),
	key = getString(getColumnIndex(Table.COLUMN_KEY)),
	timestamp = getLong(getColumnIndex(Table.COLUMN_TIMESTAMP)),
	appVersion = getLong(getColumnIndex(Table.COLUMN_APP_VERSION)),
	value = getString(getColumnIndex(Table.COLUMN_VALUE))
)
