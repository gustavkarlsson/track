package se.gustavkarlsson.track.sqlite

import android.database.Cursor
import se.gustavkarlsson.track.Record

internal fun Cursor.toRecordSequence(): Sequence<Record> =
    sequence {
        while (moveToNext())
            yield(readExistingRecord())
    }

internal fun Cursor.readOptionalRecord(): Record? =
    if (moveToNext())
        readExistingRecord()
    else
        null

private fun Cursor.readExistingRecord(): Record = Record(
    id = this[Table.COLUMN_ID],
    key = this[Table.COLUMN_KEY],
    timestamp = this[Table.COLUMN_TIMESTAMP],
    appVersion = this[Table.COLUMN_APP_VERSION],
    value = this[Table.COLUMN_VALUE]
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
