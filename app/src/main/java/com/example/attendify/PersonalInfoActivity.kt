package com.example.attendify

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.attendify.databinding.ActivityPersonalInfoBinding

class PersonalInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalInfoBinding
    private lateinit var dbHelper: DatabaseHelperProfile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        dbHelper = DatabaseHelperProfile(this)
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProfileData()
        binding.back.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.editText.setOnClickListener{
            val intent = Intent(this, editProfile::class.java)
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
            if(profile.bio.isNotEmpty()) {
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
                binding.FotoProfile.setImageResource(R.drawable.account_circle)
            }

        } else {
            binding.nama.text = "[Nama tidak ditemukan]"
            binding.kelas.text = "[Kelas tidak ditemukan]"
            binding.abcent.text = "[Absen tidak ditemukan]"
            binding.nisn.text = "[Nisn tidak ditemukan]"
        }
    }
}