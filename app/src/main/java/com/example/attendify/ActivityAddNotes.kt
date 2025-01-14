package com.example.attendify

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.attendify.databinding.ActivityAddNotesBinding

class ActivityAddNotes : AppCompatActivity() {

    private lateinit var binding: ActivityAddNotesBinding
    private lateinit var dbHelper: DatabaseHelperNote
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelperNote(this)

        binding.fab.setOnClickListener {
            if (intent.hasExtra("note_id")) {
                updateNote()
            } else {
                val noteContent = binding.edtNote.text.toString()
                dbHelper.insertNote(noteContent)
                finish()
            }
        }

        binding.back.setOnClickListener {
            val intent = Intent(this, ActivityMain::class.java)
            startActivity(intent)
            finish()
        }

        if (intent.hasExtra("note_id")) {
            binding.edtNote.setText(intent.getStringExtra("note_content"))
        }
    }

    fun updateNote(){
        val noteId = intent.getIntExtra("note_id", -1)
        val noteContent = binding.edtNote.text.toString()
        dbHelper.updateNote(noteId, noteContent)
        finish()
    }
}