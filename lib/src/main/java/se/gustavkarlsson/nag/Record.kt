package se.gustavkarlsson.nag

data class Record(
	val id: Long,
	val key: String,
	val timestamp: Long,
	val appVersion: Int,
	val value: String
)
