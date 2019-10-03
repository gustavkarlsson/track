package se.gustavkarlsson.track.sqlite

import android.database.Cursor
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import se.gustavkarlsson.track.Record

private typealias ReadOptionalRecord = Cursor.() -> Record?
private typealias ToRecordSequence = Cursor.() -> Sequence<Record>

class SqliteTrackTest {

    private val key = "key"

    private val record = Record(1, key, 2, 3, "value")

    private val appVersion = 5L

    private val timestamp = 5000L

    private val mockSqlite = mock<Sqlite>()

    private val mockReadOptionalRecord = mock<ReadOptionalRecord>()

    private val mockToRecordSequence = mock<ToRecordSequence>()

    private val sqliteTrack = SqliteTrack(
        mockSqlite,
        appVersion,
        ::timestamp,
        mockReadOptionalRecord,
        mockToRecordSequence
    )

    @Test
    fun `get no record`() {
        mockQuery<Record?>(null)

        val result = sqliteTrack.get(key)

        assertThat(result).isNull()
    }

    @Test
    fun `get existing record`() {
        mockQuery(record)

        val result = sqliteTrack.get(key)

        assertThat(result).isEqualTo(record)
    }

    @Test
    fun `get passes correct selections`() {
        val expected = listOf(
            Selection(Table.COLUMN_KEY, Operator.Equals, key),
            Selection(Table.COLUMN_SINGLETON, Operator.Equals, true)
        )

        sqliteTrack.get(key)

        val capturedSelections = argumentCaptor<List<Selection>> {
            verify(mockSqlite).query(capture(), any(), any())
        }.firstValue
        assertThat(capturedSelections).isEqualTo(expected)
    }

    private inline fun <reified T> mockQuery(returnValue: T) {
        whenever(mockSqlite.query(any(), any(), any<(Cursor) -> T>())) doReturn returnValue
    }
}
