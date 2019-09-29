package se.gustavkarlsson.track.sqlite

internal data class Selection(
    private val column: String,
    private val operator: Operator<*>,
    private val value: Any?
) {
    fun toSelectionSql(): String = "$column ${operator.toSql()} ?"

    fun toSelectionArgSql(): String = value.toValueSql()
}

private fun Any?.toValueSql(): String =
    when (this) {
        is Boolean -> if (this) "1" else "0"
        is Collection<*> -> this.joinToString(prefix = "(", postfix = ")") { it.toValueSql() }
        else -> toString()
    }

internal infix fun String.isEqualTo(value: Any): Selection =
    Selection(this, Operator.Equals, value)

internal infix fun String.isIn(value: Collection<Any>): Selection =
    Selection(this, Operator.In, value)

internal fun List<Selection>?.toSelectionSql(): String? =
    this
        ?.takeIf { it.isNotEmpty() }
        ?.joinToString(" AND ", transform = Selection::toSelectionSql)

internal fun List<Selection>?.toSelectionArgSql(): Array<String>? =
    this
        ?.takeIf { it.isNotEmpty() }
        ?.map(Selection::toSelectionArgSql)?.toTypedArray()

internal sealed class Operator<T> {
    object LessThan : Operator<Number>()
    object GreaterThan : Operator<Number>()
    object Equals : Operator<Any>()
    object NotEquals : Operator<Any>()
    object In : Operator<Any>()
}

private fun Operator<*>.toSql() =
    when (this) {
        Operator.LessThan -> "<"
        Operator.GreaterThan -> ">"
        Operator.Equals -> "="
        Operator.NotEquals -> "<>"
        Operator.In -> "IN"
    }
