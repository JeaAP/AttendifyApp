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

    val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri: Uri? = result.uriContent
            Picasso.get().load(uri).into(binding.imageView)
        } else {
            val error = result.error
            error?.printStackTrace()
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

            // Simpan kelas dari database untuk diatur setelah spinner diinisialisasi
            val kelasFromDb = profile.kelas

            // Panggil setupSpinner dengan kelas yang dipilih
            setupSpinner(kelasFromDb)
        }
    }


    private fun setupSpinner(selectedKelas: String?) {
        val kelasArray = arrayOf(
            "Kelas", "X PPL 1", "X PPL 2", "X TBS 1", "X TBS 2", "X TBS 3", "X KUL 1", "X KUL 2", "X KUL 3", "X PH 1", "X PH 2", "X PH 3", "X ULW 1",
            "XI PPL 1", "XI PPL 2", "XI TBS 1", "XI TBS 2", "XI TBS 3", "XI KUL 1", "XI KUL 2", "XI KUL 3", "XI PH 1", "XI PH 2", "XI PH 3", "XI ULW 1",
            "XII PPL 1", "XII PPL 2", "XII TBS 1", "XII TBS 2", "XII TBS 3", "XII KUL 1", "XII KUL 2", "XII KUL 3", "XII PH 1", "XII PH 2", "XII PH 3", "XII ULW 1"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            kelasArray
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKelas.adapter = adapter

        // Set kelas yang dipilih jika ada
        selectedKelas?.let {
            val selectedIndex = kelasArray.indexOf(it)
            if (selectedIndex >= 0) {
                binding.spinnerKelas.setSelection(selectedIndex)
            }
        }
    }


    private fun saveProfileData() {
        val nama = binding.edNama.text.toString()
        val username = binding.edUsername.text.toString()
        val kelas = binding.spinnerKelas.selectedItem.toString()

        if (kelas == "Kelas") {
            Toast.makeText(this, "Silakan pilih kelas yang valid.", Toast.LENGTH_SHORT).show()
            return
        }
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

    //------------------------------
    private fun requestStoragePermission() {
        requestPermissions(storagePermissions, STORAGE_PERMISSION)
    }

    private fun checkStoragePermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED)
        return result
    }

    private fun pickFromGallery() {
        cropImageLauncher.launch(CropImageContractOptions(null, CropImageOptions()))
//        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivityForResult(intent, GALLERY_REQUEST_CODE)

    }

    private fun requestCameraPersmission() {
        requestPermissions(cameraPermissions, CAMERA_REQUEST)
    }

    private fun checkCameraPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED)
        val camera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED)
        return result && camera
    }
    //------------------------------

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
