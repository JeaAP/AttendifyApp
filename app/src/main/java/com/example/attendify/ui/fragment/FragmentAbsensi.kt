package com.example.attendify.ui.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attendify.databasehelper.DatabaseHelperAbsensi
import com.example.attendify.R
import com.example.attendify.ui.adapter.AbsensiAdapter
import com.example.attendify.databinding.FragmentAbsensiBinding
import com.example.attendify.model.Absensi
import com.example.attendify.ui.ActivityMain
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FragmentAbsensi : Fragment() {

    private lateinit var binding: FragmentAbsensiBinding
    private lateinit var activityMain: ActivityMain
    private lateinit var databaseHelper: DatabaseHelperAbsensi

    //======WAKTU========
    private val now = Calendar.getInstance()
    private val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

    private var absensiList: List<Absensi> = listOf() // Daftar absensi

    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        if (context is ActivityMain) {
            activityMain = context
        } else {
            throw IllegalStateException("AbsensiFragment must be attached to MainActivity")
        }
    }

    @SuppressLint("MissingSuperCall")
    fun onBackPressed() {
//        super.onBackPressed()
//        Toast.makeText(this, "Back button is disabled on this screen.", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAbsensiBinding.inflate(layoutInflater)
        databaseHelper = DatabaseHelperAbsensi(this@FragmentAbsensi.requireContext())
        databaseHelper.deleteOldAbsensi()

        binding.tvMonthYear.text = monthYear

        absensiList = databaseHelper.getAllAbsensi()

        val adapter = AbsensiAdapter(absensiList) { absensi ->
            showDetailPopup(absensi)
        }

        binding.activityContent.layoutManager =
            LinearLayoutManager(this@FragmentAbsensi.requireContext())
        binding.activityContent.adapter = adapter

        val currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_MONTH)
        Log.d("FragmentAbsensi", "Minggu saat ini: $currentWeek")

        binding.week1button.visibility = View.VISIBLE
        binding.week2button.visibility = if (currentWeek >= 2) View.VISIBLE else View.GONE
        binding.week3button.visibility = if (currentWeek >= 3) View.VISIBLE else View.GONE
        binding.week4button.visibility = if (currentWeek >= 4) View.VISIBLE else View.GONE

        adapter.updateData(filterDataByWeek(currentWeek))

        if (adapter.itemCount == 0) {
            binding.tvNoData.visibility = View.VISIBLE
        } else {
            binding.tvNoData.visibility = View.GONE
        }

//        binding.back.setOnClickListener {
//            activityMain.replaceFragment(FragmentHome())
//        }

        setupWeekButtons(adapter)
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

        val drawableRes = when (absensi.mood) {
            "Happy" -> R.drawable.salutingemote
            "Good" -> R.drawable.smileemote
            "Sad" -> R.drawable.sademote
            "Angry" -> R.drawable.angryemote
            "Not Good" -> R.drawable.confusedemote
            else -> R.drawable.smileemote
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

    private fun setupWeekButtons(adapter: AbsensiAdapter) {
        binding.week1button.setOnClickListener {
            val filteredData = filterDataByWeek(1)
            adapter.updateData(filteredData)
            updateNoDataMessage(filteredData)
        }

        binding.week2button.setOnClickListener {
            val filteredData = filterDataByWeek(2)
            adapter.updateData(filteredData)
            updateNoDataMessage(filteredData)
        }

        binding.week3button.setOnClickListener {
            val filteredData = filterDataByWeek(3)
            adapter.updateData(filteredData)
            updateNoDataMessage(filteredData)
        }

        binding.week4button.setOnClickListener {
            val filteredData = filterDataByWeek(4)
            adapter.updateData(filteredData)
            updateNoDataMessage(filteredData)
        }
    }

    private fun updateNoDataMessage(filteredData: List<Absensi>) {
        if (filteredData.isEmpty()) {
            binding.tvNoData.visibility = View.VISIBLE
        } else {
            binding.tvNoData.visibility = View.GONE
        }
    }

    private fun filterDataByWeek(week: Int): List<Absensi> {
        now.firstDayOfWeek = Calendar.MONDAY // Set awal minggu ke Senin
        now.set(Calendar.DAY_OF_MONTH, 1) // Awal bulan
        now.set(Calendar.HOUR_OF_DAY, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)

        now.add(Calendar.MONTH, 1)
        now.add(Calendar.DAY_OF_MONTH, -1)

        return absensiList.filter { absensi ->
            try {
                val absensiDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(absensi.tanggal)
                if (absensiDate != null) {
                    now.time = absensiDate
                    val weekOfMonth = now.get(Calendar.WEEK_OF_MONTH)
                    weekOfMonth == week
                } else {
                    Log.d("FilterDataByWeek", "Tanggal tidak valid: ${absensi.tanggal}")
                    false
                }
            } catch (e: Exception) {
                Log.e("FilterDataByWeek", "Error parsing tanggal: ${absensi.tanggal}, ${e.message}")
                false
            }
        }
    }

}