package se.gustavkarlsson.nag

class FiltersConfig internal constructor() {
	private val internalFilters = mutableListOf<Filter>()
	internal val filters: List<Filter>
		get() = internalFilters.toList()

	fun before(timestamp: Long) = internalFilters.add(Filter.Before(timestamp))
	fun after(timestamp: Long) = internalFilters.add(Filter.After(timestamp))
	fun versionIs(version: Long) = internalFilters.add(Filter.VersionIs(version))
	fun versionIsNot(version: Long) = internalFilters.add(Filter.VersionIsNot(version))
	fun versionLessThan(version: Long) = internalFilters.add(Filter.VersionLessThan(version))
	fun versionGreaterThan(version: Long) = internalFilters.add(Filter.VersionGreaterThan(version))
	fun valueIs(value: String) = internalFilters.add(Filter.ValueIs(value))
	fun valueIsNot(value: String) = internalFilters.add(Filter.ValueIsNot(value))
}

internal sealed class Filter {
	data class Before(val timestamp: Long) : Filter()
	data class After(val timestamp: Long) : Filter()
	data class VersionIs(val version: Long) : Filter()
	data class VersionIsNot(val version: Long) : Filter()
	data class VersionLessThan(val version: Long) : Filter()
	data class VersionGreaterThan(val version: Long) : Filter()
	data class ValueIs(val value: String) : Filter()
	data class ValueIsNot(val value: String) : Filter()
}
