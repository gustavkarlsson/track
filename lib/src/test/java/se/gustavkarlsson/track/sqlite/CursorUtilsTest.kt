package se.gustavkarlsson.track.sqlite

import android.database.Cursor
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doReturnConsecutively
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import se.gustavkarlsson.track.Record

class CursorUtilsTest {

    private val stubRecord1 = Record(1, "key", 2, 3, "value")
    private val stubRecord2 = Record(2, "key2", 3, 4, "value2")

    private val mockCursor = mock<Cursor> {
        on(it.moveToNext()) doReturn false
        on(it.getColumnIndexOrThrow(Table.COLUMN_ID)) doReturn 0
        on(it.getColumnIndexOrThrow(Table.COLUMN_KEY)) doReturn 1
        on(it.getColumnIndexOrThrow(Table.COLUMN_TIMESTAMP)) doReturn 2
        on(it.getColumnIndexOrThrow(Table.COLUMN_APP_VERSION)) doReturn 3
        on(it.getColumnIndexOrThrow(Table.COLUMN_VALUE)) doReturn 4
    }

    @Test
    fun `toRecordSequence no rows`() {
        val sequence = mockCursor.toRecordSequence()

        assertThat(sequence.toList()).isEmpty()
    }

    @Test
    fun `toRecordSequence one row`() {
        mockCursor.mockRecords(stubRecord1)

        val sequence = mockCursor.toRecordSequence()

        assertThat(sequence.toList()).containsExactly(stubRecord1)
    }

    @Test
    fun `toRecordSequence multiple rows`() {
        mockCursor.mockRecords(stubRecord1, stubRecord2)

        val sequence = mockCursor.toRecordSequence()

        assertThat(sequence.toList()).containsExactly(stubRecord1, stubRecord2)
    }

    @Test
    fun `readOptionalRecord with no rows`() {
        val record = mockCursor.readOptionalRecord()

        assertThat(record).isNull()
    }

    @Test
    fun `readOptionalRecord with one row`() {
        mockCursor.mockRecords(stubRecord1)

        val record = mockCursor.readOptionalRecord()

        assertThat(record).isEqualTo(stubRecord1)
    }

    @Test
    fun `readOptionalRecord with two rows`() {
        mockCursor.mockRecords(stubRecord1, stubRecord2)

        val record = mockCursor.readOptionalRecord()

        assertThat(record).isEqualTo(stubRecord1)
    }
}

private fun Cursor.mockRecords(vararg records: Record) {
    whenever(this.getLong(0)) doReturnConsecutively records.map(Record::id)
    whenever(this.getString(1)) doReturnConsecutively records.map(Record::key)
    whenever(this.getLong(2)) doReturnConsecutively records.map(Record::timestamp)
    whenever(this.getLong(3)) doReturnConsecutively records.map(Record::appVersion)
    whenever(this.getString(4)) doReturnConsecutively records.map(Record::value)
    whenever(this.moveToNext()) doReturnConsecutively records.map { true } + false
}
