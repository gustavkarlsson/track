package se.gustavkarlsson.track.sqlite

import android.provider.BaseColumns

internal const val DATABASE_VERSION = 1

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
        $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        $COLUMN_SINGLETON INTEGER NOT NULL,
        $COLUMN_KEY TEXT NOT NULL,
        $COLUMN_TIMESTAMP INTEGER NOT NULL,
        $COLUMN_APP_VERSION INTEGER NOT NULL,
        $COLUMN_VALUE TEXT NOT NULL)
    """.trimIndent()
}

internal typealias Table = RecordTableV1
