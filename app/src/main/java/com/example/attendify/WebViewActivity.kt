package com.example.attendify

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.attendify.databinding.ActivityWebViewBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val webView = findViewById<WebView>(R.id.webView)
        val url = intent.getStringExtra("url")

        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true

        if(url != null) {
            webView.loadUrl(url)
        }

        binding.done.setOnClickListener {
            val intent = Intent(this, deskripsiAbsen::class.java)
            startActivity(intent)
            finish()
        }
    }
}