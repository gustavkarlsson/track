package se.gustavkarlsson.track

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.Size
import androidx.annotation.VisibleForTesting
import se.gustavkarlsson.track.sqlite.Sqlite
import se.gustavkarlsson.track.sqlite.SqliteTrack

/**
 * A simple database holding [Record]s.
 */
interface Track {
    /**
     * Gets a singleton record for [key] if one exists. Will not return records added with [add].
     */
    suspend fun get(key: String): Record?

    /**
     * Sets a singleton value for [key]. Overwrites any existing value.
     *
     * @return `true` if an existing value was replaced, otherwise `false`
     */
    suspend fun set(key: String, value: String = ""): Boolean

    /**
     * Gets all records for [key].
     */
    suspend fun query(key: String): List<Record> = query(key, Order.InsertionAscending) { it.toList() }

    /**
     * Creates a sequence of all records for [key] and invokes [selector] on the resulting sequence,
     * yielding a return value. The elements of the sequence will be ordered by [order]
     *
     * The sequence may only be iterated once, and may not be used after this function returns.
     */
    suspend fun <T> query(
        key: String,
        order: Order = Order.InsertionAscending,
        selector: (Sequence<Record>) -> T
    ): T

    /**
     * Adds a [value] for [key]
     */
    suspend fun add(key: String, value: String = "")

    /**
     * Removes the record matching [id] if it exists. Returns `true` if any record was removed, otherwise `false`.
     */
    suspend fun remove(id: Long): Boolean

    /**
     * Removes all records matching [key] and returns the number of records removed.
     */
    suspend fun remove(key: String): Int

    /**
     * Removes all records matching [filter] and returns the number of records removed.
     */
    suspend fun remove(filter: (Record) -> Boolean): Int

    /**
     * Clears all records and closes any underlying resources. Returns `true` if successful, otherwise `false`.
     *
     * You do **NOT** need to call [initialize] again to use Track after clearing.
     */
    suspend fun clear(): Boolean

    /**
     * Disconnects any open connections to the database, freeing up resources
     */
    fun disconnect()

    /**
     * The default instance of [Track]. Must be initialized with [initialize] before use.
     */
    companion object : Track {
        @VisibleForTesting
        internal var initializedDelegate: Track? = null
        private val delegate: Track
            get() = checkNotNull(initializedDelegate) {
                "Track is not yet initialized. Run initialize() first"
            }

        /**
         * Initializes the default instance of [Track]
         *
         * In most cases you want to do this in your application's `onCreate()`
         */
        fun initialize(context: Context, @Size(min = 1) databaseFileName: String = "track.db") {
            check(initializedDelegate == null) { "Track is already initialized" }
            initializedDelegate = create(context, databaseFileName)
        }

        /**
         * Creates a new instance of [Track] using the given database file name
         */
        fun create(context: Context, @Size(min = 1) databaseFileName: String): Track {
            require(databaseFileName.isNotBlank()) { "Database file name may not be blank" }
            return SqliteTrack(Sqlite(context, databaseFileName), context.appVersion)
        }

        override suspend fun get(key: String) = delegate.get(key)

        override suspend fun set(key: String, value: String) = delegate.set(key, value)

        override suspend fun <T> query(key: String, order: Order, selector: (Sequence<Record>) -> T): T {
            return delegate.query(key, order, selector)
        }

        override suspend fun add(key: String, value: String) = delegate.add(key, value)

        override suspend fun remove(id: Long) = delegate.remove(id)

        override suspend fun remove(key: String) = delegate.remove(key)

        override suspend fun remove(filter: (Record) -> Boolean) = delegate.remove(filter)

        override suspend fun clear() = delegate.clear()

        override fun disconnect() = delegate.disconnect()
    }
}

private val Context.appVersion: Long
    get() {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    }
