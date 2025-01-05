package com.example.attendify

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.attendify.databinding.ActivityIzinBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class IzinActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIzinBinding
    private lateinit var dbHelper: DatabaseHelperAbsensi

    private var capturedPhoto: ByteArray? = null

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // Back button is disabled on this screen
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIzinBinding.inflate(layoutInflater)
        dbHelper = DatabaseHelperAbsensi(this)
        setContentView(binding.root)

        val llPhoto = binding.llPhoto
        val llSendMessage = binding.llSendMessage
        val llIzin = binding.llIzin

        var keterangan: String? = null

        binding.back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.llSick.setOnClickListener{
            llIzin.visibility = View.GONE
            llPhoto.visibility = View.VISIBLE

            binding.btnNext.setOnClickListener {
                if (capturedPhoto == null) {
                    Snackbar.make(binding.root, "Please take a photo first!", Snackbar.LENGTH_SHORT).show()
                } else {
                    saveAbsensi(keterangan = "Sakit")
                }
            }
        }

        binding.llPermit.setOnClickListener{
            llIzin.visibility = View.GONE
            llPhoto.visibility = View.VISIBLE
            binding.ket.text = "Please provide a permission note!"
            binding.btnNext.text = "Next"

            binding.btnNext.setOnClickListener {
                if (capturedPhoto == null) {
                    Snackbar.make(binding.root, "Please take a photo first!", Snackbar.LENGTH_SHORT).show()
                } else {
                    llPhoto.visibility = View.GONE
                    llSendMessage.visibility = View.VISIBLE

                    binding.btnSend.setOnClickListener {
                        val perasaan = binding.edDescription.text.toString().trim()
                        if (perasaan.isEmpty()) {
                            Snackbar.make(binding.root, "Please specify the reason for your excused absence!", Snackbar.LENGTH_SHORT).show()
                        } else {
                            saveAbsensi(binding.edDescription.text.toString().trim())
                        }
                    }
                }
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        return stream.toByteArray()
    }

    private fun saveAbsensi(keterangan: String) {
        val currentTime = Calendar.getInstance()
        val hariFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val tanggalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val jamFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val hari = hariFormat.format(currentTime.time)
        val tanggal = tanggalFormat.format(currentTime.time)
        val jam = jamFormat.format(currentTime.time)
        val mood = ""
        val perasaan = ""

        val result = dbHelper.insertAbsensi(hari, tanggal, jam, mood, perasaan, keterangan, capturedPhoto)

        if (result != -1L) {
            Snackbar.make(binding.root, "Izin Berhasil!", Snackbar.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Snackbar.make(binding.root, "Izin gagal!", Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }
}