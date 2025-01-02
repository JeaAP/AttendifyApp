package com.example.attendify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attendify.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var dbHelperProfile: DatabaseHelperProfile
    private lateinit var dbHelperAbsensi: DatabaseHelperAbsensi
    private val handler = android.os.Handler(Looper.getMainLooper())
    private lateinit var timeUpdater: Runnable

    interface FragmentInteractionListener {
        fun updateLocationText(text: String)
        fun isUserInGeofence(): Boolean
    }

    private var listener: FragmentInteractionListener? = null
    private var currentLocation: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? FragmentInteractionListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        dbHelperProfile = DatabaseHelperProfile(requireContext())
        dbHelperAbsensi = DatabaseHelperAbsensi(requireContext())

        if (savedInstanceState != null) {
            currentLocation = savedInstanceState.getString("CURRENT_LOCATION")
        }
        updateLocationText(currentLocation ?: "Loading location...")

        loadProfileData()

        binding.FotoProfile.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnAbcent.setOnClickListener {
            handleAttendance()
        }

        binding.btnIzin.setOnClickListener {
            val intent = Intent(requireContext(), coomingSoon::class.java)
            startActivity(intent)
        }

        binding.cardSchedule.setOnClickListener {
            val intent = Intent(requireContext(), coomingSoon::class.java)
            startActivity(intent)
        }

        binding.linkText.setOnClickListener {
            val intent = Intent(requireContext(), viewAllAbsensi::class.java)
            startActivity(intent)
        }

        val absensiList = dbHelperAbsensi.getLimitedAbsensi()
        val adapter = AbsensiAdapter(absensiList)

        binding.activityContent.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTimeUpdater()
        updateDate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("CURRENT_LOCATION", binding.location.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timeUpdater) // Hentikan pembaruan saat fragment dihancurkan
    }

    private fun setupTimeUpdater() {
        timeUpdater = object : Runnable {
            override fun run() {
                updateCurrentTime()
                updateDate() // Perbarui tanggal secara terus-menerus
                handler.postDelayed(this, 60000) // Jalankan setiap menit
            }
        }
        handler.post(timeUpdater)
    }

    private fun updateCurrentTime() {
        val timeFormat = SimpleDateFormat("HH:mm 'WIB'", Locale.getDefault())
        val currentTime = timeFormat.format(Date())
        binding.time.text = currentTime
    }

    private fun updateDate() {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        binding.date.text = currentDate
    }

    private fun updateLocationText(text: String) {
        binding.location.text = text
        currentLocation = text
    }

    private fun loadProfileData() {
        val profile = dbHelperProfile.getProfile()
        if (profile != null) {
            binding.accountName.text = profile.nama
            binding.accountClass.text = profile.kelas
            binding.username.text = "Hi ${profile.username},"
            binding.greetings.text = "Selamat datang!"
            binding.motivations.text = "Tetap semangat belajar!"

            val bitmap = profile.foto?.let {
                DatabaseHelperProfile.byteArrayToBitmap(it)
            }
            if (bitmap != null) {
                binding.FotoProfile.setImageBitmap(bitmap)
            } else {
                binding.FotoProfile.setImageResource(R.drawable.account_circle)
            }
        } else {
            binding.accountName.text = "[Nama tidak ditemukan]"
            binding.accountClass.text = "[Kelas tidak ditemukan]"
            binding.username.text = "Hi [Nama],"
            binding.greetings.text = "[Greetings]"
            binding.motivations.text = "[Motivasi]"
        }
    }

    private fun handleAttendance() {
        val now = Calendar.getInstance()
        val cutOffTime = Calendar.getInstance()
        cutOffTime.set(Calendar.HOUR_OF_DAY, 6)
        cutOffTime.set(Calendar.MINUTE, 30)

        if (listener?.isUserInGeofence() == true) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            if (!dbHelperAbsensi.hasAbsensiToday(today)) {
                if (now.after(cutOffTime)) {
                    Toast.makeText(context, "Anda tidak bisa absen setelah pukul 06:30", Toast.LENGTH_LONG).show()
                } else {
                    val intent = Intent(requireContext(), ScanActivity::class.java)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(context, "Anda sudah melakukan absen hari ini", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Anda harus berada di dalam wilayah SMKN 24 Jakarta", Toast.LENGTH_LONG).show()
        }
    }
}
