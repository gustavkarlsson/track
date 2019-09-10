package se.gustavkarlsson.track.sqlite

import se.gustavkarlsson.track.Field
import se.gustavkarlsson.track.Filter
import se.gustavkarlsson.track.Operator

internal data class Selection(
	private val column: String,
	private val operator: Operator<*>,
	private val value: Any?
) {
	fun toSelectionSql(): String
		= "$column ${operator.toSql()} ?"

	fun toSelectionArgSql(): String =
		when (value) {
			is Boolean -> if (value) "1" else "0"
			else -> value.toString()
		}
}

private fun Operator<*>.toSql() =
	when (this) {
		Operator.LessThan -> "<"
		Operator.GreaterThan -> ">"
		Operator.Equals -> "="
		Operator.NotEquals -> "<>"
	}

internal fun List<Selection>.toSelectionSql(): String =
	joinToString(" AND ", transform = Selection::toSelectionSql)

internal fun List<Selection>.toSelectionArgSql(): Array<String> =
	map(Selection::toSelectionArgSql).toTypedArray()

internal fun Filter<*>.toSelection(): Selection {
	val column = when (this.field) {
		is Field.Id -> Table.COLUMN_ID
		is Field.Timestamp -> Table.COLUMN_TIMESTAMP
		is Field.AppVersion -> Table.COLUMN_APP_VERSION
		is Field.Value -> Table.COLUMN_VALUE
	}
	return Selection(column, operator, argument)
}
