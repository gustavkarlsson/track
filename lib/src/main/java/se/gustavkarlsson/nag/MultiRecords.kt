package se.gustavkarlsson.nag

interface MultiRecords {
	fun query(
		key: String,
		filters: Filters.() -> Unit = {}
	): CloseableSequence<MultiRecord>

	fun add(key: String, value: String = "")
	fun remove(id: Long)
	fun remove(
		key: String,
		filters: Filters.() -> Unit = {}
	)

	class Filters {
		internal val filters: MutableList<MultiQueryFilter> = mutableListOf()
		private fun add(filter: MultiQueryFilter) = filters.add(filter)

		fun before(timestamp: Long) = add(MultiQueryFilter.Before(timestamp))
		fun after(timestamp: Long) = add(MultiQueryFilter.After(timestamp))
		fun versionIs(version: Int) = add(MultiQueryFilter.VersionIs(version))
		fun versionIsNot(version: Int) = add(MultiQueryFilter.VersionIsNot(version))
		fun versionBefore(version: Int) = add(MultiQueryFilter.VersionBefore(version))
		fun versionAfter(version: Int) = add(MultiQueryFilter.VersionAfter(version))
		fun valueIs(value: String) = add(MultiQueryFilter.ValueIs(value))
	}
}

internal sealed class MultiQueryFilter {
	data class Before(val timestamp: Long) : MultiQueryFilter()
	data class After(val timestamp: Long) : MultiQueryFilter()
	data class VersionIs(val version: Int) : MultiQueryFilter()
	data class VersionIsNot(val version: Int) : MultiQueryFilter()
	data class VersionBefore(val version: Int) : MultiQueryFilter()
	data class VersionAfter(val version: Int) : MultiQueryFilter()
	data class ValueIs(val value: String) : MultiQueryFilter()
}
