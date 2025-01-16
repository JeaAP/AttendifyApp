package com.example.attendify.ui

import androidx.annotation.OptIn
import com.google.mlkit.vision.barcode.common.Barcode
import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.attendify.databinding.ActivityScan2Binding
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Scan : AppCompatActivity() {

    private lateinit var binding: ActivityScan2Binding
    private lateinit var previewView: PreviewView
    private lateinit var resultTextView: TextView
    private lateinit var cameraExecutor: ExecutorService
    private val TARGET_URL = "https://get-qr.com/cpBKH0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityScan2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        previewView = binding.previewView
        resultTextView = binding.info
        cameraExecutor = Executors.newSingleThreadExecutor()

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) startCamera() else resultTextView.text = "Camera permission is required"
        }
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)

        binding.back.setOnClickListener {
            startActivity(Intent(this, ActivityMain::class.java))
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also {
                    it.setAnalyzer(cameraExecutor, ::processImageProxy)
                }
            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        imageProxy.image?.let {
            val image = InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees)
            BarcodeScanning.getClient().process(image)
                .addOnSuccessListener { barcodes ->
                    barcodes.forEach { handleBarcode(it) }
                }
                .addOnFailureListener {
                    resultTextView.text = "Failed to Scan QR Code"
                }
                .addOnCompleteListener { imageProxy.close() }
        }
    }

    private fun handleBarcode(barcode: Barcode) {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        val url = barcode.url?.url ?: barcode.displayValue
        if (url != null) {
            binding.scan.setOnClickListener {
                if (isConnected()) {
                    if (networkInfo != null && networkInfo.isConnected) { // Cek koneksi internet
                        if (url == TARGET_URL) {
                            resultTextView.text = "QR Detected"
                            Toast.makeText(this, "QR Detected!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, ActivityDeskripsiAbsen::class.java))
                        } else {
                            resultTextView.text = "QR tidak valid untuk absen"
                            Toast.makeText(this, "URL tidak valid untuk absen.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        AlertDialog.Builder(this@Scan)
                            .setTitle("Koneksi Internet Tidak Tersedia")
                            .setMessage("Silakan periksa koneksi internet Anda dan coba lagi.")
                            .setPositiveButton("Coba Lagi") { dialog, _ ->
                                recreate() // Reload activity untuk mencoba kembali
                                dialog.dismiss()
                            }
                            .setNegativeButton("Tutup") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .setCancelable(false)
                            .show()
                    }
                } else {
                    showNoInternetDialog()
                }
            }
        } else {
            resultTextView.text = "No QR Code detected"
        }
    }

    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo?.isConnected == true
    }

    private fun showNoInternetDialog() {
        AlertDialog.Builder(this)
            .setTitle("Koneksi Internet Tidak Tersedia")
            .setMessage("Periksa koneksi internet Anda.")
            .setPositiveButton("Coba Lagi") { dialog, _ -> recreate(); dialog.dismiss() }
            .setNegativeButton("Tutup") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
