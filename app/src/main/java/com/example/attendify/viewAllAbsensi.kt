package com.example.attendify

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attendify.databinding.ActivityViewAllAbsensiBinding

class viewAllAbsensi : AppCompatActivity() {
    private lateinit var databaseHelper: DatabaseHelperAbsensi
    private lateinit var binding: ActivityViewAllAbsensiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAllAbsensiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelperAbsensi(this)

        // Delete old absensi exceeding 30 days
        databaseHelper.deleteOldAbsensi()

        val absensiList = databaseHelper.getAllAbsensi()
        val adapter = AbsensiAdapter(absensiList)

        binding.activityContent.apply {
            layoutManager = LinearLayoutManager(this@viewAllAbsensi)
            this.adapter = adapter
        }

        binding.back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}