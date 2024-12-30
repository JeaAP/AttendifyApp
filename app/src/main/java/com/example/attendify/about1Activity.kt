package com.example.attendify

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.attendify.databinding.ActivityAbout1Binding

class about1Activity : AppCompatActivity() {
    private lateinit var binding: ActivityAbout1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityAbout1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.next.setOnClickListener{
            val intent = Intent(this, about2Activity::class.java)
            startActivity(intent)
            finish()
        }
        binding.skip.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}