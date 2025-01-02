package com.example.attendify

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.attendify.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val webView = findViewById<WebView>(R.id.webView)
        val url = intent.getStringExtra("url")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Setelah halaman selesai dimuat, pindah ke deskripsiAbsen
                navigateToDeskripsiAbsen()
            }
        }

        webView.settings.javaScriptEnabled = true

        if (url != null) {
            webView.loadUrl(url)
        }
    }

    private fun navigateToDeskripsiAbsen() {
        val intent = Intent(this, deskripsiAbsen::class.java)
        startActivity(intent)
        finish() // Menutup WebViewActivity agar tidak kembali lagi
    }
}
