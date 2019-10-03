package se.gustavkarlsson.track.sqlite

import android.database.Cursor
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import se.gustavkarlsson.track.Record

private typealias Selector<T> = Cursor.() -> T
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
        mockQuery(mockReadOptionalRecord, null)

        val result = sqliteTrack.get(key)

        assertThat(result).isNull()
    }

    @Test
    fun `get existing record`() {
        mockQuery(mockReadOptionalRecord, record)

        val result = sqliteTrack.get(key)

        assertThat(result).isEqualTo(record)
    }

    @Test
    fun `get passes correct selections`() {
        sqliteTrack.get(key)

        val capturedSelections = argumentCaptor<List<Selection>> {
            verify(mockSqlite).query(capture(), any(), any())
        }.firstValue
        val expected = listOf(
            Selection(Table.COLUMN_KEY, Operator.Equals, key),
            Selection(Table.COLUMN_SINGLETON, Operator.Equals, true)
        )
        assertThat(capturedSelections).isEqualTo(expected)
    }

    @Test
    fun `set passes correct arguments`() {
        sqliteTrack.set(key, "value")

        val expectedSelections = listOf(
            Selection(Table.COLUMN_KEY, Operator.Equals, key),
            Selection(Table.COLUMN_SINGLETON, Operator.Equals, true)
        )
        val expectedRow = mapOf(
            Table.COLUMN_SINGLETON to true,
            Table.COLUMN_KEY to key,
            Table.COLUMN_TIMESTAMP to timestamp,
            Table.COLUMN_APP_VERSION to appVersion,
            Table.COLUMN_VALUE to "value"
        )
        verify(mockSqlite).upsert(expectedSelections, expectedRow)
    }

    @Test
    fun `set with existing replaced`() {
        whenever(mockSqlite.upsert(any(), any())) doReturn true

        val replaced = sqliteTrack.set(key)

        assertThat(replaced).isTrue()
    }

    @Test
    fun `set with existing not replaced`() {
        whenever(mockSqlite.upsert(any(), any())) doReturn false

        val replaced = sqliteTrack.set(key)

        assertThat(replaced).isFalse()
    }

    @Test
    fun `query to list returns correct value`() {
        mockQuery(mockToRecordSequence, sequenceOf(record))

        val result = sqliteTrack.query(key)

        assertThat(result).containsExactly(record)
    }

    @Test
    fun `query with selector returns correct value`() {
        mockQuery(mockToRecordSequence, sequenceOf(record, record))

        val result = sqliteTrack.query(key) { it.count() }

        assertThat(result).isEqualTo(2)
    }

    @Test
    fun add() {
        sqliteTrack.add(key, "value")

        val expectedRow = mapOf(
            Table.COLUMN_SINGLETON to false,
            Table.COLUMN_KEY to key,
            Table.COLUMN_TIMESTAMP to timestamp,
            Table.COLUMN_APP_VERSION to appVersion,
            Table.COLUMN_VALUE to "value"
        )
        verify(mockSqlite).insert(expectedRow)
    }

    @Test
    fun `remove by id`() {
        whenever(mockSqlite.delete(any())) doReturn 1

        val removed = sqliteTrack.remove(5)

        assertThat(removed).isTrue()
        val expectedSelections = listOf(Selection(Table.COLUMN_ID, Operator.Equals, 5L))
        verify(mockSqlite).delete(expectedSelections)
    }

    @Test
    fun `remove by key`() {
        whenever(mockSqlite.delete(any())) doReturn 5

        val removed = sqliteTrack.remove(key)

        assertThat(removed).isEqualTo(5)
        val expectedSelections = listOf(Selection(Table.COLUMN_KEY, Operator.Equals, key))
        verify(mockSqlite).delete(expectedSelections)
    }

    @Test
    fun `remove by filter`() {
        val querySequence = sequenceOf(
            record.copy(id = 1),
            record.copy(id = 7),
            record.copy(id = 6)
        )
        mockQuery(mockToRecordSequence, querySequence)
        whenever(mockSqlite.delete(any())) doReturn 2

        val removed = sqliteTrack.remove { it.id > 5 }

        assertThat(removed).isEqualTo(2)
        val expectedSelections = listOf(Selection(Table.COLUMN_ID, Operator.In, listOf(7L, 6L)))
        verify(mockSqlite).delete(expectedSelections)
    }

    @Test
    fun clear() {
        whenever(mockSqlite.deleteDatabase()) doReturn true

        val success = sqliteTrack.clear()

        assertThat(success).isTrue()
        verify(mockSqlite).deleteDatabase()
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> mockQuery(block: Selector<T>, returnValue: T) {
        val mockCursor = mock<Cursor>()
        whenever(block.invoke(mockCursor)) doReturn returnValue
        whenever(mockSqlite.query(any(), anyOrNull(), any<Selector<T>>())) doAnswer {
            val function = it.arguments[2] as Selector<T>
            function(mockCursor)
        }
    }
}
