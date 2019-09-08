package se.gustavkarlsson.nag

data class SingletonRecord(
	val key: String,
	val timestamp: Long,
	val appVersion: Int,
	val value: String
)
