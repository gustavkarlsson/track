package se.gustavkarlsson.track.sqlite

import android.content.ContentValues
import androidx.annotation.VisibleForTesting

internal fun Map<String, Any?>.toContentValues(): ContentValues = toContentValues(::ContentValues)

@VisibleForTesting
internal fun Map<String, Any?>.toContentValues(constructor: (Int) -> ContentValues): ContentValues {
    val contentValues = constructor(values.size)
    forEach { (column, value) ->
        contentValues[column] = value
    }
    return contentValues
}

private operator fun ContentValues.set(column: String, value: Any?) {
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
