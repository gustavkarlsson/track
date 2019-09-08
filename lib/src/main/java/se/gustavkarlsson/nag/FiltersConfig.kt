package se.gustavkarlsson.nag

class FiltersConfig internal constructor() {
	internal val filters: MutableList<Filter> = mutableListOf()

	fun before(timestamp: Long) = filters.add(Filter.Before(timestamp))
	fun after(timestamp: Long) = filters.add(Filter.After(timestamp))
	fun versionIs(version: Int) = filters.add(Filter.VersionIs(version))
	fun versionIsNot(version: Int) = filters.add(Filter.VersionIsNot(version))
	fun versionLessThan(version: Int) = filters.add(Filter.VersionLessThan(version))
	fun versionGreaterThan(version: Int) = filters.add(Filter.VersionGreaterThan(version))
	fun valueIs(value: String) = filters.add(Filter.ValueIs(value))
}

internal sealed class Filter {
	data class Before(val timestamp: Long) : Filter()
	data class After(val timestamp: Long) : Filter()
	data class VersionIs(val version: Int) : Filter()
	data class VersionIsNot(val version: Int) : Filter()
	data class VersionLessThan(val version: Int) : Filter()
	data class VersionGreaterThan(val version: Int) : Filter()
	data class ValueIs(val value: String) : Filter()
}
