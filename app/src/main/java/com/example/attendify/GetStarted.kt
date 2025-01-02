package com.example.attendify

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.attendify.databinding.ActivityGetStartedBinding

class GetStarted : AppCompatActivity() {

    private lateinit var bindnig: ActivityGetStartedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        bindnig = ActivityGetStartedBinding.inflate(layoutInflater)
        setContentView(bindnig.root)
    }
}