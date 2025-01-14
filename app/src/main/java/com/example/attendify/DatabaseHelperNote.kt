package com.example.attendify

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelperNote(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "NoteDatabase.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "note"
        private const val COLUMN_ID = "id"
        private const val COLUMN_CONTENT = "content"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_CONTENT TEXT)")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }

    fun insertNote(content: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CONTENT, content)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllNotes(): List<Note> {
        val noteList = mutableListOf<Note>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "$COLUMN_ID DESC")

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                val content = it.getString(it.getColumnIndexOrThrow(COLUMN_CONTENT))
                noteList.add(Note(id, content))
            }
        }
        return noteList
    }

    fun updateNote(noteId: Int, newContent: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CONTENT, newContent)
        }
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(noteId.toString()))
    }

    fun deleteNote(noteId: Int) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(noteId.toString()))
    }
}