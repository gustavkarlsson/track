package se.gustavkarlsson.track.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.Size
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class Sqlite(
    context: Context,
    @Size(min = 1) databaseName: String,
    databaseVersion: Int = DATABASE_VERSION,
    private val table: String = Table.NAME,
    private val createStatements: List<String> =
        listOf(Table.CREATE_STATEMENT),
    private val toSelectionSql: List<Selection>.() -> String? =
        List<Selection>::toSelectionSql,
    private val toSelectionArgSql: List<Selection>.() -> Array<String> =
        List<Selection>::toSelectionArgSql,
    private val toContentValues: Map<String, Any?>.() -> ContentValues =
        Map<String, Any?>::toContentValues,
    private val deleteDatabase: (File) -> Boolean =
        SQLiteDatabase::deleteDatabase,
    private val getDatabase: SQLiteOpenHelper.() -> SQLiteDatabase =
        SQLiteOpenHelper::getWritableDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SQLiteOpenHelper(
    context,
    databaseName,
    null,
    databaseVersion
) {
    private val mutex = Mutex()

    override fun onCreate(db: SQLiteDatabase) {
        createStatements.forEach(db::execSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        error("DB upgrade not configured from version $oldVersion-$newVersion")
    }

    suspend fun <T> query(selections: List<Selection>, limit: Int? = null, block: (Cursor) -> T): T =
        mutex.withLock {
            withContext(dispatcher) {
                getDatabase().use {
                    val cursor = it.query(
                        table,
                        null,
                        selections.toSelectionSql(),
                        selections.toSelectionArgSql(),
                        null,
                        null,
                        null,
                        limit?.let(Int::toString)
                    )
                    cursor.use(block)
                }
            }
        }

    suspend fun insert(row: Map<String, Any>) {
        mutex.withLock {
            withContext(dispatcher) {
                getDatabase().use {
                    it.insertOrThrow(table, null, row.toContentValues())
                }
            }
        }
    }

    suspend fun upsert(selections: List<Selection>, row: Map<String, Any>): Boolean =
        mutex.withLock {
            withContext(dispatcher) {
                getDatabase().use {
                    it.beginTransaction()
                    try {
                        val deletedCount = it.delete(
                            table,
                            selections.toSelectionSql(),
                            selections.toSelectionArgSql()
                        )
                        it.insertOrThrow(table, null, row.toContentValues())
                        it.setTransactionSuccessful()
                        deletedCount > 0
                    } finally {
                        it.endTransaction()
                    }
                }
            }
        }

    suspend fun delete(selections: List<Selection>): Int =
        mutex.withLock {
            withContext(dispatcher) {
                getDatabase().use {
                    it.delete(
                        table,
                        selections.toSelectionSql(),
                        selections.toSelectionArgSql()
                    )
                }
            }
        }

    suspend fun deleteDatabase(): Boolean {
        val deleted = mutex.withLock {
            withContext(dispatcher) {
                val file = getDatabase().use { File(it.path) }
                close()
                deleteDatabase(file)
            }
        }
        return deleted
    }
}
