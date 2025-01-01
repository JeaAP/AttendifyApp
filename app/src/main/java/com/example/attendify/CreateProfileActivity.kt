package com.example.attendify

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.attendify.databinding.ActivityCreateProfileBinding

class CreateProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateProfileBinding
    private lateinit var db: DatabaseHelperProfile
    private lateinit var dbLogin: DatabaseHelperLogin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = DatabaseHelperProfile(this)
        dbLogin = DatabaseHelperLogin(this)

        val nisn = dbLogin.getLoggedInNisn()
        if (nisn != null) {
            binding.edNisn.setText(nisn)
            binding.edNisn.isEnabled = false
        } else {
            Toast.makeText(this, "Gagal memuat NISN.", Toast.LENGTH_SHORT).show()
        }

        val kelasArray = resources.getStringArray(R.array.kelas_array)

        // Adapter untuk Spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            kelasArray
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKelas.adapter = adapter

        // Listener untuk Spinner
        binding.spinnerKelas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    Toast.makeText(this@CreateProfileActivity, "Harap pilih kelas.", Toast.LENGTH_SHORT).show()
                } else {
                    val selectedClass = kelasArray[position]
                    Toast.makeText(this@CreateProfileActivity, "Kelas dipilih: $selectedClass", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Tidak ada aksi ketika tidak ada item dipilih
            }
        }

        binding.btnCreate.setOnClickListener {
            saveProfile()
        }
    }

    private fun saveProfile() {
        val nama = binding.edNama.text.toString().trim()
        val username = binding.edUsername.text.toString().trim()
        val kelas = binding.spinnerKelas.selectedItem.toString()
        val absen = binding.edAbsen.text.toString().trim()
        val nisn = binding.edNisn.text.toString().trim()

        if (nama.isEmpty() || username.isEmpty() || kelas == "Kelas" || absen.isEmpty() || nisn.isEmpty()) {
            Toast.makeText(this, "Harap lengkapi semua data.", Toast.LENGTH_SHORT).show()
            return
        }

        val absenInt = absen.toIntOrNull()
        if (absenInt == null) {
            Toast.makeText(this, "Absen harus berupa angka.", Toast.LENGTH_SHORT).show()
            return
        }

        val profile = Profile(
            id = 0,
            nama = nama,
            username = username,
            kelas = kelas,
            absen = absenInt,
            nisn = nisn,
            foto = null
        )

        db.upsertProfile(profile)
        Toast.makeText(this, "Profil berhasil dibuat.", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}
