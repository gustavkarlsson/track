package se.gustavkarlsson.nag.sqlite

import se.gustavkarlsson.nag.Field
import se.gustavkarlsson.nag.Filter
import se.gustavkarlsson.nag.Operator

internal data class Selection(
	private val column: String,
	private val operator: Operator<*>,
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

private val Operator<*>.sql
	get() = when (this) {
		Operator.LessThan -> "<"
		Operator.GreaterThan -> ">"
		Operator.Equals -> "="
		Operator.NotEquals -> "<>"
	}

internal fun List<Selection>.toSelectionSql(): String =
	map(Selection::selectionSql).joinToString(" AND ")

internal fun List<Selection>.toSelectionArgSql(): Array<String> =
	map(Selection::selectionArgSql).toTypedArray()

internal fun Filter<*>.toSelection(): Selection {
	val column = when (this.field) {
		is Field.Id -> Table.COLUMN_ID
		is Field.Timestamp -> Table.COLUMN_TIMESTAMP
		is Field.AppVersion -> Table.COLUMN_APP_VERSION
		is Field.Value -> Table.COLUMN_VALUE
	}
	return Selection(column, operator, argument)
}
