package se.gustavkarlsson.nag.sqlite

import android.database.Cursor
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import se.gustavkarlsson.nag.Record

class CloseableRecordCursorSequenceTest {

	private val records = listOf(
		Record(1, "key1", 1, 1, "value1"),
		Record(2, "key2", 2, 2, "value2")
	)

	var cursorIndex = -1

	private val mockCursor = mock<Cursor> {
		var closed = false

		on { moveToNext() }.then {
			cursorIndex++
			records.size > cursorIndex
		}

		on { close() }.then {
			closed = true
			Unit
		}

		on { isClosed }.then { closed }
	}

	private val readExistingRecord: Cursor.() -> Record = { records[cursorIndex] }

	private val underTest = CloseableRecordCursorSequence(mockCursor, readExistingRecord)

	@Test
	fun `initially not closed`() {
		assertThat(underTest.isClosed).isFalse()
	}

	@Test
	fun `closes if closed`() {
		underTest.close()

		assertThat(underTest.isClosed).isTrue()
	}

	@Test
	fun `read all records`() {
		val result = underTest.toList()

		assertThat(result).isEqualTo(records)
	}

	@Test
	fun `exhausting sequence closes it`() {
		underTest.count()

		assertThat(underTest.isClosed).isTrue()
	}

	@Test
	fun `closing sequence calls close on underlying cursor`() {
		underTest.close()

		verify(mockCursor).close()
	}
}
