package com.example.attendify

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.example.attendify.databinding.ActivityDeskripsiAbsenBinding
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class deskripsiAbsen : AppCompatActivity() {

    private lateinit var binding: ActivityDeskripsiAbsenBinding
    private lateinit var dbHelper: DatabaseHelperAbsensi

    private var capturedPhoto: ByteArray? = null

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // Back button is disabled on this screen
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeskripsiAbsenBinding.inflate(layoutInflater)
        dbHelper = DatabaseHelperAbsensi(this)
        setContentView(binding.root)

        val llPhoto = binding.llPhoto
        val llSendMessage = binding.llSendMessage
        val llFeelings = binding.llFeelings

        binding.back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnNext.setOnClickListener {
            if (capturedPhoto == null) {
                Snackbar.make(binding.root, "Please take a photo first!", Snackbar.LENGTH_SHORT).show()
            } else {
                llPhoto.visibility = View.GONE
                llSendMessage.visibility = View.VISIBLE
            }
        }

        binding.btnSend.setOnClickListener {
            val perasaan = binding.edDescription.text.toString().trim()
            if (perasaan.isEmpty()) {
                Snackbar.make(binding.root, "Please describe your day!", Snackbar.LENGTH_SHORT).show()
            } else {
                llSendMessage.visibility = View.GONE
                llFeelings.visibility = View.VISIBLE
            }
        }

        binding.Happy.setOnClickListener { saveAbsensi("Happy") }
        binding.Good.setOnClickListener { saveAbsensi("Good") }
        binding.Bad.setOnClickListener { saveAbsensi("Bad") }

        binding.cardImage.setOnClickListener {
            openCamera()
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        return stream.toByteArray()
    }

    private fun saveAbsensi(mood: String) {
        val currentTime = Calendar.getInstance()
        val hariFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val tanggalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val jamFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val hari = hariFormat.format(currentTime.time)
        val tanggal = tanggalFormat.format(currentTime.time)
        val jam = jamFormat.format(currentTime.time)
        val perasaan = binding.edDescription.text.toString().trim()
        val keterangan = "Hadir"

        val result = dbHelper.insertAbsensi(hari, tanggal, jam, mood, perasaan, keterangan, capturedPhoto)

        if (result != -1L) {
            Snackbar.make(binding.root, "Absensi berhasil!", Snackbar.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
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
