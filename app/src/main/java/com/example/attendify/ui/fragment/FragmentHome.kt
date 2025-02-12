package com.example.attendify.ui.fragment

import com.example.attendify.ui.*
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attendify.databasehelper.DatabaseHelperAbsensi
import com.example.attendify.databasehelper.DatabaseHelperProfile
import com.example.attendify.R
import com.example.attendify.ui.adapter.AbsensiAdapter
import com.example.attendify.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.Manifest
import android.net.ConnectivityManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import java.io.IOException

class FragmentHome : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var activityMain: ActivityMain
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

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val REQUEST_CODE = 1002

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

    //WAKTU TELAT (TAMBAHAN KALAU MAU)

    private val remainingTimeInMinutes = ((cutOffTimeMorning.timeInMillis - calendar.timeInMillis) / 60000).toInt()

    private val currentTimeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    //======WAKTU========

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
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater)
        dbHelperProfile = DatabaseHelperProfile(requireContext())
        dbHelperAbsensi = DatabaseHelperAbsensi(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Load data from the database and update UI
        loadProfileData()

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
            val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo

            if (listener?.isUserInGeofence() == true) { // Jika dalam wilayah
                if (networkInfo != null && networkInfo.isConnected) { // Cek koneksi internet
                    val absensiStatus = dbHelperAbsensi.getAbsensiStatus(today)
                    if (absensiStatus == null) { // Jika hari ini belum absen
                        if (calendar.before(cutOffTimeAfternoon)) { // Sebelum jam 3 sore
                            val intent = Intent(this@FragmentHome.requireContext(), ActivityScan::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(context, "Waktu sekolah selesai", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Anda sudah melakukan absen hari ini", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Anda harus berada di dalam wilayah SMKN 24 Jakarta", Toast.LENGTH_SHORT).show()
                }
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle("Koneksi Internet Tidak Tersedia")
                    .setMessage("Silakan periksa koneksi internet Anda dan coba lagi.")
                    .setPositiveButton("Coba Lagi") { dialog, _ ->
                        requireActivity().recreate() // Ganti recreate() dengan requireActivity().recreate()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Tutup") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
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
                if (isAdded && !dbHelperAbsensi.hasAbsensiToday(today)) {
                    dialog.show()
                }
            }
        }
        //-----------------------------MUNCUL NOTIFIKASI-----------------------------
        //POP UP NOTIFIKASI DIALOG ABSEN

        // Set listeners for buttons and views
        binding.FtProfile.setOnClickListener {
            val intent = Intent(this@FragmentHome.requireContext(), ActivityProfile::class.java)
            startActivity(intent)
        }

        binding.btnAbcent.setOnClickListener {
            val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo

            if(!isWeekend){
                if (listener?.isUserInGeofence() == true) { // Jika dalam wilayah
                    if (networkInfo != null && networkInfo.isConnected) { // Cek koneksi internet
                        val absensiStatus = dbHelperAbsensi.getAbsensiStatus(today)
                        if (absensiStatus == null) { // Jika hari ini belum absen
                            if (calendar.before(cutOffTimeAfternoon)) { // Sebelum jam 3 sore
                                val intent = Intent(this@FragmentHome.requireContext(), ActivityScan::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(context, "Waktu sekolah selesai", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Anda sudah melakukan absen hari ini", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Anda harus berada di dalam wilayah SMKN 24 Jakarta", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    AlertDialog.Builder(requireContext()) // Ganti this@FragmentHome dengan requireContext()
                        .setTitle("Koneksi Internet Tidak Tersedia")
                        .setMessage("Silakan periksa koneksi internet Anda dan coba lagi.")
                        .setPositiveButton("Coba Lagi") { dialog, _ ->
                            requireActivity().recreate() // Ganti recreate() dengan requireActivity().recreate()
                            dialog.dismiss()
                        }
                        .setNegativeButton("Tutup") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setCancelable(false)
                        .show()
                }
            } else {
                Toast.makeText(context, "Hari ini hari libur, silahkan beristirahat", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnNote.setOnClickListener {
            activityMain = (requireActivity() as ActivityMain)
            activityMain.replaceFragment(FragmentNotes())
        }

        binding.lainnya.setOnClickListener {
            val intent = Intent(this@FragmentHome.requireContext(), ActivityCoomingSoon::class.java)
            startActivity(intent)
        }

        binding.cardSchedule.setOnClickListener {
            val url = "https://docs.google.com/spreadsheets/d/1eM7gvonky0HdSKDPUxjs9o9IZjeC28Ud/edit?pli=1&gid=1682312915#gid=1682312915"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        val absensiList = dbHelperAbsensi.getLimitedAbsensi()

        val (totalKehadiran, terlambat) = dbHelperAbsensi.hitungAbsensi()
        binding.txtKehadiran.text = "${totalKehadiran} Times"
        binding.txtKeterlambatan.text = "${terlambat} Times"

        val (persentaseKehadiran, persentaseTerlambat) = dbHelperAbsensi.hitungPersentaseAbsensi()
        binding.persentaseKehadiran.text = "Persentase kehadiran bulan ini ${persentaseKehadiran.toInt()}%"
        binding.persentaseKeterlembatan.text = "Persentase terlambat bulan ini ${persentaseTerlambat.toInt()}%"

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
            binding.locatioan.text = "Anda berada di wilayah SMKN 24 Jakarta"
            binding.dtLocation.visibility = View.VISIBLE
            checkLocationPermissionAndFetch()
        } else {
            binding.locatioan.text = "Anda berada di luar wilayah SMKN 24 Jakarta"
            binding.dtLocation.visibility = View.VISIBLE
            checkLocationPermissionAndFetch()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateDate()
    }
//    }

    override fun onResume() {
        super.onResume()
        updateDate()
    }

    // Fungsi untuk memeriksa izin lokasi dan mendapatkan lokasi
    private fun checkLocationPermissionAndFetch() {
        // Pastikan fragment masih terhubung ke context dan activity
        context?.let {
            if (ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getLastLocation()
            } else {
                // Minta izin
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun getLastLocation() {
        // Pastikan fragment masih terhubung ke context dan activity
        context?.let {
            if (ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude

                        val geocoder = Geocoder(it, Locale.getDefault())
                        try {
                            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                            val address = addresses?.get(0)?.getAddressLine(0)
                            if (address != null) {
                                binding.dtLocation.text = "$address"
                            } else {
                                binding.dtLocation.text = "Tidak dapat menemukan nama lokasi."
                            }
                        } catch (e: IOException) {
                            binding.dtLocation.text = "Error mendapatkan alamat: ${e.message}"
                        }
                    } else {
                        binding.dtLocation.text = "Gagal mendapatkan lokasi."
                    }
                }.addOnFailureListener {
                    binding.dtLocation.text = "Gagal mendapatkan lokasi."
                }
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
            }
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getLastLocation()
        } else {
            binding.dtLocation.text = "Izin lokasi ditolak."
        }
    }

    fun updateLocationText(text: String) {
        if (isAdded) {
            binding.locatioan.text = text
        }
    }

    private fun updateDate() {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.ENGLISH)
        val currentDate = dateFormat.format(Date())
        binding.date.text = currentDate
    }

    private fun loadProfileData() {
        val profile = dbHelperProfile.getProfile()
        if (profile != null) {
//            binding.accountName.text = profile.nama
//            binding.accountClass.text = profile.kelas
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
//            binding.accountName.text = "[Nama tidak ditemukan]"
//            binding.accountClass.text = "[Kelas tidak ditemukan]"
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
}
