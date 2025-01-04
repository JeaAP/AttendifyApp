package com.example.attendify

data class Absensi(
    val id: Int,
    val hari: String,
    val tanggal: String,
    val mood: String,
    val jam: String,
    val perasaan: String,
    val keterangan: String,
    val foto: ByteArray? = null
)