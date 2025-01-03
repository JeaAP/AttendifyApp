package com.example.attendify

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attendify.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var dbHelperProfile: DatabaseHelperProfile
    private lateinit var dbHelperAbsensi: DatabaseHelperAbsensi
    private val handler = android.os.Handler(Looper.getMainLooper())

    private lateinit var dialog: Dialog
    private lateinit var cancelImage: ImageView
    private lateinit var time: TextView
    private lateinit var timeText: TextView
    private lateinit var btnAbcentDialog: CardView

    private var currentLocation: String? = null
    private var isDialogShown: Boolean = false

    private val calendar = Calendar.getInstance()
    private val hour = calendar.get(Calendar.HOUR_OF_DAY)
    private val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    private val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY

    private var listener: FragmentInteractionListener? = null

    private val jamAbsen = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 6)
        set(Calendar.MINUTE, 24)
    }
    private val cutOffTimeMorning = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 6)
        set(Calendar.MINUTE, 30)
    }

    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        dbHelperProfile = DatabaseHelperProfile(requireContext())
        dbHelperAbsensi = DatabaseHelperAbsensi(requireContext())

        loadProfileData()
        setupDialog()
        handleDialogDisplay()
        setupButtonsAndViews()
        setupRecyclerView()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setupTimeUpdater()
        updateDate()
    }

    private fun setupDialog() {
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.absenct_notification_dialog)
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(R.drawable.custom_dialog_bg)
        dialog.setCancelable(false)

        cancelImage = dialog.findViewById(R.id.cancelImage)
        time = dialog.findViewById(R.id.time)
        timeText = dialog.findViewById(R.id.timeText)
        btnAbcentDialog = dialog.findViewById(R.id.btnAbcentDialog)

        cancelImage.setOnClickListener {
            dialog.dismiss()
        }

        btnAbcentDialog.setOnClickListener {
            if (listener?.isUserInGeofence() == true) {
                dialog.dismiss()
                startActivity(Intent(requireContext(), ScanActivity::class.java))
            } else {
                Toast.makeText(context, "Anda harus berada di dalam wilayah SMKN 24 Jakarta", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleDialogDisplay() {
        val sharedPreferences = requireContext().getSharedPreferences("AttendifyPrefs", Context.MODE_PRIVATE)
        isDialogShown = sharedPreferences.getBoolean("isDialogShown", false)

        if (!isDialogShown && shouldShowDialog()) {
            dialog.show()
            sharedPreferences.edit().putBoolean("isDialogShown", true).apply()
        }
    }

    private fun shouldShowDialog(): Boolean {
        return !isWeekend && calendar.after(jamAbsen) && calendar.before(cutOffTimeMorning) &&
                !dbHelperAbsensi.hasAbsensiToday(today)
    }

    private fun setupButtonsAndViews() {
        binding.FtProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        binding.btnAbcent.setOnClickListener {
            if (listener?.isUserInGeofence() == true) {
                if (!dbHelperAbsensi.hasAbsensiToday(today)) {
                    when {
                        calendar.before(jamAbsen) -> {
                            Toast.makeText(context, "Belum bisa absen, masih pagi", Toast.LENGTH_LONG).show()
                        }
                        calendar.before(cutOffTimeMorning) -> {
                            startActivity(Intent(requireContext(), ScanActivity::class.java))
                        }
                        else -> {
                            Toast.makeText(context, "Waktu absen telah habis", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Anda sudah melakukan absen hari ini", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Anda harus berada di dalam wilayah SMKN 24 Jakarta", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnIzin.setOnClickListener {
            startActivity(Intent(requireContext(), coomingSoon::class.java))
        }

        binding.cardSchedule.setOnClickListener {
            val url = "https://docs.google.com/spreadsheets/d/1eM7gvonky0HdSKDPUxjs9o9IZjeC28Ud/edit?pli=1&gid=1682312915#gid=1682312915"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        binding.linkText.setOnClickListener {
            startActivity(Intent(requireContext(), viewAllAbsensi::class.java))
        }
    }

    private fun setupRecyclerView() {
        val absensiList = dbHelperAbsensi.getLimitedAbsensi()
        val adapter = AbsensiAdapter(absensiList)

        binding.activityContent.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter

            binding.tvNoData.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    private fun setupTimeUpdater() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateCurrentTime()
                handler.postDelayed(this, 60000) // Update time every minute
            }
        }, 60000)
    }

    private fun updateCurrentTime() {
        val currentTime = SimpleDateFormat("HH:mm 'WIB'", Locale.getDefault()).format(Date())
        binding.time.text = currentTime
    }

    private fun updateDate() {
        val currentDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
        binding.date.text = currentDate
    }

    private fun loadProfileData() {
        val profile = dbHelperProfile.getProfile()
        if (profile != null) {
            binding.accountName.text = profile.nama
            binding.accountClass.text = profile.kelas
            binding.username.text = "Hi ${profile.username},"
            setGreetings()
            setMotivations()
        } else {
            binding.accountName.text = "[Nama tidak ditemukan]"
            binding.accountClass.text = "[Kelas tidak ditemukan]"
            binding.username.text = "Hi [Nama],"
            binding.greetings.text = "[Greetings]"
            binding.motivations.text = "[Motivasi]"
        }
    }

    private fun setGreetings() {
        val greeting = when {
            isWeekend -> "Selamat berlibur!"
            hour < 12 -> "Selamat pagi!"
            hour in 12..15 -> "Selamat siang!"
            hour in 16..18 -> "Selamat sore!"
            else -> "Selamat malam!"
        }
        binding.greetings.text = greeting
    }

    private fun setMotivations() {
        val motivation = when {
            dayOfWeek == Calendar.FRIDAY && hour >= 17 -> "Terima kasih untuk 5 hari kerja kerasmu, istirahat yang nyenyak."
            dayOfWeek == Calendar.SATURDAY -> "Saatnya libur, gunakan waktumu untuk kegiatan kesukaanmu."
            dayOfWeek == Calendar.SUNDAY -> "Pilihlah untuk bersantai atau bersiap untuk minggu baru, selamat libur."
            hour < 12 -> "Pagi yang indah, hadapi dengan semangat dan kegembiraan yang baru."
            hour in 12..16 -> "Lanjutkan energimu, kamu sudah melakukan banyak hal hari ini."
            else -> "Terima kasih telah berusaha hari ini, sekarang waktunya untuk istirahat yang nyenyak."
        }
        binding.motivations.text = motivation
    }

    interface FragmentInteractionListener {
        fun isUserInGeofence(): Boolean
    }
}
