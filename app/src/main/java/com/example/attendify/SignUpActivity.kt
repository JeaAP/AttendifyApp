package com.example.attendify

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.attendify.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var db: DatabaseHelperLogin

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
                else -> {
                    signupDatabase(usernameSG, passwordSG)
                }
            }
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
