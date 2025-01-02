package com.example.attendify

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.attendify.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: DatabaseHelperLogin

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
//        super.onBackPressed()
//        Toast.makeText(this, "Back button is disabled on this screen.", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelperLogin(this)

        binding.btnLogin.setOnClickListener {
            val usernameLG = binding.edNISN.text.toString()
            val passwordLG = binding.edPassword.text.toString()

            when {
                usernameLG.isEmpty() || passwordLG.isEmpty() -> {
                    Toast.makeText(this, "Username dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                }
                binding.edNISN.length() < 10 -> {
                    Toast.makeText(this, "NISN harus terdiri dari minimal 10 digit.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    loginDatabase(usernameLG, passwordLG)
                }
            }
        }

        binding.showPass.setOnClickListener {
            // Periksa apakah password sedang ditampilkan
            if (binding.edPassword.inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                // Sembunyikan password
                binding.edPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.showPass.setImageResource(R.drawable.baseline_visibility_off_24) // Ganti dengan icon 'visibility_off'
            } else {
                // Tampilkan password
                binding.edPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.showPass.setImageResource(R.drawable.baseline_visibility_24) // Ganti dengan icon 'visibility'
            }

            // Set kursor ke posisi akhir
            binding.edPassword.setSelection(binding.edPassword.text.length)
        }


        binding.register.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.forgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun loginDatabase(username: String, password: String) {
        when (val result = db.readUser(username, password)) {
            0 -> {  // Login successful
                val sharedPreferences = getSharedPreferences("AttendifyPrefs", MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putBoolean("isLoggedIn", true)
                    apply()
                }
                Toast.makeText(this, "Login Successful", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, CreateProfileActivity::class.java))
                finish()
            }
            1 -> Toast.makeText(this, "NISN tidak ditemukan", Toast.LENGTH_LONG).show()
            2 -> Toast.makeText(this, "Password salah", Toast.LENGTH_LONG).show()
        }
    }



    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Reset Password")

        val input = EditText(this)
        input.hint = "Enter your NISN"
        builder.setView(input)

        builder.setPositiveButton("Submit") { dialog, _ ->
            val username = input.text.toString()
            if (username.isEmpty()) {
                Toast.makeText(this, "NISN tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                handlePasswordReset(username)
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun handlePasswordReset(username: String) {
        val userExists = db.isUserExists(username) // Periksa hanya berdasarkan NISN
        if (userExists) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Enter New Password")

            val input = EditText(this)
            input.hint = "New Password"
            builder.setView(input)

            builder.setPositiveButton("Submit") { _, _ ->
                val newPassword = input.text.toString()
                if (newPassword.isEmpty()) {
                    Toast.makeText(this, "Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                } else {
                    val updated = db.updatePassword(username, newPassword)
                    if (updated) {
                        Toast.makeText(this, "Password berhasil diubah!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Gagal mengubah password!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            builder.create().show()
        } else {
            Toast.makeText(this, "NISN tidak ditemukan!", Toast.LENGTH_SHORT).show()
        }
    }
}
