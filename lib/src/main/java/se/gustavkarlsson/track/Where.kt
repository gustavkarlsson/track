package se.gustavkarlsson.track

class FiltersBuilder internal constructor() {
	private val filters = mutableListOf<Filter<*>>()

	val id: Field<Long> get() = Field.Id
	val timestamp: Field<Long> get() = Field.Timestamp
	val appVersion: Field<Long> get() = Field.AppVersion
	val value: Field<String> get() = Field.Value

	infix fun <T : Number> Field<T>.isLessThan(argument: T) =
		filters.add(Filter(this, Operator.LessThan, argument))

	infix fun <T : Number> Field<T>.isGreaterThan(argument: T) =
		filters.add(Filter(this, Operator.GreaterThan, argument))

	infix fun <T : Any> Field<T>.isEqualTo(argument: T) =
		filters.add(Filter(this, Operator.Equals, argument))

	infix fun <T : Any> Field<T>.isNotEqualTo(argument: T) =
		filters.add(Filter(this, Operator.NotEquals, argument))

	internal fun build(): List<Filter<*>> = filters.toList()
}

sealed class Field<T> {
	internal object Id : Field<Long>()
	internal object Timestamp : Field<Long>()
	internal object AppVersion : Field<Long>()
	internal object Value : Field<String>()
}

internal data class Filter<T>(
	val field: Field<T>,
	val operator: Operator<in T>,
	val argument: T
)

internal sealed class Operator<T> {
	object LessThan : Operator<Number>()
	object GreaterThan : Operator<Number>()
	object Equals : Operator<Any>()
	object NotEquals : Operator<Any>()
}
