package se.gustavkarlsson.nag

data class MultiRecord(
	val id: Long,
	val key: String,
	val timestamp: Long,
	val appVersion: Int,
	val value: String
)
