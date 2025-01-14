package com.example.attendify

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class splashscreen : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        sharedPreferences = getSharedPreferences("AttendifyPrefs", MODE_PRIVATE)

        Handler(Looper.getMainLooper()).postDelayed({
            // Cek apakah pengguna sudah login
            val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
            if (isLoggedIn) {
                // Jika sudah login, pindah ke MainActivity
                val intent = Intent(this@splashscreen, ActivityMain::class.java)
                startActivity(intent)
            } else {
                // Jika belum login, pindah ke about1Activity
                val intent = Intent(this@splashscreen, ActivityNavigation::class.java)
                startActivity(intent)
            }
            finish()
        }, 3000)
    }
}
