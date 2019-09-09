package se.gustavkarlsson.nag.sqlite

import android.database.Cursor
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import se.gustavkarlsson.nag.Record

class CursorTryGetRecordAndCloseTest {

	private val record = Record(1, "key1", 1, 1, "value1")

	private val readExistingRecord: Cursor.() -> Record = { record }

	var hasNext = true

	private val mockCursor = mock<Cursor> {
		on { moveToNext() }.then {
			hasNext
		}
	}

	@Test
	fun `value is read`() {
		val result = mockCursor.tryGetRecordAndClose(readExistingRecord)

		assertThat(result).isEqualTo(record)
	}

	@Test
	fun `null is read`() {
		hasNext = false

		val result = mockCursor.tryGetRecordAndClose(readExistingRecord)

		assertThat(result).isNull()
	}

	@Test
	fun `cursor is closed`() {
		mockCursor.tryGetRecordAndClose(readExistingRecord)

		verify(mockCursor).close()
	}
}
