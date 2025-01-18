package com.example.attendify.api

import android.content.ContentValues.TAG
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ApiController(url: String, method: String) {
    companion object{
        const val BASE_URL = "https://backend24.site/Rian/XI/attendify/"
    }

    private var conn: HttpURLConnection = URL(BASE_URL +url).openConnection() as HttpURLConnection

    init {
        conn.requestMethod = method
        conn.doOutput = true
    }

    fun execute(postData: Map<String, Any>? = null, onResponse: (String, Int)-> Unit) {
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                postData?.let { data ->
                    val postDataString = data.entries.joinToString("&") { "${it.key}=${it.value}" }
                    conn.outputStream.use { output ->
                        OutputStreamWriter(output).use { writer ->
                            writer.write(postDataString)
                            writer.flush()
                        }
                    }
                }
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                withContext(Dispatchers.Main) {
                    onResponse(response, conn.responseCode)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResponse("Error: ${e.message}", conn.responseCode)
                }
            }
        }
    }

    fun getQrCode(onResponse: (String, Int)-> Unit) {
        ApiController("qrcode_attendify.php", "GET").execute(onResponse = onResponse)
    }

}