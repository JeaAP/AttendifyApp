package com.example.attendify

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelperAbsensi(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Absensi.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_NAME = "absensi"
        private const val COLUMN_ID = "id"
        private const val COLUMN_HARI = "hari"
        private const val COLUMN_TANGGAL = "tanggal"
        private const val COLUMN_MOOD = "mood"
        private const val COLUMN_JAM = "jam"
        private const val COLUMN_PERASAAN = "perasaan"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_HARI TEXT,
                $COLUMN_TANGGAL TEXT,
                $COLUMN_MOOD TEXT,
                $COLUMN_JAM TEXT,
                $COLUMN_PERASAAN TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertAbsensi(hari: String, tanggal: String, jam: String, mood: String, perasaan: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_HARI, hari)
            put(COLUMN_TANGGAL, tanggal)
            put(COLUMN_MOOD, mood)
            put(COLUMN_JAM, jam)
            put(COLUMN_PERASAAN, perasaan)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllAbsensi(): List<Absensi> {
        val absensiList = mutableListOf<Absensi>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "$COLUMN_ID ASC")

        cursor?.use {
            while (it.moveToNext()) {
                val hari = it.getString(it.getColumnIndexOrThrow(COLUMN_HARI))
                val tanggal = it.getString(it.getColumnIndexOrThrow(COLUMN_TANGGAL))
                val mood = it.getString(it.getColumnIndexOrThrow(COLUMN_MOOD))
                val jam = it.getString(it.getColumnIndexOrThrow(COLUMN_JAM))
                val perasaan = it.getString(it.getColumnIndexOrThrow(COLUMN_PERASAAN))
                absensiList.add(Absensi(hari, tanggal, mood, jam, perasaan))
            }
        }
        return absensiList
    }

    fun getLimitedAbsensi(): List<Absensi> {
        val absensiList = mutableListOf<Absensi>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "$COLUMN_ID DESC", "3")

        cursor?.use {
            while (it.moveToNext()) {
                val hari = it.getString(it.getColumnIndexOrThrow(COLUMN_HARI))
                val tanggal = it.getString(it.getColumnIndexOrThrow(COLUMN_TANGGAL))
                val mood = it.getString(it.getColumnIndexOrThrow(COLUMN_MOOD))
                val jam = it.getString(it.getColumnIndexOrThrow(COLUMN_JAM))
                val perasaan = it.getString(it.getColumnIndexOrThrow(COLUMN_PERASAAN))
                absensiList.add(Absensi(hari, tanggal, mood, jam, perasaan))
            }
        }
        return absensiList
    }

    fun deleteOldAbsensi() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID NOT IN (SELECT $COLUMN_ID FROM $TABLE_NAME ORDER BY $COLUMN_ID DESC LIMIT 30)")
    }

    //Cek data hari ini
    fun hasAbsensiToday(date: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf("id"),  // Cukup ambil kolom id atau kolom yang menunjukkan record
            "tanggal = ?",   // Kolom 'tanggal' harus dalam format YYYY-MM-DD jika menggunakan SQLite
            arrayOf(date),
            null,
            null,
            null
        )
        val hasEntry = cursor.count > 0
        cursor.close()
        return hasEntry
    }

}