package se.gustavkarlsson.track.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyArray
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import java.io.File
import org.junit.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

private typealias ToSelectionSql = List<Selection>.() -> String?
private typealias ToSelectionArgSql = List<Selection>.() -> Array<String>
private typealias ToContentValues = Map<String, Any?>.() -> ContentValues
private typealias DeleteDatabase = (File) -> Boolean

class SqliteTest {

    private val databaseName = "database.db"

    private val databaseVersion = 3

    private val tableName = "table"

    private val databasePath = "/path/to/db"

    private val createStatements = listOf("create 1", "create 2")

    private val rowToInsert = mapOf("a" to 1, "b" to true)

    private val selections = listOf(
        Selection("col1", Operator.In, 1),
        Selection("col2", Operator.Equals, true)
    )

    private val mockToSelectionSql = mock<ToSelectionSql> {
        on { invoke(any()) } doAnswer {
            it.arguments[0].toString()
        }
    }

    private val mockToSelectionArgSql = mock<ToSelectionArgSql> {
        on { invoke(any()) } doAnswer {
            it.arguments[0].toStringArray()
        }
    }

    private val mockContentValues = mock<ContentValues>()

    private val mockToContentValues = mock<ToContentValues> {
        on { invoke(any()) } doReturn mockContentValues
    }

    private val mockDeleteDatabase = mock<DeleteDatabase> {
        whenever(it.invoke(any())) doReturn true
    }

    private val mockContext = mock<Context>()

    private val mockCursor = mock<Cursor>()

    private val mockDb = mock<SQLiteDatabase> {
        on {
            query(
                any(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        } doAnswer {
            mockCursor
        }

        on { path } doReturn databasePath
    }

    private val sqlite = Sqlite(
        mockContext,
        databaseName,
        databaseVersion,
        tableName,
        createStatements,
        mockToSelectionSql,
        mockToSelectionArgSql,
        mockToContentValues,
        mockDeleteDatabase,
        { mockDb }
    )

    @Test
    fun create_database() {
        sqlite.onCreate(mockDb)

        inOrder(mockDb) {
            verify(mockDb).execSQL(createStatements[0])
            verify(mockDb).execSQL(createStatements[1])
        }
        verifyNoMoreInteractions(mockDb)
    }

    @Test
    fun upgrade_not_yet_supported() {
        expectThrows<IllegalStateException> {
            sqlite.onUpgrade(mockDb, 0, 1)
        }
    }

    @Test
    fun query_no_selection() {
        val selections = emptyList<Selection>()

        sqlite.query(selections) { }

        verify(mockDb).query(
            tableName,
            null,
            selections.toString(),
            selections.toStringArray(),
            null,
            null,
            null,
            null
        )
        verify(mockDb).close()
    }

    @Test
    fun query_with_selection_and_limit() {
        sqlite.query(selections, 5) { }

        verify(mockDb).query(
            tableName,
            null,
            selections.toString(),
            selections.toStringArray(),
            null,
            null,
            null,
            "5"
        )
        verify(mockDb).close()
    }

    @Test
    fun insert() {
        sqlite.insert(rowToInsert)

        verify(mockDb).insertOrThrow(tableName, null, mockContentValues)
        verify(mockDb).close()
    }

    @Test
    fun successful_upsert_calls_all_the_right_database_functions() {
        sqlite.upsert(selections, rowToInsert)

        verify(mockDb).delete(
            tableName, selections.toString(),
            selections.toStringArray()
        )
        verify(mockDb).insertOrThrow(tableName, null, mockContentValues)
        verify(mockDb).beginTransaction()
        verify(mockDb).setTransactionSuccessful()
        verify(mockDb).endTransaction()
        verify(mockDb).close()
    }

    @Test
    fun failed_upsert_still_closes_transaction_and_database() {
        whenever(mockDb.delete(any(), any(), anyArray())).thenThrow(RuntimeException())

        try {
            sqlite.upsert(selections, rowToInsert)
        } catch (e: Exception) {
            // ignore
        }

        verify(mockDb, never()).setTransactionSuccessful()
        verify(mockDb).endTransaction()
        verify(mockDb).close()
    }

    @Test
    fun upsert_with_no_existing_row_returns_true() {
        val replaced = sqlite.upsert(selections, rowToInsert)

        expectThat(replaced).describedAs("replaced").isFalse()
    }

    @Test
    fun upsert_with_1_existing_row_returns_true() {
        whenever(mockDb.delete(any(), anyOrNull(), anyArray())) doReturn 1

        val replaced = sqlite.upsert(selections, rowToInsert)

        expectThat(replaced).describedAs("replaced").isTrue()
    }

    @Test
    fun upsert_with_2_existing_rows_returns_true() {
        whenever(mockDb.delete(any(), anyOrNull(), anyArray())) doReturn 2

        val replaced = sqlite.upsert(selections, rowToInsert)

        expectThat(replaced).describedAs("replaced").isTrue()
    }

    @Test
    fun delete_calls_database() {
        sqlite.delete(selections)

        verify(mockDb).delete(
            tableName, selections.toString(),
            selections.toStringArray()
        )
        verify(mockDb).close()
    }

    @Test
    fun delete_with_no_deleted_rows() {
        val deletedCount = sqlite.delete(emptyList())

        expectThat(deletedCount).describedAs("deleted count").isEqualTo(0)
    }

    @Test
    fun delete_with_2_deleted_rows() {
        whenever(mockDb.delete(any(), anyOrNull(), anyArray())) doReturn 2

        val deletedCount = sqlite.delete(emptyList())

        expectThat(deletedCount).describedAs("deleted count").isEqualTo(2)
    }

    @Test
    fun deleteDatabase_success() {
        val deleted = sqlite.deleteDatabase()

        expectThat(deleted).describedAs("deleted").isTrue()
        verify(mockDeleteDatabase).invoke(File(databasePath))
    }

    @Test
    fun deleteDatabase_failure() {
        whenever(mockDeleteDatabase.invoke(any())) doReturn false

        val deleted = sqlite.deleteDatabase()

        expectThat(deleted).describedAs("deleted").isFalse()
        verify(mockDeleteDatabase).invoke(File(databasePath))
    }
}

private fun Any.toStringArray() =
    this.toString().split(" ").toTypedArray()
