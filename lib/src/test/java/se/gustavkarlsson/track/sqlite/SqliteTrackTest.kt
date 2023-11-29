@file:OptIn(ExperimentalCoroutinesApi::class)

package se.gustavkarlsson.track.sqlite

import android.database.Cursor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import se.gustavkarlsson.track.Order
import se.gustavkarlsson.track.Record
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue

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
    fun `get no record`() = runTest {
        mockQuery(mockReadOptionalRecord, null)

        val result = sqliteTrack.get(key)

        expectThat(result).describedAs("record").isNull()
    }

    @Test
    fun `get existing record`() = runTest {
        mockQuery(mockReadOptionalRecord, record)

        val result = sqliteTrack.get(key)

        expectThat(result).describedAs("record").isEqualTo(record)
    }

    @Test
    fun `get passes correct selections`() = runTest {
        sqliteTrack.get(key)

        val capturedSelections = argumentCaptor<List<Selection>> {
            verify(mockSqlite).query(capture(), anyOrNull(), any(), any())
        }.firstValue
        val expected = listOf(
            Selection(Table.COLUMN_KEY, Operator.Equals, key),
            Selection(Table.COLUMN_SINGLETON, Operator.Equals, true)
        )
        expectThat(capturedSelections).describedAs("selections").isEqualTo(expected)
    }

    @Test
    fun `set passes correct arguments`() = runTest {
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
    fun `set with existing replaced`() = runTest {
        whenever(mockSqlite.upsert(any(), any())) doReturn true

        val replaced = sqliteTrack.set(key)

        expectThat(replaced).describedAs("replaced").isTrue()
    }

    @Test
    fun `set with existing not replaced`() = runTest {
        whenever(mockSqlite.upsert(any(), any())) doReturn false

        val replaced = sqliteTrack.set(key)

        expectThat(replaced).describedAs("replaced").isFalse()
    }

    @Test
    fun `query to list returns correct value`() = runTest {
        mockQuery(mockToRecordSequence, sequenceOf(record))

        val results = sqliteTrack.query(key)

        expectThat(results).describedAs("results").containsExactly(record)
    }

    @Test
    fun `query with selector returns correct value`() = runTest {
        mockQuery(mockToRecordSequence, sequenceOf(record, record))

        val result = sqliteTrack.query(key, Order.InsertionAscending) { it.count() }

        expectThat(result).describedAs("result").isEqualTo(2)
    }

    @Test
    fun add() = runTest {
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
    fun `remove by id`() = runTest {
        whenever(mockSqlite.delete(any())) doReturn 1

        val removed = sqliteTrack.remove(5)

        expectThat(removed).describedAs("removed").isTrue()
        val expectedSelections = listOf(Selection(Table.COLUMN_ID, Operator.Equals, 5L))
        verify(mockSqlite).delete(expectedSelections)
    }

    @Test
    fun `remove by key`() = runTest {
        whenever(mockSqlite.delete(any())) doReturn 5

        val removed = sqliteTrack.remove(key)

        expectThat(removed).describedAs("removed").isEqualTo(5)
        val expectedSelections = listOf(Selection(Table.COLUMN_KEY, Operator.Equals, key))
        verify(mockSqlite).delete(expectedSelections)
    }

    @Test
    fun `remove by filter`() = runTest {
        val querySequence = sequenceOf(
            record.copy(id = 1),
            record.copy(id = 7),
            record.copy(id = 6)
        )
        mockQuery(mockToRecordSequence, querySequence)
        whenever(mockSqlite.delete(any())) doReturn 2

        val removedCount = sqliteTrack.remove { it.id > 5 }

        expectThat(removedCount).describedAs("removed count").isEqualTo(2)
        val expectedSelections = listOf(Selection(Table.COLUMN_ID, Operator.In, listOf(7L, 6L)))
        verify(mockSqlite).delete(expectedSelections)
    }

    @Test
    fun clear() = runTest {
        whenever(mockSqlite.deleteDatabase()) doReturn true

        val cleared = sqliteTrack.clear()

        expectThat(cleared).describedAs("cleared").isTrue()
        verify(mockSqlite).deleteDatabase()
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> mockQuery(block: Selector<T>, returnValue: T) {
        val mockCursor = mock<Cursor>()
        whenever(block.invoke(mockCursor)) doReturn returnValue
        mockSqlite.stub {
            onBlocking { mockSqlite.query(any(), anyOrNull(), anyOrNull(), any<Selector<T>>()) } doAnswer {
                val function = it.arguments[3] as Selector<T>
                function(mockCursor)
            }
        }
    }
}
