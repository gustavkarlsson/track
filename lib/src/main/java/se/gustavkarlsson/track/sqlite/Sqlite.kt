package se.gustavkarlsson.track.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.Size
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.gustavkarlsson.track.Order

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
    private val toOrderBySql: Order.() -> String =
        Order::toOrderBySql,
    private val toContentValues: Map<String, Any?>.() -> ContentValues =
        Map<String, Any?>::toContentValues,
    private val deleteDatabase: (File) -> Boolean =
        SQLiteDatabase::deleteDatabase,
    private val getDatabase: SQLiteOpenHelper.() -> SQLiteDatabase =
        SQLiteOpenHelper::getWritableDatabase
) : SQLiteOpenHelper(
    context,
    databaseName,
    null,
    databaseVersion
) {
    private val database: SQLiteDatabase get() = getDatabase()

    override fun onCreate(db: SQLiteDatabase) {
        createStatements.forEach(db::execSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        error("DB upgrade not configured from version $oldVersion-$newVersion")
    }

    suspend fun <T> query(
        selections: List<Selection>,
        order: Order? = null,
        limit: Int? = null,
        block: (Cursor) -> T
    ): T =
        withContext(Dispatchers.IO) {
            val cursor = database.query(
                table,
                null,
                selections.toSelectionSql(),
                selections.toSelectionArgSql(),
                null,
                null,
                order?.toOrderBySql(),
                limit?.let(Int::toString)
            )
            cursor.use(block).also { cursor.close() }
        }

    suspend fun insert(row: Map<String, Any>) {
        withContext(Dispatchers.IO) {
            database.insertOrThrow(table, null, row.toContentValues())
        }
    }

    suspend fun upsert(selections: List<Selection>, row: Map<String, Any>): Boolean =
        withContext(Dispatchers.IO) {
            database.beginTransaction()
            try {
                val deletedCount = database.delete(
                    table,
                    selections.toSelectionSql(),
                    selections.toSelectionArgSql()
                )
                database.insertOrThrow(table, null, row.toContentValues())
                database.setTransactionSuccessful()
                deletedCount > 0
            } finally {
                database.endTransaction()
            }
        }

    suspend fun delete(selections: List<Selection>): Int =
        withContext(Dispatchers.IO) {
            database.delete(
                table,
                selections.toSelectionSql(),
                selections.toSelectionArgSql()
            )
        }

    suspend fun deleteDatabase(): Boolean {
        val deleted = withContext(Dispatchers.IO) {
            val file = File(database.path)
            close()
            deleteDatabase(file)
        }
        return deleted
    }
}
