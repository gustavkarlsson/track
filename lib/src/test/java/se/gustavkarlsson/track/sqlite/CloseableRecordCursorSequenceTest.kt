package se.gustavkarlsson.track.sqlite

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import se.gustavkarlsson.track.Record

class CloseableRecordCursorSequenceTest {

	private val records = listOf(
		Record(1, "key1", 1, 1, "value1"),
		Record(2, "key2", 2, 2, "value2")
	)

	var cursorIndex = -1

	private val mockCursor = mock<RecordCursor> {
		var closed = false

		on { readExistingRecord() }.then {
			if (closed) error("Closed")
			records[cursorIndex]
		}

		on { tryGetRecordAndClose() }.then {
			if (closed) error("Closed")
			closed = true
			records.getOrNull(++cursorIndex)
		}

		on { checkForDataOrClose() }.then {
			if (closed) error("Closed")
			if (records.size > ++cursorIndex) {
				true
			} else {
				closed = true
				false
			}
		}

		on { close() }.then {
			closed = true
			Unit
		}

		on { isClosed }.then { closed }
	}

	private val underTest = CloseableRecordCursorSequence(mockCursor)

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
