package se.gustavkarlsson.track.sqlite

import android.database.Cursor
import se.gustavkarlsson.track.*

internal class SqliteTrack(
    private val sqlite: Sqlite,
    private val appVersion: Long,
    private val getTimestamp: () -> Long = System::currentTimeMillis,
    private val toRecordCursor: Cursor.() -> RecordCursor = ::DefaultRecordCursor,
    private val toSelection: Filter<*>.() -> Selection = Filter<*>::toSelection
) : Track {
    override fun get(key: String): Record? {
        val selections =
            createKeySelection(key) + Selection(Table.COLUMN_SINGLETON, Operator.Equals, true)
        val cursor = sqlite.query(selections, limit = 1).toRecordCursor()
        return cursor.tryGetRecordAndClose()
    }

    override fun set(key: String, value: String) {
        val selections =
            createKeySelection(key) + Selection(Table.COLUMN_SINGLETON, Operator.Equals, true)
        sqlite.upsert(selections, createRow(key, value, true))
    }

    override fun query(
        key: String,
        order: Order,
        filters: FiltersBuilder.() -> Unit
    ): CloseableSequence<Record> {
        val selections = createKeySelection(key) + filters.toSelections()
        val orderBy = when (order) {
            Order.Ascending -> OrderBy.Ascending(Table.COLUMN_ID)
            Order.Descending -> OrderBy.Descending(Table.COLUMN_ID)
        }
        val cursor = sqlite.query(selections, orderBy).toRecordCursor()
        return CloseableRecordCursorSequence(cursor)
    }

    override fun add(key: String, value: String) {
        sqlite.insert(createRow(key, value, false))
    }

    private fun createRow(key: String, value: String, singleton: Boolean): Map<String, Any> =
        mapOf(
            Table.COLUMN_SINGLETON to singleton,
            Table.COLUMN_KEY to key,
            Table.COLUMN_TIMESTAMP to getTimestamp(),
            Table.COLUMN_APP_VERSION to appVersion,
            Table.COLUMN_VALUE to value
        )

    override fun remove(id: Long): Boolean {
        val selections = listOf(Selection(Table.COLUMN_ID, Operator.Equals, id))
        return sqlite.delete(selections) > 0
    }

    override fun remove(key: String, filters: FiltersBuilder.() -> Unit): Int {
        val selections = createKeySelection(key) + filters.toSelections()
        return sqlite.delete(selections)
    }

    override fun deleteDatabase() = sqlite.deleteDatabase()

    private fun createKeySelection(key: String) =
        listOf(Selection(Table.COLUMN_KEY, Operator.Equals, key))

    private fun (FiltersBuilder.() -> Unit).toSelections(): List<Selection> =
        FiltersBuilder()
            .apply(this)
            .build()
            .map(toSelection)
}
