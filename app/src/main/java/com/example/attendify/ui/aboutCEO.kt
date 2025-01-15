package com.example.attendify.ui

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.attendify.databinding.ActivityAboutCeoBinding

class aboutCEO : AppCompatActivity() {

    private lateinit var binding: ActivityAboutCeoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutCeoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener{
            // Cek jika ada fragment dalam back stack, jika tidak, kembalikan ke Activity/Frament sebelumnya
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                super.onBackPressed() // Akan kembali ke Activity atau Fragment yang memulai ActivityA
            }
            /*  WARNING WARNING WARNING
                 onBackPressed tidak terdeteksi
             WARNING WARNING WARNING */
        }
    }
}