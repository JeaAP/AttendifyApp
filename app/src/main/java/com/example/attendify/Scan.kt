package com.example.attendify

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.attendify.databinding.ActivityScan2Binding
import com.example.attendify.databinding.ActivityScanBinding

class Scan : AppCompatActivity() {

    private lateinit var binding: ActivityScan2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityScan2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }

//        binding.scan.setOnClickListener {
//
//        }
    }
}