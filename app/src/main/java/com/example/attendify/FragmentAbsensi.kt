package com.example.attendify

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attendify.databinding.FragmentAbsensiBinding

class FragmentAbsensi : Fragment() {

    private lateinit var binding: FragmentAbsensiBinding
    private lateinit var databaseHelper: DatabaseHelperAbsensi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAbsensiBinding.inflate(layoutInflater)
        databaseHelper = DatabaseHelperAbsensi(this@FragmentAbsensi.requireContext())
        databaseHelper.deleteOldAbsensi()

        val absensiList = databaseHelper.getAllAbsensi()

        val adapter = AbsensiAdapter(absensiList) { absensi ->
            showDetailPopup(absensi)
        }

        binding.activityContent.layoutManager =
            LinearLayoutManager(this@FragmentAbsensi.requireContext())
        binding.activityContent.adapter = adapter

        if (adapter.itemCount == 0) {
            binding.tvNoData.visibility = View.VISIBLE
        } else {
            binding.tvNoData.visibility = View.GONE
        }

        binding.back.setOnClickListener {
            val intent = Intent(this@FragmentAbsensi.requireContext(), ActivityMain::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    private fun showDetailPopup(absensi: Absensi) {
        val dialog = Dialog(this@FragmentAbsensi.requireContext())
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