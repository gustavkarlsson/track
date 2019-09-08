package se.gustavkarlsson.nag.sqlite

import android.database.Cursor
import se.gustavkarlsson.nag.*

internal class SqliteNag(
	private val helper: Helper,
	private val getTimestamp: () -> Long = System::currentTimeMillis,
	private val appVersion: Int = BuildConfig.VERSION_CODE,
	private val tryGetRecordAndClose: Cursor.() -> Record? = { tryGetRecordAndClose(Cursor::readExistingRecord) }
) : Nag {
	override fun getSingle(key: String): Record? {
		val selections = listOf(
			Selection(Table.COLUMN_KEY, Operator.Equals, key),
			Selection(Table.COLUMN_SINGLETON, Operator.Equals, 1)
		)
		val cursor = helper.query(selections, limit = 1)
		return cursor.tryGetRecordAndClose()
	}

	override fun setSingle(key: String, value: String) {
		val selections = listOf(
			Selection(Table.COLUMN_KEY, Operator.Equals, key),
			Selection(Table.COLUMN_SINGLETON, Operator.Equals, 1)
		)
		helper.upsert(selections, createRow(key, value, true))
	}

	override fun query(
		key: String,
		order: Order,
		filters: Filters.() -> Unit
	): CloseableSequence<Record> {
		val keySelection = listOf(Selection(Table.COLUMN_KEY, Operator.Equals, key))
		val filterSelections = createFilterSelections(filters)
		val selections = keySelection + filterSelections
		val cursor = helper.query(selections, order)
		return CloseableRecordCursorSequence(cursor)
	}

	override fun add(key: String, value: String) {
		helper.insert(createRow(key, value, false))
	}

	private fun createRow(key: String, value: String, singleton: Boolean): Map<String, Any> =
		mapOf(
			Table.COLUMN_SINGLETON to if (singleton) 1 else 0,
			Table.COLUMN_KEY to key,
			Table.COLUMN_TIMESTAMP to getTimestamp(),
			Table.COLUMN_APP_VERSION to appVersion,
			Table.COLUMN_VALUE to value
		)

	override fun remove(id: Long) {
		helper.delete(listOf(Selection(Table.COLUMN_ID, Operator.Equals, id)))
	}

	override fun remove(key: String, filters: Filters.() -> Unit) {
		val keySelection = listOf(Selection(Table.COLUMN_KEY, Operator.Equals, key))
		val filterSelections = createFilterSelections(filters)
		val selections = keySelection + filterSelections
		helper.delete(selections)
	}

	override fun clearDatabase() = helper.deleteDatabase()
}

private fun createFilterSelections(filters: Filters.() -> Unit): List<Selection> =
	Filters().apply(filters).filters.map(Filter::toSelection)

private fun Filter.toSelection() =
	when (this) {
		is Filter.Before -> Selection(
			Table.COLUMN_TIMESTAMP,
			Operator.LessThan,
			timestamp
		)
		is Filter.After -> Selection(
			Table.COLUMN_TIMESTAMP,
			Operator.GreaterThan,
			timestamp
		)
		is Filter.VersionIs -> Selection(
			Table.COLUMN_APP_VERSION,
			Operator.Equals,
			version
		)
		is Filter.VersionIsNot -> Selection(
			Table.COLUMN_APP_VERSION,
			Operator.NotEquals,
			version
		)
		is Filter.VersionLessThan -> Selection(
			Table.COLUMN_APP_VERSION,
			Operator.LessThan,
			version
		)
		is Filter.VersionGreaterThan -> Selection(
			Table.COLUMN_APP_VERSION,
			Operator.GreaterThan,
			version
		)
		is Filter.ValueIs -> Selection(
			Table.COLUMN_APP_VERSION,
			Operator.Equals,
			value
		)
	}
