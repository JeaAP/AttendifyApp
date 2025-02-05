package com.example.attendify.ui

import com.example.attendify.databasehelper.*
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.example.attendify.SyncHelper
import com.example.attendify.databinding.ActivityDeskripsiAbsenBinding
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ActivityDeskripsiAbsen : AppCompatActivity() {

    private lateinit var binding: ActivityDeskripsiAbsenBinding
    private lateinit var syncHelper: SyncHelper
    private lateinit var dbHelper: DatabaseHelperAbsensi

    private var capturedPhoto: ByteArray? = null

    private val waktuBatasAbsen = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 6)
        set(Calendar.MINUTE, 30)
        set(Calendar.SECOND, 0)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // Back button is disabled on this screen
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeskripsiAbsenBinding.inflate(layoutInflater)
        dbHelper = DatabaseHelperAbsensi(this)
        setContentView(binding.root)

        syncHelper = SyncHelper(this)

        val llPhoto = binding.llPhoto
        val llSendMessage = binding.llSendMessage
        val llFeelings = binding.llFeelings

        binding.back.setOnClickListener {
            val intent = Intent(this, ActivityMain::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnNext.setOnClickListener {
            if (capturedPhoto == null) {
                Snackbar.make(binding.root, "Please take a photo first!", Snackbar.LENGTH_SHORT).show()
            } else {
                llPhoto.visibility = View.GONE
                llFeelings.visibility = View.VISIBLE
            }
        }

        binding.btnMood.setOnClickListener{
            binding.rbAngry.setOnClickListener {
                handleMoodSelection("Angry")
            }
            binding.rbTired.setOnClickListener {
                handleMoodSelection("Tired")
            }
            binding.rbSad.setOnClickListener {
                handleMoodSelection("Sad")
            }
            binding.rbHappy.setOnClickListener {
                handleMoodSelection("Happy")
            }
            binding.rbExcited.setOnClickListener {
                handleMoodSelection("Excited")
            }
        }

        binding.cardImage.setOnClickListener {
            openCamera()
            binding.textAbsen.visibility = View.GONE
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap
            binding.fotoAbsen.setImageBitmap(photo)
            capturedPhoto = bitmapToByteArray(photo)
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }

    private fun handleMoodSelection(mood: String) {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        if (mood == "Happy" || mood == "Good") {
            if(networkInfo != null && networkInfo.isConnected){
                saveAbsensi(mood, "Baik")
                syncHelper.syncData()
            } else {
                AlertDialog.Builder(this@ActivityDeskripsiAbsen)
                    .setTitle("Koneksi Internet Tidak Tersedia")
                    .setMessage("Silakan periksa koneksi internet Anda dan coba lagi.")
                    .setPositiveButton("Coba Lagi") { dialog, _ ->
                        recreate() // Reload activity untuk mencoba kembali
                        dialog.dismiss()
                    }
                    .setNegativeButton("Tutup") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
            }
        } else {
            binding.llFeelings.visibility = View.GONE
            binding.llSendMessage.visibility = View.VISIBLE
            binding.btnSend.setOnClickListener {
                if (binding.edDescription.text.toString().trim().isNotEmpty()) {
                    if(networkInfo != null && networkInfo.isConnected){
                        saveAbsensi(mood, binding.edDescription.text.toString().trim())
                        syncHelper.syncData()
                    } else {
                        AlertDialog.Builder(this@ActivityDeskripsiAbsen)
                            .setTitle("Koneksi Internet Tidak Tersedia")
                            .setMessage("Silakan periksa koneksi internet Anda dan coba lagi.")
                            .setPositiveButton("Coba Lagi") { dialog, _ ->
                                recreate() // Reload activity untuk mencoba kembali
                                dialog.dismiss()
                            }
                            .setNegativeButton("Tutup") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .setCancelable(false)
                            .show()
                    }
                } else {
                    Snackbar.make(binding.root, "Please describe your day!", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveAbsensi(mood: String, perasaan: String) {
        val currentTime = Calendar.getInstance()
        val hariFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val tanggalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val jamFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val hari = hariFormat.format(currentTime.time)
        val tanggal = tanggalFormat.format(currentTime.time)
        val jam = jamFormat.format(currentTime.time)
        val keterangan = dbHelper.checkKeterlambatan(jam)

        val result = dbHelper.insertAbsensi(hari, tanggal, jam, mood, perasaan, keterangan, capturedPhoto)

        if (result != -1L) {
            Snackbar.make(binding.root, "Absensi berhasil!", Snackbar.LENGTH_SHORT).show()
            val intent = Intent(this, ActivityMain::class.java)
            startActivity(intent)
            finish()
        } else {
            Snackbar.make(binding.root, "Absensi gagal!", Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }
}