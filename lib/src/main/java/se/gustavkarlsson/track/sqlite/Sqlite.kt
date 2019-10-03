package se.gustavkarlsson.track.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File

internal class Sqlite(
    context: Context,
    databaseName: String? = Database.NAME,
    databaseVersion: Int = Database.VERSION,
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
        SQLiteOpenHelper::getWritableDatabase
) : SQLiteOpenHelper(
    context,
    databaseName,
    null,
    databaseVersion
) {
    override fun onCreate(db: SQLiteDatabase) {
        createStatements.forEach(db::execSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        error("DB upgrade not configured from version $oldVersion-$newVersion")
    }

    fun <T> query(selections: List<Selection>, limit: Int? = null, block: (Cursor) -> T): T =
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

    fun insert(row: Map<String, Any>) {
        getDatabase().use {
            it.insertOrThrow(table, null, row.toContentValues())
        }
    }

    fun upsert(selections: List<Selection>, row: Map<String, Any>): Boolean =
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

    fun delete(selections: List<Selection>): Int =
        getDatabase().use {
            it.delete(
                table,
                selections.toSelectionSql(),
                selections.toSelectionArgSql()
            )
        }

    fun deleteDatabase(): Boolean {
        val file = getDatabase().use { File(it.path) }
        close()
        return deleteDatabase(file)
    }
}
