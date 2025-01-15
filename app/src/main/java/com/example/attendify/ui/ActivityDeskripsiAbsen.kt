package com.example.attendify.ui

import com.example.attendify.databasehelper.*
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
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

        binding.Anggry.setOnClickListener {
            handleMoodSelection("Angry")
        }
        binding.Happy.setOnClickListener {
            handleMoodSelection("Happy")
        }
        binding.Good.setOnClickListener {
            handleMoodSelection("Good")
        }
        binding.notGood.setOnClickListener {
            handleMoodSelection("Not Good")
        }
        binding.Sad.setOnClickListener {
            handleMoodSelection("Sad")
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
        if (mood == "Happy" || mood == "Good") {
            saveAbsensi(mood, "Baik")
            syncHelper.syncData()
        } else {
            binding.llFeelings.visibility = View.GONE
            binding.llSendMessage.visibility = View.VISIBLE
            binding.btnSend.setOnClickListener {
                val description = binding.edDescription.text.toString().trim()
                if (description.isNotEmpty()) {
                    saveAbsensi(mood, description)
                    syncHelper.syncData()
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
        val keterangan = if (currentTime.after(waktuBatasAbsen)) "Hadir" else "Terlambat"

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