package com.example.attendify

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer.OnPreparedListener
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attendify.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.logging.Handler

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var dbHelperProfile: DatabaseHelperProfile
    private lateinit var dbHelperAbsensi: DatabaseHelperAbsensi
    private val handler = android.os.Handler(Looper.getMainLooper())
    private lateinit var timeUpdater: Runnable

    interface FragmentInteractionListener {
//        fun onButtonClicked()
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
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater)
        dbHelperProfile = DatabaseHelperProfile(requireContext())
        dbHelperAbsensi = DatabaseHelperAbsensi(requireContext())

        if (savedInstanceState != null) {
            currentLocation = savedInstanceState.getString("CURRENT_LOCATION")
        }
        updateLocationText(currentLocation ?: "Loading location...")

        // Load data from the database and update UI
        loadProfileData()

        // Set listeners for buttons and views
        binding.FotoProfile.setOnClickListener {
            val intent = Intent(this@HomeFragment.requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnAbcent.setOnClickListener {
            val now = Calendar.getInstance()
            val cutOffTime = Calendar.getInstance()
            cutOffTime.set(Calendar.HOUR_OF_DAY, 6)
            cutOffTime.set(Calendar.MINUTE, 30)

            if (listener?.isUserInGeofence() == true) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                if (!dbHelperAbsensi.hasAbsensiToday(today)) {
                    if(now.after(cutOffTime)){
                        Toast.makeText(context, "Anda tidak bisa absen setelah pukul 06:30", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    } else{
                        val intent = Intent(this@HomeFragment.requireContext(), ScanActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(context, "Anda sudah melakukan absen hari ini", Toast.LENGTH_LONG).show()
                }
//                val intent = Intent(this@HomeFragment.requireContext(), ScanActivity::class.java)
//                startActivity(intent)
            } else {
                Toast.makeText(context, "Anda harus berada di dalam wilayah SMKN 24 Jakarta", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnIzin.setOnClickListener {
            val intent = Intent(this@HomeFragment.requireContext(), coomingSoon::class.java)
            startActivity(intent)
        }

        binding.cardSchedule.setOnClickListener {
            val intent = Intent(this@HomeFragment.requireContext(), coomingSoon::class.java)
            startActivity(intent)
        }

        binding.linkText.setOnClickListener {
            val intent = Intent(this@HomeFragment.requireContext(), viewAllAbsensi::class.java)
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

    fun updateLocationText(text: String) {
        binding.location.text = text
        currentLocation = text
    }

    private fun setupTimeUpdater() {
        timeUpdater = object : Runnable {
            override fun run() {
                updateCurrentTime()
                handler.postDelayed(this, 60000)  // Update time every minute
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
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.ENGLISH)
        val currentDate = dateFormat.format(Date())
        binding.date.text = currentDate
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
}
