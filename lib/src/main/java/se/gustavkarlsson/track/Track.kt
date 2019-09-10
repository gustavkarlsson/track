package se.gustavkarlsson.track

import android.content.Context
import android.os.Build
import se.gustavkarlsson.track.sqlite.Sqlite
import se.gustavkarlsson.track.sqlite.SqliteTrack

interface Track {
    fun get(key: String): Record?
    fun set(key: String, value: String = "")
    fun query(
        key: String,
        order: Order = Order.Ascending,
        filters: FiltersBuilder.() -> Unit = {}
    ): CloseableSequence<Record>

    fun add(key: String, value: String = "")
    fun remove(id: Long): Boolean
    fun remove(key: String, filters: FiltersBuilder.() -> Unit = {}): Int
    fun deleteDatabase(): Boolean

    companion object : Track {
        private var initializedDelegate: Track? = null
        private inline val delegate: Track
            get() = requireNotNull(initializedDelegate) {
                "Track is not yet initialized. Run initialize() first"
            }

        fun initialize(context: Context) {
            initializedDelegate = SqliteTrack(Sqlite(context), context.appVersion)
        }

        override fun get(key: String) = delegate.get(key)

        override fun set(key: String, value: String) = delegate.set(key, value)

        override fun query(
            key: String,
            order: Order,
            filters: FiltersBuilder.() -> Unit
        ) = delegate.query(key, order, filters)

        override fun add(key: String, value: String) = delegate.add(key, value)

        override fun remove(id: Long) = delegate.remove(id)

        override fun remove(key: String, filters: FiltersBuilder.() -> Unit) =
            delegate.remove(key, filters)

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
