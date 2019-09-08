package se.gustavkarlsson.nag.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import se.gustavkarlsson.nag.Order
import java.io.File

internal class Helper(
	context: Context,
	databaseName: String? = Database.NAME,
	databaseVersion: Int = Database.VERSION,
	private val table: String = Table.NAME,
	private val createStatements: List<String> = listOf(Table.CREATE_STATEMENT),
	private val toContentValues: Map<String, Any?>.() -> ContentValues = Map<String, Any?>::toContentValues
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

	fun query(selections: List<Selection>, order: Order? = null, limit: Int? = null): Cursor =
		readableDatabase.query(
			table,
			null,
			selections.toSelectionSql(),
			selections.toSelectionArgSql(),
			null,
			null,
			order?.toSql(),
			limit?.let(Int::toString)
		)

	fun insert(row: Map<String, Any>) {
		writableDatabase.insertOrThrow(table, null, row.toContentValues())
	}

	fun upsert(selections: List<Selection>, row: Map<String, Any>) {
		writableDatabase.run {
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

	fun delete(selections: List<Selection>) {
		writableDatabase.delete(
			table,
			selections.toSelectionSql(),
			selections.toSelectionArgSql()
		)
	}

	fun deleteDatabase() {
		SQLiteDatabase.deleteDatabase(File(databaseName))
	}
}

private fun List<Selection>.toSelectionSql() =
	map(Selection::selectionSql).joinToString(" AND ")

private fun List<Selection>.toSelectionArgSql() =
	map(Selection::selectionArgSql).toTypedArray()

private fun Order.toSql(): String =
	when (this) {
		Order.OldestFirst -> "${Table.COLUMN_TIMESTAMP} ASC"
		Order.NewestFirst -> "${Table.COLUMN_TIMESTAMP} DESC"
	}

internal data class Selection(
	private val column: String,
	private val operator: Operator,
	private val value: Any
) {
	val selectionSql: String
		get() = "$column ${operator.sql} ?"

	val selectionArgSql: String
		get() = value.toString()
}

internal enum class Operator(val sql: String) {
	LessThan("<"),
	GreaterThan(">"),
	Equals("="),
	NotEquals("<>")
}
