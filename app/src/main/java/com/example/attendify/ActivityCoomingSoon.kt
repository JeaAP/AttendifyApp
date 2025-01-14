package com.example.attendify

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.attendify.databinding.ActivityCoomingSoonBinding

class ActivityCoomingSoon : AppCompatActivity() {

    private lateinit var binding: ActivityCoomingSoonBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoomingSoonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener{
            val intent = Intent(this, ActivityMain::class.java)
            startActivity(intent)
        }
    }
}