package com.example.attendify.model

data class Absensi(
    val hari: String,
    val tanggal: String,
    val mood: String,
    val jam: String,
    val perasaan: String,
    val keterangan: String,
    val foto: ByteArray? = null
)