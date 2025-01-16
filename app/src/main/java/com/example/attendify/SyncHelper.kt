package com.example.attendify

import android.content.Context
import android.util.Log
import com.example.attendify.databasehelper.DatabaseHelperAbsensi
import com.example.attendify.databasehelper.DatabaseHelperProfile

class SyncHelper(private val context: Context) {

    private val databaseHelperAbsensi = DatabaseHelperAbsensi(context)
    private val databaseHelperProfile = DatabaseHelperProfile(context)
    private lateinit var apiController: ApiController


    fun syncData() {
        val absensi = databaseHelperAbsensi.getLastestAbsensi()
        val profile = databaseHelperProfile.getProfile()

        if(absensi != null && profile != null){
            val postData = mapOf(
                "nisn" to profile.nisn,
                "nama" to profile.nama,
                "kelas" to profile.kelas,
                "mood" to absensi.mood,
                "perasaan" to absensi.perasaan,
//                "keterangan" to absensi.keterangan
            )
            apiController = ApiController("absensi_attendify.php", "POST")
            apiController.execute(postData) { response, error ->
                if (error != null) {
                    Log.e("API Error", "Failed to sync data: $error")
                } else {
                    Log.d("API Response", "Data synced successfully: $response")
                }
            }
        } else {
            Log.e("Sync Error", "Absensi or profile data is null")
        }
    }
}