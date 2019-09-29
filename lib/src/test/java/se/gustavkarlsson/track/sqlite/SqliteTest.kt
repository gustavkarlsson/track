package se.gustavkarlsson.track.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import assertk.assertThat
import assertk.assertions.isNotNull
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Before
import org.junit.Test

private typealias ToSelectionSql = List<Selection>.() -> String?
private typealias ToSelectionArgSql = List<Selection>.() -> Array<String>
private typealias ToContentValues = Map<String, Any?>.() -> ContentValues

class SqliteTest {

    private val databaseName = "database.db"

    private val databaseVersion = 3

    private val tableName = "table"

    private val createStatements = listOf("create 1", "create 2")

    private val mockToSelectionSql = mock<ToSelectionSql> {
        on { it.invoke(any()) } doAnswer {
            it.arguments[0].toString()
        }
    }

    private val mockToSelectionArgSql = mock<ToSelectionArgSql> {
        on { it.invoke(any()) } doAnswer {
            it.arguments[0].toStringArray()
        }
    }

    private val mockToContentValues = mock<ToContentValues>()

    private val mockContext = mock<Context>()

    private val mockCursor = mock<Cursor>()

    private val mockDb = mock<SQLiteDatabase> {
        on {
            it.query(
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
    }

    private val sqlite = Sqlite(
        mockContext,
        databaseName,
        databaseVersion,
        tableName,
        createStatements,
        mockToSelectionSql,
        mockToSelectionArgSql,
        mockToContentValues
    )

    @Before
    fun setUp() {
        sqlite.getReadableDb = { mockDb }
        sqlite.getWritableDb = { mockDb }
    }

    @Test
    fun `create database`() {
        sqlite.onCreate(mockDb)
        inOrder(mockDb) {
            verify(mockDb).execSQL(createStatements[0])
            verify(mockDb).execSQL(createStatements[1])
        }
        verifyNoMoreInteractions(mockDb)
    }

    @Test(expected = IllegalStateException::class)
    fun `upgrade not yet supported`() {
        sqlite.onUpgrade(mockDb, 0, 1)
    }

    @Test
    fun `query no selection`() {
        val selections = emptyList<Selection>()
        val result = sqlite.query(selections) {
            it.toString()
        }
        assertThat(result).isNotNull()
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
        verifyNoMoreInteractions(mockDb)
    }

    @Test
    fun `query with selection and limit`() {
        val selections = listOf(
            Selection("col1", Operator.In, 1),
            Selection("col2", Operator.Equals, true)
        )
        val result = sqlite.query(selections, 5) {
            it.toString()
        }
        assertThat(result).isNotNull()
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
        verifyNoMoreInteractions(mockDb)
    }
}

private fun Any.toStringArray() =
    this.toString().split(" ").toTypedArray()
