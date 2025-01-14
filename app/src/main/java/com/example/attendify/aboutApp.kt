package com.example.attendify

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.attendify.databinding.ActivityAboutAppBinding

class aboutApp : AppCompatActivity() {

    private lateinit var binding: ActivityAboutAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.EasterEgg.setOnClickListener{
            Toast.makeText(this, "Fun fact : aplikasi ini hanya dibuat dalam waktu 3 hari 3 malam", Toast.LENGTH_SHORT).show()
        }

        binding.back.setOnClickListener{
            val intent = Intent(this, ActivityProfile::class.java)
            startActivity(intent)
        }
    }
}