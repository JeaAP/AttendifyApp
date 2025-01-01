package com.example.attendify

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class DatabaseHelperLogin(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "data"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "nisn"
        private const val COLUMN_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USERNAME TEXT, " +
                "$COLUMN_PASSWORD TEXT)")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }

    fun insertUser(username: String, password: String): Long {
        val db = readableDatabase

        // Periksa apakah tabel sudah memiliki data
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        if (cursor.count > 0) {
            cursor.close()
            return -1 // Tabel sudah berisi data
        }

        // Jika tabel kosong, masukkan data baru
        cursor.close()
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
        }
        val writableDb = writableDatabase
        return writableDb.insert(TABLE_NAME, null, values)
    }

    fun readUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val selection = "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(username, password)
        val cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)

        val userExists = cursor.count > 0
        cursor.close()
        return userExists
    }

    fun isDatabaseFull(): Boolean {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        val isFull = cursor.count > 0
        cursor.close()
        return isFull
    }

    fun isUserExists(username: String): Boolean {
        val db = readableDatabase
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)
        val cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)

        val userExists = cursor.count > 0
        cursor.close()
        return userExists
    }

    fun updatePassword(username: String, newPassword: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PASSWORD, newPassword)
        }
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)

        val rowsUpdated = db.update(TABLE_NAME, values, selection, selectionArgs)
        return rowsUpdated > 0
    }

    fun getLoggedInNisn(): String? {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_USERNAME), null, null, null, null, null)
        return if (cursor.moveToFirst()) {
            val nisn = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
            cursor.close()
            nisn
        } else {
            cursor.close()
            null
        }
    }

}
