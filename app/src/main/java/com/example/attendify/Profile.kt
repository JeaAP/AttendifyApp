package com.example.attendify

data class Profile(
    val id: Int,
    val nama: String,
    val username: String,
    val kelas: String,
    val absen: Int,
    val nisn: String,
    val foto: ByteArray? = null,
    val bio: String
)