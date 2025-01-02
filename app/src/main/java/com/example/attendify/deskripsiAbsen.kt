package com.example.attendify

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.example.attendify.databinding.ActivityDeskripsiAbsenBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class deskripsiAbsen : AppCompatActivity() {

    private lateinit var binding: ActivityDeskripsiAbsenBinding
    private lateinit var dbHelper: DatabaseHelperAbsensi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeskripsiAbsenBinding.inflate(layoutInflater)
        dbHelper = DatabaseHelperAbsensi(this)
        setContentView(binding.root)

        val llSendMessage = binding.llSendMessage
        val llFeelings = binding.llFeelings

        binding.btnSend.setOnClickListener {
            val perasaan = binding.edDescription.text.toString().trim()
            if (perasaan.isEmpty()) {
                Snackbar.make(binding.root, "Please describe your day!", Snackbar.LENGTH_SHORT).show()
            } else {
                llSendMessage.visibility = View.GONE
                llFeelings.visibility = View.VISIBLE
            }
        }

        binding.Happy.setOnClickListener { saveAbsensi("Senang") }
        binding.Good.setOnClickListener { saveAbsensi("Biasa Saja") }
        binding.Bad.setOnClickListener { saveAbsensi("Sedih") }
    }

    private fun saveAbsensi(mood: String) {
        val currentTime = Calendar.getInstance()
        val hariFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val tanggalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val jamFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val hari = hariFormat.format(currentTime.time)
        val tanggal = tanggalFormat.format(currentTime.time)
        val jam = jamFormat.format(currentTime.time)
        val perasaan = binding.edDescription.text.toString().trim()

        val result = dbHelper.insertAbsensi(hari, tanggal, jam, mood, perasaan)

        if (result != -1L) {
            Snackbar.make(binding.root, "Absensi berhasil!", Snackbar.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Snackbar.make(binding.root, "Absensi gagal!", Snackbar.LENGTH_SHORT).show()
        }
    }
}
