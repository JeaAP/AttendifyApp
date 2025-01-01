package com.example.attendify

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelperLogin(this)

        binding.btnLogin.setOnClickListener {
            val usernameLG = binding.edNISN.text.toString()
            val passwordLG = binding.edPassword.text.toString()

            if (usernameLG.isEmpty() || passwordLG.isEmpty()) {
                Toast.makeText(this, "Username dan Password tidak boleh kosong", Toast.LENGTH_LONG).show()
            } else {
                loginDatabase(usernameLG, passwordLG)
            }
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
        val userExists = db.readUser(username, password)
        if (userExists) {
            // Simpan status login ke SharedPreferences
            val sharedPreferences = getSharedPreferences("AttendifyPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", true)
            editor.apply()

            Toast.makeText(this, "Login Successful", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG).show()
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
        val userExists = db.readUser(username, "") // Check if user exists
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
