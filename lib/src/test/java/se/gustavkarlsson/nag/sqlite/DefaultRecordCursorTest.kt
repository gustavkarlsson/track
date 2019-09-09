package se.gustavkarlsson.nag.sqlite

import android.database.Cursor
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import se.gustavkarlsson.nag.Record
import java.lang.Exception

class DefaultRecordCursorTest {

	private val record = Record(1, "key1", 1, 1, "value1")

	private val databaseRow = with(record) {
		listOf(
			Table.COLUMN_ID to id,
			Table.COLUMN_KEY to key,
			Table.COLUMN_TIMESTAMP to timestamp,
			Table.COLUMN_APP_VERSION to appVersion,
			Table.COLUMN_VALUE to value
		)
	}

	private var hasNext = true

	private val mockCursor = mock<Cursor> {
		var closed = false

		on { moveToNext() }.then {
			hasNext
		}

		on { close() }.then {
			closed = true
			Unit
		}

		on { isClosed }.then { closed }
		
		on { getColumnIndexOrThrow(any()) }.then { invocation ->
			if (closed) error("Closed")
			val column = invocation.arguments[0] as String
			databaseRow
				.indexOfFirst { it.first == column }
				.takeIf { it > -1 }
				?: error("Couldn't find column index of $column")
		}

		on { getShort(any()) }.then {
			databaseRow[it.arguments[0] as Int].second
		}

		on { getInt(any()) }.then {
			databaseRow[it.arguments[0] as Int].second
		}

		on { getLong(any()) }.then { 
			databaseRow[it.arguments[0] as Int].second
		}

		on { getFloat(any()) }.then { 
			databaseRow[it.arguments[0] as Int].second
		}

		on { getDouble(any()) }.then { 
			databaseRow[it.arguments[0] as Int].second
		}

		on { getBlob(any()) }.then { 
			databaseRow[it.arguments[0] as Int].second
		}

		on { getString(any()) }.then { 
			databaseRow[it.arguments[0] as Int].second
		}
	}

	private val underTest = DefaultRecordCursor(mockCursor)

	@Test
	fun `tryGetRecordAndClose returns value when cursor has more data`() {
		val result = underTest.tryGetRecordAndClose()

		assertThat(result).isEqualTo(record)
	}

	@Test
	fun `tryGetRecordAndClose returns null when cursor does not have more data`() {
		hasNext = false

		val result = underTest.tryGetRecordAndClose()

		assertThat(result).isNull()
	}

	@Test
	fun `tryGetRecordAndClose closes cursor`() {
		underTest.tryGetRecordAndClose()

		verify(mockCursor).close()
	}

	@Test
	fun `readExistingRecord success`() {
		val result = underTest.readExistingRecord()

		assertThat(result).isEqualTo(record)
	}

	@Test
	fun `checkForDataOrClose has data`() {
		val result = underTest.checkForDataOrClose()

		assertThat(result).isTrue()
	}

	@Test
	fun `checkForDataOrClose does not have data`() {
		hasNext = false

		val result = underTest.checkForDataOrClose()

		assertThat(result).isFalse()
	}

	@Test
	fun `checkForDataOrClose without data closes`() {
		hasNext = false

		underTest.checkForDataOrClose()

		verify(mockCursor).close()
	}
}
