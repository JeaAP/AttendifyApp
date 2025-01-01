package com.example.attendify

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import java.io.ByteArrayOutputStream

class DatabaseHelperProfile(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "profile.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "profile"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "nama"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_CLASS = "kelas"
        private const val COLUMN_ABSEN = "absen"
        private const val COLUMN_NISN = "nisn"
        private const val COLUMN_PHOTO = "foto"

        fun byteArrayToBitmap(byteArray: ByteArray): Bitmap? {
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_USERNAME TEXT,
                $COLUMN_CLASS TEXT,
                $COLUMN_ABSEN INTEGER,
                $COLUMN_NISN TEXT,
                $COLUMN_PHOTO BLOB
            )
        """.trimIndent()
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }

    fun upsertProfile(profile: Profile) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, profile.nama)
            put(COLUMN_USERNAME, profile.username)
            put(COLUMN_CLASS, profile.kelas)
            put(COLUMN_ABSEN, profile.absen)
            put(COLUMN_NISN, profile.nisn)
            put(COLUMN_PHOTO, profile.foto)
        }

        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        val isProfileExist = cursor.count > 0
        cursor.close()

        if (isProfileExist) {
            db.update(TABLE_NAME, values, null, null)
        } else {
            db.insert(TABLE_NAME, null, values)
        }
        db.close()
    }

    fun getProfile(): Profile? {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME LIMIT 1"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val nama = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
            val kelas = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS))
            val absen = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ABSEN))
            val nisn = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NISN))
            val foto = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_PHOTO))
            cursor.close()
            db.close()

            return Profile(id, nama, username, kelas, absen, nisn, foto)
        }

        cursor.close()
        db.close()
        return null
    }

    fun imageViewToByte(img: ImageView): ByteArray {
        val bitmap: Bitmap = if (img.drawable is BitmapDrawable) {
            (img.drawable as BitmapDrawable).bitmap
        } else {
            val vectorDrawable = img.drawable
            val width = vectorDrawable.intrinsicWidth
            val height = vectorDrawable.intrinsicHeight
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                val canvas = Canvas(this)
                vectorDrawable.setBounds(0, 0, width, height)
                vectorDrawable.draw(canvas)
            }
        }

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        return stream.toByteArray()
    }

}