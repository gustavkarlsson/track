package se.gustavkarlsson.track.sqlite

import se.gustavkarlsson.track.CloseableSequence
import se.gustavkarlsson.track.Record

internal class CloseableRecordCursorSequence(
	private val cursor: RecordCursor
) : CloseableSequence<Record> {
	private val sequence = sequence {
		cursor.use { cursor ->
			var hasData = cursor.checkForDataOrClose()
			while (hasData) {
				val record = cursor.readExistingRecord()
				hasData = cursor.checkForDataOrClose()
				yield(record)
			}
		}
	}

	override fun iterator() = sequence.iterator()

	override fun close() = cursor.close()

	override val isClosed get() = cursor.isClosed
}
