package com.example.attendify

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.attendify.databinding.ActivityPersonalInfoBinding

class ActivityPersonalInfo : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalInfoBinding
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
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProfileData()
        binding.back.setOnClickListener{
            val intent = Intent(this, ActivityProfile::class.java)
            startActivity(intent)
        }

        binding.editText.setOnClickListener{
            val intent = Intent(this, ActivityEditProfile::class.java)
            startActivity(intent)
        }
    }

    private fun loadProfileData() {
        val profile = dbHelper.getProfile()
        if (profile != null) {
            binding.nama.text = profile.nama
            binding.kelas.text = profile.kelas
            binding.abcent.text = profile.absen.toString()
            binding.nisn.text = profile.nisn
            if (!profile.bio.isNullOrEmpty() && profile.bio != "null") {
                binding.bio.text = profile.bio
            } else {
                binding.bio.text = "No Bio Yet"
            }

            val bitmap = profile.foto?.let {
                DatabaseHelperProfile.byteArrayToBitmap(it)
            }
            if (bitmap != null) {
                binding.FotoProfile.setImageBitmap(bitmap)
            } else {
                binding.FotoProfile.setImageResource(R.drawable.profile___iconly_pro)
            }

        } else {
            binding.nama.text = "[Nama tidak ditemukan]"
            binding.kelas.text = "[Kelas tidak ditemukan]"
            binding.abcent.text = "[Absen tidak ditemukan]"
            binding.nisn.text = "[Nisn tidak ditemukan]"
        }
    }
}