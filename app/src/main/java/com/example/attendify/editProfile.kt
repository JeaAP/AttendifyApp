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
    val CAMERA_REQUEST = 100
    val STORAGE_PERMISSION = 101

    val cameraPermissions: Array<String> = arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val storagePermissions: Array<String> = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

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
            var avatar = 0
            if (avatar == 0) {
                if (!checkCameraPermission()) {
                    requestCameraPersmission()
                } else {
                    pickFromGallery()
                }
            } else if (avatar == 1) {
                if (!checkStoragePermission()) {
                    requestStoragePermission()
                } else {
                    pickFromGallery()
                }
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

    private fun requestStoragePermission() {
        requestPermissions(storagePermissions, STORAGE_PERMISSION)
    }

    private fun checkStoragePermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED)
        return result
    }

    private fun pickFromGallery() {
        cropImageLauncher.launch(CropImageContractOptions(null, CropImageOptions()))
    }

    private fun requestCameraPersmission() {
        requestPermissions(cameraPermissions, CAMERA_REQUEST)
    }

    private fun checkCameraPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED)
        val result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED)
        return result && result2
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST -> {
                if (grantResults.size > 0) {
                    val cameraAccept = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccept) {
                        pickFromGallery()
                    } else {
                        Toast.makeText(this, "Enable Camera and Storage Permissions", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            STORAGE_PERMISSION -> {
                if (grantResults.size > 0) {
                    val storegaAccept = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (storegaAccept) {
                        pickFromGallery()
                    } else {
                        Toast.makeText(this, "Enable Storage Permission", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
