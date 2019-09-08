package se.gustavkarlsson.nag.sqlite

import android.database.Cursor
import se.gustavkarlsson.nag.*

private val table = MultiRecordTable

internal class SqliteMultiRecords(
	private val helper: Helper,
	private val getTimestamp: () -> Long = System::currentTimeMillis,
	private val appVersion: Int = BuildConfig.VERSION_CODE
) : MultiRecords {

	// FIXME order
	override fun query(
		key: String,
		filters: MultiRecords.Filters.() -> Unit
	): CloseableSequence<MultiRecord> {
		val filterSelections =
			MultiRecords.Filters().apply(filters).filters.map(MultiQueryFilter::toSelection)
		val selections =
			listOf(Selection(table.COLUMN_KEY, Operator.Equals, key)) + filterSelections
		val cursor = helper.query(table.NAME, selections)
		return CloseableMultiRecordSequence(cursor)
	}

	override fun add(key: String, value: String) {
		val values = mapOf(
			table.COLUMN_KEY to key,
			table.COLUMN_TIMESTAMP to getTimestamp(),
			table.COLUMN_APP_VERSION to appVersion,
			table.COLUMN_VALUE to value
		)
		helper.insert(table.NAME, values)
	}

	override fun remove(
		key: String,
		filters: MultiRecords.Filters.() -> Unit
	) {
		val filterSelections =
			MultiRecords.Filters().apply(filters).filters.map(MultiQueryFilter::toSelection)
		val selections =
			listOf(Selection(table.COLUMN_KEY, Operator.Equals, key)) + filterSelections
		helper.delete(table.NAME, selections)
	}

	override fun remove(id: Long) {
		helper.delete(table.NAME, listOf(Selection(table.COLUMN_ID, Operator.Equals, id)))
	}
}

private fun MultiQueryFilter.toSelection(): Selection =
	when (this) {
		is MultiQueryFilter.Before -> Selection(
			table.COLUMN_TIMESTAMP,
			Operator.LessThan,
			timestamp
		)
		is MultiQueryFilter.After -> Selection(
			table.COLUMN_TIMESTAMP,
			Operator.GreaterThan,
			timestamp
		)
		is MultiQueryFilter.VersionIs -> Selection(
			table.COLUMN_APP_VERSION,
			Operator.Equals,
			version
		)
		is MultiQueryFilter.VersionIsNot -> Selection(
			table.COLUMN_APP_VERSION,
			Operator.NotEquals,
			version
		)
		is MultiQueryFilter.VersionBefore -> Selection(
			table.COLUMN_APP_VERSION,
			Operator.LessThan,
			version
		)
		is MultiQueryFilter.VersionAfter -> Selection(
			table.COLUMN_APP_VERSION,
			Operator.GreaterThan,
			version
		)
		is MultiQueryFilter.ValueIs -> Selection(table.COLUMN_APP_VERSION, Operator.Equals, value)
	}

private class CloseableMultiRecordSequence(
	cursor: Cursor
) : CloseableCursorSequence<MultiRecord>(cursor) {
	override fun Cursor.readEntity(): MultiRecord = MultiRecord(
		id = getLong(getColumnIndex(table.COLUMN_ID)),
		key = getString(getColumnIndex(table.COLUMN_KEY)),
		timestamp = getLong(getColumnIndex(table.COLUMN_TIMESTAMP)),
		appVersion = getInt(getColumnIndex(table.COLUMN_APP_VERSION)),
		value = getString(getColumnIndex(table.COLUMN_VALUE))
	)
}

