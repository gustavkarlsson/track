package se.gustavkarlsson.track.sqlite

import android.database.Cursor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doReturnConsecutively
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import se.gustavkarlsson.track.Record
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

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
    fun toRecordSequence_may_not_be_iterated_twice() {
        val sequence = mockCursor.toRecordSequence()
        sequence.count()
        expectThrows<IllegalStateException> { sequence.count() }
    }

    @Test
    fun toRecordSequence_no_rows() {
        val sequence = mockCursor.toRecordSequence()

        expectThat(sequence.toList()).describedAs("sequence").isEmpty()
    }

    @Test
    fun toRecordSequence_one_row() {
        mockCursor.mockRecords(stubRecord1)

        val sequence = mockCursor.toRecordSequence()

        expectThat(sequence.toList()).describedAs("sequence").containsExactly(stubRecord1)
    }

    @Test
    fun toRecordSequence_multiple_rows() {
        mockCursor.mockRecords(stubRecord1, stubRecord2)

        val sequence = mockCursor.toRecordSequence()

        expectThat(sequence.toList()).describedAs("sequence").containsExactly(stubRecord1, stubRecord2)
    }

    @Test
    fun readOptionalRecord_with_no_rows() {
        val record = mockCursor.readOptionalRecord()

        expectThat(record).describedAs("record").isNull()
    }

    @Test
    fun readOptionalRecord_with_one_row() {
        mockCursor.mockRecords(stubRecord1)

        val record = mockCursor.readOptionalRecord()

        expectThat(record).describedAs("record").isEqualTo(stubRecord1)
    }

    @Test
    fun readOptionalRecord_with_two_rows() {
        mockCursor.mockRecords(stubRecord1, stubRecord2)

        val record = mockCursor.readOptionalRecord()

        expectThat(record).describedAs("record").isEqualTo(stubRecord1)
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
