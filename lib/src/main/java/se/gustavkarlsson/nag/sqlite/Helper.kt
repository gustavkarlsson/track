package se.gustavkarlsson.nag.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import java.io.File

const val DATABASE_NAME = "nag.db"
const val DATABASE_VERSION = 1

object SingletonRecordTableV1 {
	const val NAME = "singleton_record"
	const val COLUMN_ID = BaseColumns._ID
	const val COLUMN_KEY = "key"
	const val COLUMN_TIMESTAMP = "timestamp"
	const val COLUMN_APP_VERSION = "app_version"
	const val COLUMN_VALUE = "value"
	val SQL_CREATE_TABLE = """
		|CREATE TABLE $NAME (
		|$COLUMN_ID INTEGER PRIMARY KEY,
		|$COLUMN_KEY TEXT UNIQUE,
		|$COLUMN_TIMESTAMP INTEGER,
		|$COLUMN_APP_VERSION INTEGER,
		|$COLUMN_VALUE TEXT)
	""".trimMargin()
}

object MultiRecordTableV1 {
	const val NAME = "multi_record"
	const val COLUMN_ID = BaseColumns._ID
	const val COLUMN_KEY = "key"
	const val COLUMN_TIMESTAMP = "timestamp"
	const val COLUMN_APP_VERSION = "app_version"
	const val COLUMN_VALUE = "value"
	val SQL_CREATE_TABLE = """
		|CREATE TABLE $NAME (
		|$COLUMN_ID INTEGER PRIMARY KEY,
		|$COLUMN_KEY TEXT,
		|$COLUMN_TIMESTAMP INTEGER,
		|$COLUMN_APP_VERSION INTEGER,
		|$COLUMN_VALUE TEXT)
	""".trimMargin()
}

val SingletonRecordTable = SingletonRecordTableV1
val MultiRecordTable = MultiRecordTableV1

// FIXME don't hardcode values?
internal class Helper(context: Context, inMemory: Boolean = false) : SQLiteOpenHelper(
	context,
	if (inMemory) DATABASE_NAME else null,
	null,
	DATABASE_VERSION
) {
	override fun onCreate(db: SQLiteDatabase) {
		db.execSQL(SingletonRecordTableV1.SQL_CREATE_TABLE)
		db.execSQL(MultiRecordTableV1.SQL_CREATE_TABLE)
	}

	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		error("DB upgrade not configured from version $oldVersion-$newVersion")
	}

	fun query(
		table: String,
		selections: List<Selection>,
		order: Order? = null
	): Cursor =
		readableDatabase.query(
			table,
			null,
			selections.map(Selection::selectionSql).joinToString(" AND "),
			selections.map(Selection::selectionArgSql).toTypedArray(),
			null,
			null,
			order?.sql
		)

	fun insert(table: String, values: Map<String, Any>) {
		writableDatabase.insertOrThrow(table, null, values.toContentValues())
	}

	fun upsert(
		table: String,
		whereColumnName: String,
		whereColumnValue: String,
		values: Map<String, Any>
	) {
		writableDatabase.run {
			beginTransaction()
			try {
				delete(table, "$whereColumnName = ?", arrayOf(whereColumnValue))
				insertOrThrow(table, null, values.toContentValues())
				setTransactionSuccessful()
			} finally {
				endTransaction()
			}
		}
	}

	fun delete(table: String, selections: List<Selection>) {
		writableDatabase.delete(
			table,
			selections.map(Selection::selectionSql).joinToString(" AND "),
			selections.map(Selection::selectionArgSql).toTypedArray()
		)
	}

	fun deleteDatabase() {
		SQLiteDatabase.deleteDatabase(File(DATABASE_NAME)) // FIXME verify
	}

	sealed class Order {
		abstract val sql: String

		data class Ascending(val column: String) : Order() {
			override val sql: String
				get() = "$column ASC"
		}

		data class Descending(val column: String) : Order() {
			override val sql: String
				get() = "$column DESC"
		}
	}
}

data class Selection(
	private val column: String,
	private val operator: Operator,
	private val value: Any
) {
	val selectionSql: String
		get() = "$column ${operator.sql} ?"

	val selectionArgSql: String
		get() = value.toString()
}

enum class Operator(val sql: String) {
	LessThan("<"),
	GreaterThan(">"),
	Equals("="),
	NotEquals("<>")
}

private fun Map<String, Any>.toContentValues(): ContentValues =
	ContentValues(values.size).apply {
		forEach { (column, value) ->
			when (value) {
				is Boolean -> put(column, value)
				is ByteArray -> put(column, value)
				is Byte -> put(column, value)
				is Short -> put(column, value)
				is Int -> put(column, value)
				is Long -> put(column, value)
				is Float -> put(column, value)
				is Double -> put(column, value)
				is String -> put(column, value)
				else -> throw IllegalArgumentException("Unsupported type: ${value.javaClass}")
			}
		}
	}
