package se.gustavkarlsson.nag.sqlite

internal data class Selection(
	private val column: String,
	private val operator: Operator,
	private val value: Any
) {
	val selectionSql: String
		get() = "$column ${operator.sql} ?"

	val selectionArgSql: String
		get() = value.toString()
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
