package com.example.attendify

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.attendify.databinding.ActivityCreateProfileBinding

class CreateProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
    }
}
