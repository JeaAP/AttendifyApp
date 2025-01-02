package com.example.attendify

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.attendify.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), HomeFragment.FragmentInteractionListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelperAbsensi: DatabaseHelperAbsensi
    private val homeFragment: HomeFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.frameLayout) as HomeFragment
    }

    private lateinit var geofencingClient: GeofencingClient
    private val geofenceList = mutableListOf<Geofence>()
    private val GEOFENCE_ID = "MY_GEOFENCE_ID"
    private val GEOFENCE_RADIUS = 100f // in meters
    private val GEOFENCE_LATITUDE = -6.313615160801881
    private val GEOFENCE_LONGITUDE = 106.88713091541187
    private var isInsideGeofence = false // Variabel untuk menyimpan status jankauan

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            dbHelperAbsensi = DatabaseHelperAbsensi(this)

            binding.bottomNavigationView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.home -> {
                        replaceFragment(HomeFragment())
                        true
                    }
                    R.id.people -> {
                        replaceFragment(PeopleFragment())
                        true
                    }
                    else -> false
                }
            }
            replaceFragment(HomeFragment()) // Set HomeFragment as the default fragment
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, HomeFragment())
                    .commitNow() // Ini akan memastikan bahwa fragment ditambahkan segera.
            }

            binding.fab.setOnClickListener {
                val now = Calendar.getInstance()
                val cutOffTime = Calendar.getInstance()
                cutOffTime.set(Calendar.HOUR_OF_DAY, 6)
                cutOffTime.set(Calendar.MINUTE, 30)

                if (isInsideGeofence) {
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    if (!dbHelperAbsensi.hasAbsensiToday(today)) {
                        if(now.after(cutOffTime)){
                            Toast.makeText(this, "Anda tidak bisa absen setelah pukul 06:30", Toast.LENGTH_LONG).show()
                            return@setOnClickListener
                        } else{
                            val intent = Intent(this, Scan::class.java)
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this, "Anda sudah melakukan absen hari ini", Toast.LENGTH_LONG).show()
                    }
//                    val intent = Intent(this, Scan::class.java)
//                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Anda harus berada di dalam wilayah SMKN 24 Jakarta", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //----------------------------GEOFENCING
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val userLatitude = location.latitude
                    val userLongitude = location.longitude

                    setupGeofence()
                    isInsideGeofence = isUserInsideGeofence(userLatitude, userLongitude)

                    val locationMessage = if (isInsideGeofence) {
                        "Anda berada di wilayah SMKN 24 Jakarta"
                    } else {
                        "Anda berada di luar wilayah SMKN 24 Jakarta"
                    }
                    val homeFragment = supportFragmentManager.findFragmentById(R.id.frameLayout) as? HomeFragment
                    homeFragment?.updateLocationText(locationMessage)
                }
            }
        }

        geofencingClient = LocationServices.getGeofencingClient(this)
        Log.d("Geofence", "Latitude: $GEOFENCE_LATITUDE, Longitude: $GEOFENCE_LONGITUDE, Radius: $GEOFENCE_RADIUS")

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            if (locationGranted) checkLocation()
        }
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).commit()
    }

    override fun isUserInGeofence(): Boolean {
        return isInsideGeofence
    }

//    override fun onButtonClicked() {
//        val intent = Intent(this, ScanActivity::class.java)
//        startActivity(intent)
//    }

    override fun updateLocationText(text: String) {
        // Ini akan dipanggil jika Anda perlu memperbarui teks dari MainActivity
        homeFragment.updateLocationText(text)
    }

    //GEOFENCING
    override fun onStop() {
        super.onStop()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun checkLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Lokasi Diperlukan", Toast.LENGTH_SHORT).show()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun isUserInsideGeofence(lat: Double, lon: Double): Boolean {
        val distance = FloatArray(1)
        Location.distanceBetween(
            lat, lon,
            GEOFENCE_LATITUDE, GEOFENCE_LONGITUDE,
            distance
        )
        return distance[0] <= GEOFENCE_RADIUS
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    private fun setupGeofence() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Location permission is not granted", Toast.LENGTH_SHORT).show()
            return
        }

        geofenceList.add(
            Geofence.Builder()
                .setRequestId(GEOFENCE_ID)
                .setCircularRegion(
                    GEOFENCE_LATITUDE,
                    GEOFENCE_LONGITUDE,
                    GEOFENCE_RADIUS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        )

        val pendingIntent = GeofenceHelper.getGeofencePendingIntent(this)
//        Log.d("Geofence", "PendingIntent created: $pendingIntent")

        try {
            geofencingClient.addGeofences(getGeofencingRequest(), pendingIntent)
                .addOnCompleteListener {
//                    if (it.isSuccessful) {
//                        Toast.makeText(this, "Geofence added", Toast.LENGTH_SHORT).show()
//                    } else {
//                        Toast.makeText(
//                            this,
//                            "Failed to add geofence: ${it.exception?.message}",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
                }
        } catch (e: SecurityException) {
            Toast.makeText(this, "SecurityException: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.d("Geofence", "SecurityException: ${e.message}")
        }
    }

}