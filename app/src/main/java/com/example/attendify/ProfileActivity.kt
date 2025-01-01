package com.example.attendify

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.attendify.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var dbHelper: DatabaseHelperProfile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        dbHelper = DatabaseHelperProfile(this)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProfileData()

        binding.back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.PersonalInfoBtn.setOnClickListener{
            val intent = Intent(this, PersonalInfoActivity::class.java)
            startActivity(intent)
        }

        binding.AboutBtn.setOnClickListener{
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
                binding.FotoProfile.setImageBitmap(bitmap)
            } else {
                binding.FotoProfile.setImageResource(R.drawable.round_person_24)
            }

        } else {
            binding.nama.text = "[Nama tidak ditemukan]"
            binding.bio.text = "[Bio tidak ditemukan]"
        }
    }
}