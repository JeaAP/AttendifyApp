package com.example.attendify

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer.OnPreparedListener
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
import java.util.logging.Handler

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var dbHelperProfile: DatabaseHelperProfile
    private lateinit var dbHelperAbsensi: DatabaseHelperAbsensi
    private val handler = android.os.Handler(Looper.getMainLooper())
    private lateinit var timeUpdater: Runnable

    private lateinit var dialog: Dialog
    private lateinit var cancelImage: ImageView
    private lateinit var time: TextView
    private lateinit var timeText: TextView
    private lateinit var btnAbcent: CardView

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

        //POP UP NOTIFIKASI DIALOG ABSEN
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.absenct_notification_dialog)
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(R.drawable.custom_dialog_bg)
        dialog.setCancelable(false)

        cancelImage = dialog.findViewById(R.id.cancelImage)
        time = dialog.findViewById(R.id.time)
        timeText = dialog.findViewById(R.id.timeText)
        btnAbcent = dialog.findViewById(R.id.btnAbcent)

        cancelImage.setOnClickListener{
            dialog.dismiss()
        }

        btnAbcent.setOnClickListener{
            dialog.dismiss()
            val intent = Intent(this@HomeFragment.requireContext(), ScanActivity::class.java)
            startActivity(intent)
        }

        val nowTime = Calendar.getInstance()
        val jamAbsen = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 24)
        }
        val lewatJamAbsen = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 30)
        }

        val remainingTimeInMinutes = ((lewatJamAbsen.timeInMillis - nowTime.timeInMillis) / 60000).toInt()

        val currentTimeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(nowTime.time)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if(nowTime.after(jamAbsen) && nowTime.before(lewatJamAbsen)){
            time.text = currentTimeFormatted

            val remainingTimeText = if (remainingTimeInMinutes > 0) {
                "${numberToWords(remainingTimeInMinutes)} minutes left to absence"
            } else {
                "Time to absence has passed"
            }
            timeText.text = remainingTimeText

            if (!dbHelperAbsensi.hasAbsensiToday(today)){
                dialog.show()
            }
        }


        if (savedInstanceState != null) {
            currentLocation = savedInstanceState.getString("CURRENT_LOCATION")
        }
        updateLocationText(currentLocation ?: "Loading location...")

        // Load data from the database and update UI
        loadProfileData()

        // Set listeners for buttons and views
        binding.FtProfile.setOnClickListener {
            val intent = Intent(this@HomeFragment.requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnAbcent.setOnClickListener {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val now = Calendar.getInstance()

            val cutOffTimeMorning = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 6)
                set(Calendar.MINUTE, 30)
            }

            val cutOffTimeAfternoon = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 15)
                set(Calendar.MINUTE, 0)
            }

            binding.btnAbcent.setOnClickListener {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val now = Calendar.getInstance()
                val cutOffTimeMorning = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 6)
                    set(Calendar.MINUTE, 30)
                }
                val cutOffTimeAfternoon = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 15)
                    set(Calendar.MINUTE, 0)
                }
                val cutOffTimeEarlyMorning = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 5)
                    set(Calendar.MINUTE, 0)
                }

                if (listener?.isUserInGeofence() == true) { // Jika dalam wilayah
                    if (!dbHelperAbsensi.hasAbsensiToday(today)) { // Jika hari ini belum absen
                        if (now.before(cutOffTimeEarlyMorning)) {
                            Toast.makeText(context, "Belum bisa absen, masih jam 5 pagi", Toast.LENGTH_LONG).show()
                        } else if (now.before(cutOffTimeMorning)) { // Jika sudah lewat jam absen pagi
                            if (now.before(cutOffTimeAfternoon)) { // Sebelum jam 3 sore
                                val intent = Intent(this@HomeFragment.requireContext(), ScanActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(context, "Waktu sekolah selesai", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "Anda tidak bisa absen setelah pukul 06:30", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Anda sudah melakukan absen hari ini", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Anda harus berada di dalam wilayah SMKN 24 Jakarta", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.btnIzin.setOnClickListener {
            val intent = Intent(this@HomeFragment.requireContext(), coomingSoon::class.java)
            startActivity(intent)
        }

        binding.cardSchedule.setOnClickListener {
            val url = "https://docs.google.com/spreadsheets/d/1eM7gvonky0HdSKDPUxjs9o9IZjeC28Ud/edit?pli=1&gid=1682312915#gid=1682312915"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
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

            val tvNoData = binding.tvNoData
            if (adapter.itemCount == 0) {
                tvNoData.visibility = View.VISIBLE
            } else {
                tvNoData.visibility = View.GONE
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setupTimeUpdater()
        updateDate()
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

    fun numberToWords(number: Int): String { //ANGKA DALAM HURUF BAHASA INGGRIS
        val words = arrayOf(
            "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
            "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen",
            "twenty", "twenty-one", "twenty-two", "twenty-three", "twenty-four", "twenty-five", "twenty-six", "twenty-seven", "twenty-eight", "twenty-nine",
            "thirty", "thirty-one", "thirty-two", "thirty-three", "thirty-four", "thirty-five", "thirty-six", "thirty-seven", "thirty-eight", "thirty-nine",
            "forty", "forty-one", "forty-two", "forty-three", "forty-four", "forty-five", "forty-six", "forty-seven", "forty-eight", "forty-nine",
            "fifty", "fifty-one", "fifty-two", "fifty-three", "fifty-four", "fifty-five", "fifty-six", "fifty-seven", "fifty-eight", "fifty-nine"
        )
        return if (number in 0..59) words[number] else "Invalid number"
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
            setGreetings()
            setMotivations()

            val bitmap = profile.foto?.let {
                DatabaseHelperProfile.byteArrayToBitmap(it)
            }
            if (bitmap != null) {
                binding.FtProfile.setImageBitmap(bitmap)
            } else {
                binding.FtProfile.setImageResource(R.drawable.account_circle)
            }

        } else {
            binding.accountName.text = "[Nama tidak ditemukan]"
            binding.accountClass.text = "[Kelas tidak ditemukan]"
            binding.username.text = "Hi [Nama],"
            binding.greetings.text = "[Greetings]"
            binding.motivations.text = "[Motivasi]"
        }
    }

    private fun setGreetings() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Cek apakah hari ini adalah hari libur
        val isHoliday = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY

        val greeting = when {
            isHoliday -> "Selamat berlibur!"
            hour < 12 -> "Selamat pagi!"
            hour in 12..15 -> "Selamat siang!"
            hour in 16..18 -> "Selamat sore!"
            else -> "Selamat malam!"
        }

        binding.greetings.text = greeting
    }

    private fun setMotivations() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        val motivation = when {
            dayOfWeek == Calendar.FRIDAY && hour >= 17 -> "Terima kasih untuk 5 hari kerja kerasmu, istirahat yang nyeyak."
            dayOfWeek == Calendar.SATURDAY -> "Saatnya libur, gunakan waktumu untuk kegiatan kesukaanmu."
            dayOfWeek == Calendar.SUNDAY -> "Pilihlah untuk bersantai atau bersiap untuk minggu baru, selamat libur"
            hour < 12 -> "Pagi yang indah, hadapi dengan semangat dan kegembiraan yang baru."
            hour in 12..16 -> "Lanjutkan energimu, kamu sudah melakukan banyak hal hari ini."
            hour in 17..20 -> "Sore yang tenang, kamu telah bekerja keras hari ini."
            else -> "Terima kasih telah berusaha hari ini, sekarang waktunya untuk istirahat yang nyenyak."
        }

        binding.motivations.text = motivation
    }

}
