package se.gustavkarlsson.track.sqlite

internal data class Selection(
    val column: String,
    val operator: Operator<*>,
    val value: Any?
)

internal fun List<Selection>.toSelectionSql(): String? =
    takeIf { it.isNotEmpty() }
        ?.joinToString(" AND ", transform = Selection::toSelectionSql)

private fun Selection.toSelectionSql(): String =
    if (operator is Operator.In) {
        val values = when (value) {
            is Sequence<*> -> value.toList()
            is Iterable<*> -> value.toList()
            else -> throw IllegalArgumentException("Unsupported type: ${value?.javaClass}")
        }
        val valuesString =
            values.joinToString(prefix = "(", postfix = ")") { it.toValueSql()!! }
        "$column ${operator.toSql()} $valuesString"
    } else {
        "$column ${operator.toSql()} ?"
    }

internal fun List<Selection>.toSelectionArgSql(): Array<String> =
    map(Selection::value)
        .mapNotNull(Any?::toValueSql)
        .toTypedArray()

private fun Any?.toValueSql(): String? =
    when (this) {
        is Boolean -> if (this) "1" else "0"
        is Collection<*> -> null
        else -> toString()
    }

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
