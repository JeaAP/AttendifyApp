package com.example.attendify

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.attendify.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val webView = findViewById<WebView>(R.id.webView)
        val url = intent.getStringExtra("url")

        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true

        if (url != null) {
            webView.loadUrl(url)
        }

        // Tombol "done" listener
        binding.done.setOnClickListener {
            navigateToDeskripsiAbsen()
        }
    }

    override fun onBackPressed() {
        navigateToDeskripsiAbsen()
        super.onBackPressed()
    }

    private fun navigateToDeskripsiAbsen() {
        val intent = Intent(this, deskripsiAbsen::class.java)
        startActivity(intent)
        finish()
    }
}
