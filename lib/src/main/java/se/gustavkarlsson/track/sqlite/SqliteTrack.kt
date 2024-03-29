package se.gustavkarlsson.track.sqlite

import android.database.Cursor
import se.gustavkarlsson.track.Order
import se.gustavkarlsson.track.Record
import se.gustavkarlsson.track.Track

internal class SqliteTrack(
    private val sqlite: Sqlite,
    private val appVersion: Long,
    private val getTimestamp: () -> Long = System::currentTimeMillis,
    private val readOptionalRecord: Cursor.() -> Record? = Cursor::readOptionalRecord,
    private val toRecordSequence: Cursor.() -> Sequence<Record> = Cursor::toRecordSequence
) : Track {
    override suspend fun get(key: String): Record? {
        val selections = listOf(
            Table.COLUMN_KEY isEqualTo key,
            Table.COLUMN_SINGLETON isEqualTo true
        )
        return sqlite.query(selections, limit = 1, block = readOptionalRecord)
    }

    override suspend fun set(key: String, value: String): Boolean {
        val selections = listOf(
            Table.COLUMN_KEY isEqualTo key,
            Table.COLUMN_SINGLETON isEqualTo true
        )
        return sqlite.upsert(selections, createRow(key, value, true))
    }

    override suspend fun <T> query(key: String, order: Order, selector: (Sequence<Record>) -> T): T {
        val selections = listOf(Table.COLUMN_KEY isEqualTo key)
        return sqlite.query(selections, order) { cursor ->
            val recordSequence = cursor.toRecordSequence()
            selector(recordSequence)
        }
    }

    override suspend fun add(key: String, value: String) = sqlite.insert(createRow(key, value, false))

    private fun createRow(key: String, value: String, singleton: Boolean): Map<String, Any> =
        mapOf(
            Table.COLUMN_SINGLETON to singleton,
            Table.COLUMN_KEY to key,
            Table.COLUMN_TIMESTAMP to getTimestamp(),
            Table.COLUMN_APP_VERSION to appVersion,
            Table.COLUMN_VALUE to value
        )

    override suspend fun remove(id: Long): Boolean {
        val selections = listOf(Table.COLUMN_ID isEqualTo id)
        return sqlite.delete(selections) > 0
    }

    override suspend fun remove(key: String): Int {
        val selections = listOf(Table.COLUMN_KEY isEqualTo key)
        return sqlite.delete(selections)
    }

    override suspend fun remove(filter: (Record) -> Boolean): Int {
        val ids = sqlite.query(emptyList()) {
            it.toRecordSequence()
                .filter(filter)
                .map(Record::id)
                .toList()
        }
        val selections = listOf(Table.COLUMN_ID isIn ids)
        return sqlite.delete(selections)
    }

    override suspend fun clear() = sqlite.deleteDatabase()

    override fun disconnect() = sqlite.close()
}

private infix fun String.isEqualTo(value: Any): Selection =
    Selection(this, Operator.Equals, value)

private infix fun String.isIn(value: Collection<Any>): Selection =
    Selection(this, Operator.In, value)
