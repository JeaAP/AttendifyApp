package com.example.attendify

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.attendify.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var db: DatabaseHelperLogin

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
//        super.onBackPressed()
//        Toast.makeText(this, "Back button is disabled on this screen.", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelperLogin(this)

        // Periksa apakah database penuh
        if (db.isDatabaseFull()) {
            Toast.makeText(this, "Anda sudah pernah membuat akun sebelumnya", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSignUp.setOnClickListener {
            val usernameSG = binding.sgNISN.text.toString()
            val passwordSG = binding.sgPassword.text.toString()
            val passwordCheck = binding.sgCPassword.text.toString()

            when {
                usernameSG.isEmpty() || passwordSG.isEmpty() || passwordCheck.isEmpty() -> {
                    Toast.makeText(this, "Username dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                }
                passwordSG != passwordCheck -> {
                    Toast.makeText(this, "Konfirmasi Password salah!", Toast.LENGTH_SHORT).show()
                }
                binding.sgNISN.length() < 10 -> {
                    Toast.makeText(this, "NISN harus terdiri dari minimal 10 digit.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    signupDatabase(usernameSG, passwordSG)
                }
            }
        }

        binding.showP.setOnClickListener {
            // Periksa apakah password sedang ditampilkan
            if (binding.sgPassword.inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                // Sembunyikan password
                binding.sgPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.showP.setImageResource(R.drawable.baseline_visibility_off_24) // Ganti dengan icon 'visibility_off'
            } else {
                // Tampilkan password
                binding.sgPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.showP.setImageResource(R.drawable.baseline_visibility_24) // Ganti dengan icon 'visibility'
            }

            // Set kursor ke posisi akhir
            binding.sgPassword.setSelection(binding.sgPassword.text.length)
        }

        binding.showPs.setOnClickListener {
            // Periksa apakah password sedang ditampilkan
            if (binding.sgCPassword.inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                // Sembunyikan password
                binding.sgCPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.showPs.setImageResource(R.drawable.baseline_visibility_off_24) // Ganti dengan icon 'visibility_off'
            } else {
                // Tampilkan password
                binding.sgCPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.showPs.setImageResource(R.drawable.baseline_visibility_24) // Ganti dengan icon 'visibility'
            }

            // Set kursor ke posisi akhir
            binding.sgCPassword.setSelection(binding.sgCPassword.text.length)
        }

        binding.txtLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signupDatabase(username: String, password: String) {
        val insertedRowId = db.insertUser(username, password)
        if (insertedRowId != -1L) {
            Toast.makeText(this, "Pendaftaran Berhasil", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Anda sudah pernah membuat akun sebelumnya", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
