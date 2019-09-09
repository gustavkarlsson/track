package se.gustavkarlsson.nag.sqlite

import android.database.Cursor
import se.gustavkarlsson.nag.CloseableSequence
import se.gustavkarlsson.nag.Record

internal class CloseableRecordCursorSequence(
	private val cursor: Cursor,
	private val readExistingRecord: Cursor.() -> Record = Cursor::readExistingRecord
) : CloseableSequence<Record> {
	private val sequence = sequence {
		cursor.use { cursor ->
			var hasData = cursor.hasData
			while (hasData) {
				val record = cursor.readExistingRecord()
				hasData = cursor.hasData
				yield(record)
			}
		}
	}

	private val Cursor.hasData
		get() = if (moveToNext()) {
			true
		} else {
			close()
			false
		}

	override fun iterator() = sequence.iterator()

	override fun close() = cursor.close()

	override val isClosed get() = cursor.isClosed
}
