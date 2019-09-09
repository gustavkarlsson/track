package se.gustavkarlsson.nag.sqlite

import android.database.Cursor
import se.gustavkarlsson.nag.Record
import java.io.Closeable

internal interface RecordCursor : Closeable {
	fun readExistingRecord(): Record
	fun tryGetRecordAndClose(): Record?
	fun checkForDataOrClose(): Boolean
	val isClosed: Boolean
}

internal class DefaultRecordCursor(private val cursor: Cursor) : RecordCursor {

	override fun readExistingRecord(): Record = Record(
		id = cursor[Table.COLUMN_ID],
		key = cursor[Table.COLUMN_KEY],
		timestamp = cursor[Table.COLUMN_TIMESTAMP],
		appVersion = cursor[Table.COLUMN_APP_VERSION],
		value = cursor[Table.COLUMN_VALUE]
	)

	override fun tryGetRecordAndClose(): Record? =
		cursor.use { cursor ->
			if (cursor.moveToNext()) {
				readExistingRecord()
			} else {
				null
			}
		}

	override fun checkForDataOrClose() =
		if (cursor.moveToNext()) {
			true
		} else {
			close()
			false
		}

	override fun close() = cursor.close()

	override val isClosed get() = cursor.isClosed
}

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
