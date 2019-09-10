package se.gustavkarlsson.track.sqlite

internal sealed class OrderBy {
    abstract fun toSql(): String

    data class Ascending(val column: String) : OrderBy() {
        override fun toSql() = "$column ASC"
    }

    data class Descending(val column: String) : OrderBy() {
        override fun toSql() = "$column DESC"
    }
}
