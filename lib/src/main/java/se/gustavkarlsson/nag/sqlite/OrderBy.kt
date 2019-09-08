package se.gustavkarlsson.nag.sqlite

internal sealed class OrderBy {
	abstract val sql: String

	data class Ascending(val column: String) : OrderBy() {
		override val sql get() = "$column ASC"
	}

	data class Descending(val column: String) : OrderBy() {
		override val sql get() = "$column DESC"
	}
}
