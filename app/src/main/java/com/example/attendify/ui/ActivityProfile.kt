package com.example.attendify.ui

import com.example.attendify.databasehelper.*
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.attendify.R
import com.example.attendify.databinding.ActivityProfileBinding

class ActivityProfile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var dbHelper: DatabaseHelperProfile

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
//        super.onBackPressed()
//        Toast.makeText(this, "Back button is disabled on this screen.", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        dbHelper = DatabaseHelperProfile(this)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProfileData()

        binding.back.setOnClickListener {
            val intent = Intent(this, ActivityMain::class.java)
            startActivity(intent)
        }

        binding.PersonalInfo.setOnClickListener{
            val intent = Intent(this, ActivityPersonalInfo::class.java)
            startActivity(intent)
        }

        binding.About.setOnClickListener{
            val intent = Intent(this, aboutApp::class.java)
            startActivity(intent)
        }
    }

    private fun loadProfileData() {
        val profile = dbHelper.getProfile()
        if (profile != null) {
            binding.nama.text = profile.nama
            if (!profile.bio.isNullOrEmpty() && profile.bio != "null") {
                binding.bio.text = profile.bio
            } else {
                binding.bio.text = "No Bio Yet"
            }

            val bitmap = profile.foto?.let {
                DatabaseHelperProfile.byteArrayToBitmap(it)
            }
            if (bitmap != null) {
                binding.FProfile.setImageBitmap(bitmap)
            } else {
                binding.FProfile.setImageResource(R.drawable.round_image_24)
            }

        } else {
            binding.nama.text = "[Nama tidak ditemukan]"
            binding.bio.text = "[Bio tidak ditemukan]"
        }
    }
}