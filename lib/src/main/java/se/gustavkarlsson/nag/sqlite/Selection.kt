package se.gustavkarlsson.nag.sqlite

import se.gustavkarlsson.nag.Filter

internal data class Selection(
	private val column: String,
	private val operator: Operator,
	private val value: Any?
) {
	val selectionSql: String
		get() = "$column ${operator.sql} ?"

	val selectionArgSql: String
		get() = when (value) {
			is Boolean -> if (value) "1" else "0"
			else -> value.toString()
		}
}

internal enum class Operator(val sql: String) {
	LessThan("<"),
	GreaterThan(">"),
	Equals("="),
	NotEquals("<>")
}

internal fun List<Selection>.toSelectionSql(): String =
	map(Selection::selectionSql).joinToString(" AND ")

internal fun List<Selection>.toSelectionArgSql(): Array<String> =
	map(Selection::selectionArgSql).toTypedArray()

internal fun Filter.toSelection() =
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
			Table.COLUMN_VALUE,
			Operator.Equals,
			value
		)
		is Filter.ValueIsNot -> Selection(
			Table.COLUMN_VALUE,
			Operator.NotEquals,
			value
		)
	}
