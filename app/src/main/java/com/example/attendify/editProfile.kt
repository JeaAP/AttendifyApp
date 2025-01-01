package com.example.attendify

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.attendify.databinding.ActivityEditProfileBinding

class editProfile : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener{
            val intent = Intent(this, PersonalInfoActivity::class.java)
            startActivity(intent)
        }

        // Data kelas
        val kelasArray = arrayOf(
            "Kelas",
            "X PPL 1", "X PPL 2",
            "X TBS 1", "X TBS 2", "X TBS 3",
            "X KUL 1", "X KUL 2", "X KUL 3",
            "X PH 1", "X PH 2", "X PH 3",
            "X ULW 1",
            "XI PPL 1", "XI PPL 2",
            "XI TBS 1", "XI TBS 2", "XI TBS 3",
            "XI KUL 1", "XI KUL 2", "XI KUL 3",
            "XI PH 1", "XI PH 2", "XI PH 3",
            "XI ULW 1"
        )

        // Adapter untuk Spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            kelasArray
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKelas.adapter = adapter

        // Listener untuk Spinner
//        binding.spinnerKelas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: android.view.View?,
//                position: Int,
//                id: Long
//            ) {
//                if (position == 0) {
//                    Toast.makeText(this@editProfile , "Harap pilih kelas.", Toast.LENGTH_SHORT).show()
//                } else {
//                    val selectedClass = kelasArray[position]
//                    Toast.makeText(this@editProfile, "Kelas dipilih: $selectedClass", Toast.LENGTH_SHORT).show()
//                }
//            }

//        binding.doneEdit.setOnClickListener{
//            EDIT DATA DI DATABASE
//        }
    }
}