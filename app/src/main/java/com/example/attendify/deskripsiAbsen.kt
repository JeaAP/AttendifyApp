package com.example.attendify

import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.attendify.databinding.ActivityDeskripsiAbsenBinding

class deskripsiAbsen : AppCompatActivity() {

    private lateinit var binding: ActivityDeskripsiAbsenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeskripsiAbsenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val llSendMessage = binding.llSendMessage
        val llFeelings = binding.llFeelings

        binding.btnSend.setOnClickListener { // MENGIRIM DATA edDescription KE DATABASE
            llSendMessage.visibility = View.GONE
            llFeelings.visibility = View.VISIBLE
        }
    }
}