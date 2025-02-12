package com.example.attendify.databasehelper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.attendify.model.Absensi
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
        private const val COLUMN_KETERANGAN = "keterangan"
        private const val COLUMN_FOTO = "foto"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_HARI TEXT,
                $COLUMN_TANGGAL TEXT,
                $COLUMN_MOOD TEXT,
                $COLUMN_JAM TEXT,
                $COLUMN_PERASAAN TEXT,
                $COLUMN_KETERANGAN TEXT,
                $COLUMN_FOTO BLOB
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertAbsensi(hari: String, tanggal: String, jam: String, mood: String, perasaan: String, keterangan: String, foto: ByteArray?): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_HARI, hari)
            put(COLUMN_TANGGAL, tanggal)
            put(COLUMN_MOOD, mood)
            put(COLUMN_JAM, jam)
            put(COLUMN_PERASAAN, perasaan)
            put(COLUMN_KETERANGAN, keterangan)
            put(COLUMN_FOTO, foto)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllAbsensi(): List<Absensi> {
        val absensiList = mutableListOf<Absensi>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "$COLUMN_ID DESC")

        cursor?.use {
            while (it.moveToNext()) {
                val hari = it.getString(it.getColumnIndexOrThrow(COLUMN_HARI))
                val tanggal = it.getString(it.getColumnIndexOrThrow(COLUMN_TANGGAL))
                val mood = it.getString(it.getColumnIndexOrThrow(COLUMN_MOOD))
                val jam = it.getString(it.getColumnIndexOrThrow(COLUMN_JAM))
                val perasaan = it.getString(it.getColumnIndexOrThrow(COLUMN_PERASAAN))
                val keterangan = it.getString(it.getColumnIndexOrThrow(COLUMN_KETERANGAN)) ?: ""
                val foto = it.getBlob(it.getColumnIndexOrThrow(COLUMN_FOTO))
                absensiList.add(Absensi(hari, tanggal, mood, jam, perasaan, keterangan, foto))
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
                val keterangan = it.getString(it.getColumnIndexOrThrow(COLUMN_KETERANGAN)) ?: ""
                val foto = it.getBlob(it.getColumnIndexOrThrow(COLUMN_FOTO))
                absensiList.add(Absensi(hari, tanggal, mood, jam, perasaan, keterangan, foto))
            }
        }
        return absensiList
    }

    fun deleteOldAbsensi() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID NOT IN (SELECT $COLUMN_ID FROM $TABLE_NAME ORDER BY $COLUMN_ID DESC LIMIT 30)")
    }

    fun hasAbsensiToday(date: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf("id"),
            "$COLUMN_TANGGAL = ?",
            arrayOf(date),
            null,
            null,
            null
        )
        val hasEntry = cursor.count > 0
        cursor.close()
        return hasEntry
    }

    fun getAbsensiStatus(date: String): String? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_KETERANGAN),  // Mengambil hanya kolom keterangan
            "$COLUMN_TANGGAL = ?",
            arrayOf(date),
            null,
            null,
            null
        )
        var keterangan: String? = null
        if (cursor.moveToFirst()) {
            keterangan = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_KETERANGAN))
        }
        cursor.close()
        return keterangan
    }

    fun checkKeterlambatan(jam: String): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val batasWaktu = formatter.parse("06:30")
        val waktuAbsen = formatter.parse(jam)
        return if (waktuAbsen != null && waktuAbsen.after(batasWaktu)) {
            "Terlambat"
        } else {
            "Tepat Waktu"
        }
    }

    fun hitungAbsensi(): Pair<Int, Int> {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_JAM), null, null, null, null, null)

        var tepatWaktu = 0
        var terlambat = 0

        cursor?.use {
            while (it.moveToNext()) {
                val jam = it.getString(it.getColumnIndexOrThrow(COLUMN_JAM))
                val status = checkKeterlambatan(jam)
                if (status == "Tepat Waktu") {
                    tepatWaktu++
                } else {
                    terlambat++
                }
            }
        }

        val totalKehadiran = tepatWaktu + terlambat
        return Pair(totalKehadiran, terlambat)
    }


    fun hitungPersentaseAbsensi(): Pair<Double, Double> {
        val db = readableDatabase
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val bulanIni = dateFormat.format(calendar.time) // Format: 2025-01

        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_JAM, COLUMN_TANGGAL),
            "$COLUMN_TANGGAL LIKE ?",
            arrayOf("$bulanIni%"),
            null,
            null,
            null
        )

        var tepatWaktu = 0
        var terlambat = 0

        cursor?.use {
            while (it.moveToNext()) {
                val jam = it.getString(it.getColumnIndexOrThrow(COLUMN_JAM))
                val status = checkKeterlambatan(jam)
                if (status == "Tepat Waktu") {
                    tepatWaktu++
                } else {
                    terlambat++
                }
            }
        }

        val totalKehadiran = tepatWaktu + terlambat
        val persentaseKehadiran = if (totalKehadiran > 0) (totalKehadiran.toDouble() / totalKehadiran) * 100 else 0.0
        val persentaseTerlambat = if (totalKehadiran > 0) (terlambat.toDouble() / totalKehadiran) * 100 else 0.0

        return Pair(persentaseKehadiran, persentaseTerlambat)
    }

    fun getLastestAbsensi(): Absensi? {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "$COLUMN_ID DESC", "1")

        var lastAbsensi: Absensi? = null
        if (cursor.moveToFirst()) {
            val hari = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HARI))
            val tanggal = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TANGGAL))
            val mood = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD))
            val jam = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JAM))
            val perasaan = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PERASAAN))
            val keterangan = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_KETERANGAN))
            val foto = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_FOTO))
            lastAbsensi = Absensi(hari, tanggal, mood, jam, perasaan, keterangan, foto)
        }
        cursor.close()
        return lastAbsensi
    }
}