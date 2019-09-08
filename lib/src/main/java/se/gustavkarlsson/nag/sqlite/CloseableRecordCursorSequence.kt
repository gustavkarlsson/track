package se.gustavkarlsson.nag.sqlite

import android.database.Cursor
import se.gustavkarlsson.nag.CloseableSequence
import se.gustavkarlsson.nag.Record

internal class CloseableRecordCursorSequence(
	private val cursor: Cursor,
	private val readExistingRecord: Cursor.() -> Record = Cursor::readExistingRecord
) : CloseableSequence<Record> {
	private val sequence: Sequence<Record> = sequence {
		cursor.use { cursor ->
			var hasData = cursor.hasData
			while (hasData) {
				val entity = cursor.readExistingRecord()
				hasData = cursor.hasData
				yield(entity)
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

	override fun iterator(): Iterator<Record> = sequence.iterator()

	override fun close() = cursor.close()

	override val isClosed: Boolean get() = cursor.isClosed
}
