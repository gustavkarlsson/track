package se.gustavkarlsson.track.sqlite

import se.gustavkarlsson.track.CloseableSequence
import se.gustavkarlsson.track.Record

internal class CloseableRecordCursorSequence(
    private val cursor: RecordCursor
) : CloseableSequence<Record> {
    private val sequence = sequence {
        cursor.use { cursor ->
            while (cursor.hasData) yield(cursor.readExistingRecord())
        }
    }

    private val RecordCursor.hasData get() = checkForDataOrClose()

    override fun iterator() = sequence.iterator()

    override fun close() = cursor.close()

    override val isClosed get() = cursor.isClosed
}
