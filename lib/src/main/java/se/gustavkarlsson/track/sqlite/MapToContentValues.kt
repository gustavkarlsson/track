package se.gustavkarlsson.track.sqlite

import android.content.ContentValues

internal fun Map<String, Any?>.toContentValues(): ContentValues =
	ContentValues(values.size).apply {
		forEach { (column, value) ->
			when (value) {
				null -> putNull(column)
				is Boolean -> put(column, value)
				is ByteArray -> put(column, value)
				is Byte -> put(column, value)
				is Short -> put(column, value)
				is Int -> put(column, value)
				is Long -> put(column, value)
				is Float -> put(column, value)
				is Double -> put(column, value)
				is String -> put(column, value)
				else -> throw IllegalArgumentException("Unsupported type: ${value.javaClass}")
			}
		}
	}
