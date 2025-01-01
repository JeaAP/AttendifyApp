package com.example.attendify

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.attendify.databinding.ActivityEditProfileBinding
import com.squareup.picasso.Picasso
import androidx.core.app.ActivityCompat

class editProfile : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var dbHelper: DatabaseHelperProfile

    private val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri: Uri? = result.uriContent
            binding.imageView.setImageURI(uri) // Display the cropped image in ImageView
        } else {
            Toast.makeText(this, "Image cropping failed: ${result.error?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        dbHelper = DatabaseHelperProfile(this)
        setContentView(binding.root)

        loadProfileData()

        binding.back.setOnClickListener {
            val intent = Intent(this, PersonalInfoActivity::class.java)
            startActivity(intent)
        }

        setupSpinner()

        binding.doneEdit.setOnClickListener {
            saveProfileData()
        }

        binding.imageView.setOnClickListener {
            pickFromGallery()
        }
    }

    private fun loadProfileData() {
        val profile = dbHelper.getProfile()
        if (profile != null) {
            binding.edNama.setText(profile.nama)
            binding.edUsername.setText(profile.username)
            binding.edAbsen.setText(profile.absen.toString())
            binding.edNisn.setText(profile.nisn)

            // Load photo if available
            if (profile.foto != null) {
                val bitmap = DatabaseHelperProfile.byteArrayToBitmap(profile.foto)
                binding.imageView.setImageBitmap(bitmap)
            } else {
                binding.imageView.setImageResource(R.drawable.baseline_camera_24)
            }

            // Set spinner selection
            val kelasArray = resources.getStringArray(R.array.kelas_array)
            val selectedIndex = kelasArray.indexOf(profile.kelas)
            if (selectedIndex >= 0) {
                binding.spinnerKelas.setSelection(selectedIndex)
            }
        }
    }

    private fun setupSpinner() {
        val kelasArray = resources.getStringArray(R.array.kelas_array)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            kelasArray
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKelas.adapter = adapter
    }

    private fun saveProfileData() {
        val nama = binding.edNama.text.toString()
        val username = binding.edUsername.text.toString()
        val kelas = binding.spinnerKelas.selectedItem.toString()
        val absen = binding.edAbsen.text.toString().toIntOrNull() ?: 0
        val nisn = binding.edNisn.text.toString()

        // Convert image to byte array
        val foto = dbHelper.imageViewToByte(binding.imageView)

        // Create Profile object
        val profile = Profile(
            id = 1, // Assuming there's only one profile, use ID = 1
            nama = nama,
            username = username,
            kelas = kelas,
            absen = absen,
            nisn = nisn,
            foto = foto
        )

        // Update profile in database
        dbHelper.upsertProfile(profile)

        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()

        // Return to previous activity
        val intent = Intent(this, PersonalInfoActivity::class.java)
        startActivity(intent)
    }

    private fun pickFromGallery() {
        if (!checkStoragePermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION
            )
        } else {
            launchImageCropper()
        }
    }

    private fun launchImageCropper() {
        cropImageLauncher.launch(CropImageContractOptions(null, CropImageOptions()))
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchImageCropper()
            } else {
                Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val STORAGE_PERMISSION = 101
    }
}
