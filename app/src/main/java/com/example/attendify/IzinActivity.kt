package com.example.attendify

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.attendify.databinding.ActivityIzinBinding
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class IzinActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIzinBinding
    private lateinit var dbHelper: DatabaseHelperAbsensi
    private var capturedPhoto: ByteArray? = null
    private var izinType: String = ""

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIzinBinding.inflate(layoutInflater)
        dbHelper = DatabaseHelperAbsensi(this)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        // Tombol kembali ke MainActivity
        binding.back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Pilihan izin sakit
        binding.llSick.setOnClickListener {
            izinType = "Sakit"
            binding.ket.text = "Please provide a sick note!"
            binding.btnNext.text = "Send"
            showPhotoSection()
        }

        // Pilihan izin lainnya
        binding.llPermit.setOnClickListener {
            izinType = "Izin"
            binding.ket.text = "Please provide a permission note!"
            binding.btnNext.text = "Next"
            showPhotoSection()
        }

        // Ambil foto
        binding.cardImage.setOnClickListener { openCamera() }

        // Tombol Next/Send untuk izin
        binding.btnNext.setOnClickListener {
            if (capturedPhoto == null) {
                Toast.makeText(this, "Please take a photo first!", Toast.LENGTH_SHORT).show()
            } else {
                if (izinType == "Izin") {
                    showMessageSection()
                } else {
                    saveToDatabase("")
                }
            }
        }

        // Tombol Send untuk mengirim keterangan izin
        binding.btnSend.setOnClickListener {
            val keterangan = binding.edDescription.text.toString().trim()
            if (keterangan.isEmpty()) {
                Toast.makeText(this, "Please enter a description!", Toast.LENGTH_SHORT).show()
            } else {
                saveToDatabase(keterangan)
            }
        }
    }

    private fun showPhotoSection() {
        binding.llIzin.visibility = View.GONE
        binding.llPhoto.visibility = View.VISIBLE
        binding.llSendMessage.visibility = View.GONE
    }

    private fun showMessageSection() {
        binding.llPhoto.visibility = View.GONE
        binding.llSendMessage.visibility = View.VISIBLE
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
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

    private fun saveToDatabase(keterangan: String) {
        val currentTime = Calendar.getInstance()
        val hariFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val tanggalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val jamFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val hari = hariFormat.format(currentTime.time)
        val tanggal = tanggalFormat.format(currentTime.time)
        val jam = jamFormat.format(currentTime.time)

        // Simpan data ke database
        val result = dbHelper.insertAbsensi(
            hari = hari,
            tanggal = tanggal,
            jam = jam,
            keterangan = izinType,
            mood = "", // Mood tidak digunakan dalam izin
            perasaan = if (izinType == "Sakit") izinType else keterangan,
            foto = capturedPhoto
        )

        if (result != -1L) {
            Toast.makeText(this, "Permission recorded successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to record permission.", Toast.LENGTH_SHORT).show()
        }
    }
}
