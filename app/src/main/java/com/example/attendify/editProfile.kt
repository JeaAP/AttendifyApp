package com.example.attendify

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.attendify.databinding.ActivityEditProfileBinding

class editProfile : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var dbHelper: DatabaseHelperProfile

    private val cameraPermissions = arrayOf(
        Manifest.permission.CAMERA,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    )

    private val storagePermissions = arrayOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    )

    private val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri: Uri? = result.uriContent
            binding.imageView.setImageURI(uri)
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
            startActivity(Intent(this, PersonalInfoActivity::class.java))
        }

        setupSpinner()

        binding.doneEdit.setOnClickListener {
            saveProfileData()
        }

        binding.imageView.setOnClickListener {
            if (!checkCameraPermission()) {
                requestCameraPermission()
            } else {
                pickFromGallery()
            }
        }
    }

    private fun loadProfileData() {
        val profile = dbHelper.getProfile()
        if (profile != null) {
            binding.edNama.setText(profile.nama)
            binding.edUsername.setText(profile.username)
            binding.edAbsen.setText(profile.absen.toString())
            binding.edNisn.setText(profile.nisn)

            if (profile.foto != null) {
                val bitmap = DatabaseHelperProfile.byteArrayToBitmap(profile.foto)
                binding.imageView.setImageBitmap(bitmap)
            } else {
                binding.imageView.setImageResource(R.drawable.baseline_camera_24)
            }

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

        val foto = dbHelper.imageViewToByte(binding.imageView)

        val profile = Profile(
            id = 1,
            nama = nama,
            username = username,
            kelas = kelas,
            absen = absen,
            nisn = nisn,
            foto = foto
        )

        dbHelper.upsertProfile(profile)

        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()

        startActivity(Intent(this, PersonalInfoActivity::class.java))
    }

    private fun checkCameraPermission(): Boolean {
        return cameraPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST)
    }

    private fun pickFromGallery() {
        cropImageLauncher.launch(CropImageContractOptions(null, CropImageOptions()))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    pickFromGallery()
                } else {
                    Toast.makeText(this, "Enable Camera and Storage Permissions", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    pickFromGallery()
                } else {
                    Toast.makeText(this, "Enable Storage Permission", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val CAMERA_REQUEST = 100
        private const val STORAGE_PERMISSION = 101
    }
}
