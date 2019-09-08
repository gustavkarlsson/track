package se.gustavkarlsson.nag

interface SingletonRecords {
	fun get(key: String): SingletonRecord?
	fun set(key: String, value: String = "")
	fun remove(key: String)
}
