package com.example.attendify

import android.annotation.SuppressLint
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

    private lateinit var timeUpdater: Runnable
    private lateinit var dialog: Dialog
    private lateinit var cancelImage: ImageView

    private lateinit var time: TextView
    private lateinit var timeText: TextView
    private lateinit var btnAbcentDialog: CardView

    private var listener: FragmentInteractionListener? = null


//    private var currentLocation: String? = null

    //======WAKTU========
    private val calendar = Calendar.getInstance()
    private val hour = calendar.get(Calendar.HOUR_OF_DAY)
    private val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    private val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY

    private val jamAbsen = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 6)
        set(Calendar.MINUTE, 24)
    }
    private val cutOffTimeMorning = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 6)
        set(Calendar.MINUTE, 30)
    }
    private val cutOffTimeAfternoon = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 15)
        set(Calendar.MINUTE, 0)
    }
    private val cutOffTimeEarlyMorning = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 5)
        set(Calendar.MINUTE, 0)
    }
    private val jamIzin = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 7)
        set(Calendar.MINUTE, 0)
    }

    private val remainingTimeInMinutes = ((cutOffTimeMorning.timeInMillis - calendar.timeInMillis) / 60000).toInt()

    private val currentTimeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    //======WAKTU========

    private var isDialogShown = false

    interface FragmentInteractionListener {
        //        fun onButtonClicked()
        fun updateLocationText(text: String)
        fun isUserInGeofence(): Boolean
    }

    @SuppressLint("MissingSuperCall")
    fun onBackPressed() {
//        super.onBackPressed()
//        Toast.makeText(this, "Back button is disabled on this screen.", Toast.LENGTH_SHORT).show()
    }

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

        // Load data from the database and update UI
        loadProfileData()

//        if (savedInstanceState != null) {
//            currentLocation = savedInstanceState.getString("CURRENT_LOCATION")
//        }
//        updateLocationText(currentLocation ?: "Loading location...")

        //POP UP NOTIFIKASI DIALOG ABSEN
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.absenct_notification_dialog)
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(R.drawable.custom_dialog_bg)
        dialog.setCancelable(false)

        cancelImage = dialog.findViewById(R.id.cancelImage)
        time = dialog.findViewById(R.id.time)
        timeText = dialog.findViewById(R.id.timeText)
        btnAbcentDialog = dialog.findViewById(R.id.btnAbcentDialog)

        cancelImage.setOnClickListener{
            dialog.dismiss()
        }

        btnAbcentDialog.setOnClickListener{
            if (listener?.isUserInGeofence() == true) { // Jika dalam wilayah
                dialog.dismiss()
                val intent = Intent(this@HomeFragment.requireContext(), Scan::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(context, "Anda harus berada di dalam wilayah SMKN 24 Jakarta", Toast.LENGTH_LONG).show()
            }
        }

        //-----------------------------MUNCUL NOTIFIKASI-----------------------------
        if(!isWeekend){
            if(calendar.after(jamAbsen) && calendar.before(cutOffTimeMorning)){
                time.text = currentTimeFormatted

                val remainingTimeText = if (remainingTimeInMinutes > 0) {
                    "${numberToWords(remainingTimeInMinutes)} minutes left to absence"
                } else {
                    "Time to absence has passed"
                }
                timeText.text = remainingTimeText

                //MUNCUL NOTIFIKASI
                if (!dbHelperAbsensi.hasAbsensiToday(today)){
                    if (!isDialogShown) {
                        dialog.show()
                        isDialogShown = true
                    }
                }
            }
        }
        //-----------------------------MUNCUL NOTIFIKASI-----------------------------
        //POP UP NOTIFIKASI DIALOG ABSEN

        // Set listeners for buttons and views
        binding.FtProfile.setOnClickListener {
            val intent = Intent(this@HomeFragment.requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnAbcent.setOnClickListener {
            if(!isWeekend){
                if (listener?.isUserInGeofence() == true) { // Jika dalam wilayah
                    val absensiStatus = dbHelperAbsensi.getAbsensiStatus(today)
                    if (absensiStatus == null) { // Jika hari ini belum absen
                        if (calendar.before(cutOffTimeEarlyMorning)) {
                            Toast.makeText(context, "Belum bisa absen, masih jam 5 pagi", Toast.LENGTH_LONG).show()
                        } else if (calendar.before(cutOffTimeMorning)) { // Jika sudah lewat jam absen pagi
                            if (calendar.before(cutOffTimeAfternoon)) { // Sebelum jam 3 sore
                                val intent = Intent(this@HomeFragment.requireContext(), ScanActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(context, "Waktu sekolah selesai", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "Anda tidak bisa absen setelah pukul 06:30", Toast.LENGTH_LONG).show()
                        }
                    } else if (absensiStatus == "Hadir"){
                        Toast.makeText(context, "Anda sudah melakukan absen hari ini", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Anda sudah mengajukan izin hari ini", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Anda harus berada di dalam wilayah SMKN 24 Jakarta", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Hari ini hari libur, silahkan beristirahat", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnIzin.setOnClickListener {
            if(!isWeekend){
                val absensiStatus = dbHelperAbsensi.getAbsensiStatus(today)
                if (absensiStatus == null) { // Jika hari ini belum absen
                    if(calendar.before(jamIzin)){
                        val intent = Intent(this@HomeFragment.requireContext(), IzinActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(context, "Anda tidak bisa melakukan izin setelah pukul 07:00", Toast.LENGTH_LONG).show()
                    }
                } else if (absensiStatus == "Hadir"){
                    Toast.makeText(context, "Anda sudah melakukan absen hari ini", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Anda sudah mengajukan izin hari ini", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Hari ini hari libur, silahkan beristirahat", Toast.LENGTH_LONG).show()
            }
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

        val adapter = AbsensiAdapter(absensiList) { absensi ->
            Toast.makeText(context, " ${absensi.hari}, ${absensi.tanggal}", Toast.LENGTH_SHORT).show()
        }

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

        if (listener?.isUserInGeofence() == true) { // Jika dalam wilayah
            binding.location.text = "Anda berada di wilayah SMKN 24 Jakarta"
        } else if (listener?.isUserInGeofence() == false) {
            binding.location.text = "Anda berada di luar wilayah SMKN 24 Jakarta"
        } else {
            binding.location.text = "Loading location..."
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTimeUpdater()
        updateDate()
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putString("CURRENT_LOCATION", binding.location.text.toString())
//    }

    override fun onResume() {
        super.onResume()
        setupTimeUpdater()
        updateDate()
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
//        currentLocation = text
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
                binding.FtProfile.setImageResource(R.drawable.round_person_24)
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
        val greeting = when {
            isWeekend -> "Selamat berlibur!"
            hour in 4 .. 12 -> "Selamat pagi!"
            hour in 12..15 -> "Selamat siang!"
            hour in 16..18 -> "Selamat sore!"
            else -> "Selamat malam!"
        }

        binding.greetings.text = greeting
    }

    private fun setMotivations() {
        val motivation = when {
            dayOfWeek == Calendar.FRIDAY && hour > 17 && hour < 20 -> getString(R.string.Motivasi_Friday_Evening)
            dayOfWeek == Calendar.SATURDAY -> getString(R.string.Motivation_Saturday)
            dayOfWeek == Calendar.SUNDAY -> getString(R.string.Motivation_Sunday)
            hour in 4 .. 12 -> getString(R.string.Motivation_Weekday_Morning)
            hour in 12..16 -> getString(R.string.Motivation_Weekday_Afternoon)
            hour in 17..20 -> getString(R.string.Motivation_Weekday_Night)
            else -> getString(R.string.Motivation_Weekday_Midnight)
        }

        binding.motivations.text = motivation
    }

}