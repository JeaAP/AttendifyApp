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

    /**
     * Menambahkan user baru ke database. Jika tabel sudah memiliki data, proses ini akan dibatalkan.
     * @param username Nama pengguna
     * @param password Kata sandi
     * @return ID baris jika berhasil, atau -1 jika tabel sudah penuh.
     */
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

    /**
     * Membaca data user berdasarkan username dan password.
     * Digunakan untuk autentikasi login.
     * @param username Nama pengguna
     * @param password Kata sandi
     * @return True jika user ditemukan, false jika tidak.
     */
    fun readUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val selection = "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(username, password)
        val cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)

        val userExists = cursor.count > 0
        cursor.close()
        return userExists
    }

    /**
     * Mengecek apakah database sudah penuh (hanya bisa menyimpan 1 data).
     * @return True jika tabel sudah penuh, false jika kosong.
     */
    fun isDatabaseFull(): Boolean {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        val isFull = cursor.count > 0
        cursor.close()
        return isFull
    }

    /**
     * Mengubah password berdasarkan username (nisn).
     * @param username Nama pengguna (nisn)
     * @param newPassword Kata sandi baru
     * @return True jika password berhasil diubah, false jika username tidak ditemukan.
     */
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
}
