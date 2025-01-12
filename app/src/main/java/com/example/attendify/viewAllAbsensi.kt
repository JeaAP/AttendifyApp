package com.example.attendify

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
//        super.onBackPressed()
//        Toast.makeText(this, "Back button is disabled on this screen.", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAllAbsensiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelperAbsensi(this)
        databaseHelper.deleteOldAbsensi()

        val absensiList = databaseHelper.getAllAbsensi()

        val adapter = AbsensiAdapter(absensiList) { absensi ->
            showDetailPopup(absensi)
        }

        binding.activityContent.layoutManager = LinearLayoutManager(this)
        binding.activityContent.adapter = adapter

        if (adapter.itemCount == 0) {
            binding.tvNoData.visibility = View.VISIBLE
        } else {
            binding.tvNoData.visibility = View.GONE
        }

        binding.back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showDetailPopup(absensi: Absensi) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.detail_item)
        dialog.window?.apply{
            setBackgroundDrawableResource(android.R.color.transparent)
            setGravity(Gravity.CENTER_VERTICAL)
        }

        val dayTextView = dialog.findViewById<TextView>(R.id.day)
        val dateTextView = dialog.findViewById<TextView>(R.id.date)
        val timeTextView = dialog.findViewById<TextView>(R.id.time)
        val emoteImageView = dialog.findViewById<ImageView>(R.id.emoteImage)
        val status = dialog.findViewById<TextView>(R.id.status)
        val descriptionTextView = dialog.findViewById<TextView>(R.id.description)
        val descriptionPhoto = dialog.findViewById<ImageView>(R.id.descriptionPhoto)

        dayTextView.text = absensi.hari
        dateTextView.text = absensi.tanggal
        timeTextView.text = absensi.jam
        status.text = absensi.keterangan
        descriptionTextView.text = absensi.perasaan

        val drawableRes = when (absensi.keterangan) {
            "Hadir" -> when (absensi.mood) {
                "Happy" -> R.drawable.salutingemote
                "Good" -> R.drawable.smileemote
                "Bad" -> R.drawable.smileemote
                else -> R.drawable.sademote
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
            descriptionPhoto.setImageResource(R.drawable.profile___iconly_pro)
        }

        dialog.show()
    }
}