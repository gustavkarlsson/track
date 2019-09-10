package se.gustavkarlsson.track.sqlite

import android.provider.BaseColumns

internal object Database {
	const val NAME = "track.db"
	const val VERSION = 1
}

internal object RecordTableV1 {
	const val NAME = "record"
	const val COLUMN_ID = BaseColumns._ID
	const val COLUMN_SINGLETON = "singleton"
	const val COLUMN_KEY = "key"
	const val COLUMN_TIMESTAMP = "timestamp"
	const val COLUMN_APP_VERSION = "app_version"
	const val COLUMN_VALUE = "value"
	val CREATE_STATEMENT = """
		CREATE TABLE $NAME (
		$COLUMN_ID INTEGER PRIMARY KEY,
		$COLUMN_SINGLETON INTEGER,
		$COLUMN_KEY TEXT,
		$COLUMN_TIMESTAMP INTEGER,
		$COLUMN_APP_VERSION INTEGER,
		$COLUMN_VALUE TEXT)
	""".trimIndent()
}

internal typealias Table = RecordTableV1
