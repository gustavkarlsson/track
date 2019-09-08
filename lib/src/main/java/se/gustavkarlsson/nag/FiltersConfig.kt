package se.gustavkarlsson.nag

class FiltersConfig internal constructor() {
	internal val filters: MutableList<Filter> = mutableListOf()

	fun before(timestamp: Long) = filters.add(Filter.Before(timestamp))
	fun after(timestamp: Long) = filters.add(Filter.After(timestamp))
	fun versionIs(version: Long) = filters.add(Filter.VersionIs(version))
	fun versionIsNot(version: Long) = filters.add(Filter.VersionIsNot(version))
	fun versionLessThan(version: Long) = filters.add(Filter.VersionLessThan(version))
	fun versionGreaterThan(version: Long) = filters.add(Filter.VersionGreaterThan(version))
	fun valueIs(value: String) = filters.add(Filter.ValueIs(value))
}

internal sealed class Filter {
	data class Before(val timestamp: Long) : Filter()
	data class After(val timestamp: Long) : Filter()
	data class VersionIs(val version: Long) : Filter()
	data class VersionIsNot(val version: Long) : Filter()
	data class VersionLessThan(val version: Long) : Filter()
	data class VersionGreaterThan(val version: Long) : Filter()
	data class ValueIs(val value: String) : Filter()
}
