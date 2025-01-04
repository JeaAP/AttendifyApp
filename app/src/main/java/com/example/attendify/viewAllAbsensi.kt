package com.example.attendify

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attendify.databinding.ActivityViewAllAbsensiBinding

class viewAllAbsensi : AppCompatActivity() {
    private lateinit var databaseHelper: DatabaseHelperAbsensi
    private lateinit var binding: ActivityViewAllAbsensiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAllAbsensiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelperAbsensi(this)

        // Delete old absensi exceeding 30 days
        databaseHelper.deleteOldAbsensi()

        // Ambil id dari Intent
        val absensiId = intent.getIntExtra("absensi_id", -1)
        if (absensiId != -1) {
            val absensi = databaseHelper.getAbsensiById(absensiId)
            absensi?.let {
                showDetailPopup(it)
            } ?: run {
                Toast.makeText(this, "Data absensi tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }

        val absensiList = databaseHelper.getAllAbsensi()
        val adapter = AbsensiAdapter(absensiList)

        binding.activityContent.apply {
            layoutManager = LinearLayoutManager(this@viewAllAbsensi)
            this.adapter = adapter

            val tvNoData = binding.tvNoData
            if (adapter.itemCount == 0) {
                tvNoData.visibility = View.VISIBLE
            } else {
                tvNoData.visibility = View.GONE
            }
        }

        binding.activityContent.layoutManager = LinearLayoutManager(this)

        binding.back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showDetailPopup(absensi: Absensi) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.detail_item)

        val dayTextView = dialog.findViewById<TextView>(R.id.day)
        val dateTextView = dialog.findViewById<TextView>(R.id.date)
        val timeTextView = dialog.findViewById<TextView>(R.id.time)
        val emoteImageView = dialog.findViewById<ImageView>(R.id.emoteImage)
        val descriptionTextView = dialog.findViewById<TextView>(R.id.description)
        val descriptionPhoto = dialog.findViewById<ImageView>(R.id.descriptionPhoto)

        dayTextView.text = absensi.hari
        dateTextView.text = absensi.tanggal
        timeTextView.text = absensi.jam
        descriptionTextView.text = absensi.perasaan

        val drawableRes = when (absensi.keterangan) {
            "Hadir" -> when (absensi.mood) {
                "Happy" -> R.drawable.happyemote
                "Good" -> R.drawable.goodemote
                "Bad" -> R.drawable.bademote
                else -> R.drawable.goodemote
            }
            "Sakit" -> R.drawable.outline_sick_24
            else -> R.drawable.izin
        }
        emoteImageView.setImageResource(drawableRes)

        // Set photo if available
        if (absensi.foto != null) {
            val bitmap = BitmapFactory.decodeByteArray(absensi.foto, 0, absensi.foto.size)
            descriptionPhoto.setImageBitmap(bitmap)
        } else {
            descriptionPhoto.setImageResource(R.drawable.round_person_24)
        }

        dialog.show()
    }
}