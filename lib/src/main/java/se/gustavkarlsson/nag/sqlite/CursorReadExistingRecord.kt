package se.gustavkarlsson.nag.sqlite

import android.database.Cursor
import se.gustavkarlsson.nag.Record

internal fun Cursor.readExistingRecord(): Record = Record(
	id = get(Table.COLUMN_ID),
	key = get(Table.COLUMN_KEY),
	timestamp = get(Table.COLUMN_TIMESTAMP),
	appVersion = get(Table.COLUMN_APP_VERSION),
	value = get(Table.COLUMN_VALUE)
)

private inline operator fun <reified T> Cursor.get(column: String): T {
	val index = getColumnIndexOrThrow(column)
	return when (T::class) {
		Short::class -> getShort(index)
		Int::class -> getInt(index)
		Long::class -> getLong(index)
		Float::class -> getFloat(index)
		Double::class -> getDouble(index)
		ByteArray::class -> getBlob(index)
		String::class -> getString(index)
		else -> throw IllegalArgumentException("Could not read ${T::class} from $column")
	} as T
}
