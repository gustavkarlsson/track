package se.gustavkarlsson.nag

import android.content.Context
import android.os.Build
import se.gustavkarlsson.nag.sqlite.Sqlite
import se.gustavkarlsson.nag.sqlite.SqliteNag

interface Nag {
	fun get(key: String): Record?
	fun set(key: String, value: String = "")
	fun query(
		key: String,
		order: Order = Order.Ascending,
		where: WhereBuilder.() -> Unit = {}
	): CloseableSequence<Record>

	fun add(key: String, value: String = "")
	fun remove(id: Long): Boolean
	fun remove(key: String, where: WhereBuilder.() -> Unit = {}): Int
	fun deleteDatabase(): Boolean

	companion object : Nag {
		private var initializedDelegate: Nag? = null
		private inline val delegate: Nag
			get() = requireNotNull(initializedDelegate) {
				"Nag is not yet initialized. Run initialize() first"
			}

		fun initialize(context: Context) {
			initializedDelegate = SqliteNag(Sqlite(context), context.appVersion)
		}

		override fun get(key: String) = delegate.get(key)

		override fun set(key: String, value: String) = delegate.set(key, value)

		override fun query(
			key: String,
			order: Order,
			where: WhereBuilder.() -> Unit
		) = delegate.query(key, order, where)

		override fun add(key: String, value: String) = delegate.add(key, value)

		override fun remove(id: Long) = delegate.remove(id)

		override fun remove(key: String, where: WhereBuilder.() -> Unit) =
			delegate.remove(key, where)

		override fun deleteDatabase() = delegate.deleteDatabase()
	}
}

private val Context.appVersion: Long
	get() {
		val packageInfo = packageManager.getPackageInfo(packageName, 0)
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			packageInfo.longVersionCode
		} else {
			@Suppress("DEPRECATION")
			packageInfo.versionCode.toLong()
		}
	}
