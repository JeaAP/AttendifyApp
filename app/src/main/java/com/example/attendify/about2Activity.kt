package com.example.attendify

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.attendify.databinding.ActivityAbout1Binding
import com.example.attendify.databinding.ActivityAbout2Binding

class about2Activity : AppCompatActivity() {
    private lateinit var binding: ActivityAbout2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAbout2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.next.setOnClickListener{
            val intent = Intent(this, about3Activity::class.java)
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