package se.gustavkarlsson.nag.sqlite

import android.database.Cursor
import se.gustavkarlsson.nag.CloseableSequence

internal abstract class CloseableCursorSequence<T>(
	private val cursor: Cursor
) : CloseableSequence<T> {
	private val sequence: Sequence<T> = sequence {
		cursor.use { cursor ->
			var hasData = cursor.hasData
			while (hasData) {
				val entity = cursor.readEntity()
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

	override fun iterator(): Iterator<T> = sequence.iterator()

	override fun close() = cursor.close()

	protected abstract fun Cursor.readEntity(): T
}
