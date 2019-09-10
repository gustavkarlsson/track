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
	private val createStatements: List<String> = listOf(Table.CREATE_STATEMENT),
	private val toSelectionSql: List<Selection>.() -> String = List<Selection>::toSelectionSql,
	private val toSelectionArgSql: List<Selection>.() -> Array<String> =
		List<Selection>::toSelectionArgSql,
	private val toContentValues: Map<String, Any?>.() -> ContentValues =
		Map<String, Any?>::toContentValues
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

	fun query(selections: List<Selection>, orderBy: OrderBy? = null, limit: Int? = null): Cursor =
		readableDatabase.query(
			table,
			null,
			selections.toSelectionSql(),
			selections.toSelectionArgSql(),
			null,
			null,
			orderBy?.toSql(),
			limit?.let(Int::toString)
		)

	fun insert(row: Map<String, Any>) {
		writableDatabase.insertOrThrow(table, null, row.toContentValues())
	}

	fun upsert(selections: List<Selection>, row: Map<String, Any>) {
		with(writableDatabase) {
			beginTransaction()
			try {
				delete(
					table,
					selections.toSelectionSql(),
					selections.toSelectionArgSql()
				)
				insertOrThrow(table, null, row.toContentValues())
				setTransactionSuccessful()
			} finally {
				endTransaction()
			}
		}
	}

	fun delete(selections: List<Selection>): Int =
		writableDatabase.delete(
			table,
			selections.toSelectionSql(),
			selections.toSelectionArgSql()
		)

	fun deleteDatabase(): Boolean {
		val file = File(readableDatabase.path)
		close()
		return SQLiteDatabase.deleteDatabase(file)
	}
}
