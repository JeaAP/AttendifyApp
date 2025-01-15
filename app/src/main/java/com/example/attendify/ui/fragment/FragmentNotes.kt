package com.example.attendify.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attendify.databasehelper.DatabaseHelperNote
import com.example.attendify.ui.adapter.NoteAdapter
import com.example.attendify.databinding.FragmentNotesBinding
import com.example.attendify.ui.ActivityAddNotes
import com.example.attendify.ui.ActivityMain

class FragmentNotes : Fragment() {

    private lateinit var binding: FragmentNotesBinding
    private lateinit var activityMain: ActivityMain
    private lateinit var databaseHelper: DatabaseHelperNote

    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        if (context is ActivityMain) {
            activityMain = context
        } else {
            throw IllegalStateException("NotesFragment must be attached to MainActivity")
        }
    }

    @SuppressLint("MissingSuperCall")
    fun onBackPressed() {
//        super.onBackPressed()
//        Toast.makeText(this, "Back button is disabled on this screen.", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotesBinding.inflate(layoutInflater)
        databaseHelper = DatabaseHelperNote(this@FragmentNotes.requireContext())

        binding.back.setOnClickListener {
//            onBackPressed()
            activityMain.replaceFragment(FragmentHome())
        }

        val noteList = databaseHelper.getAllNotes()
        binding.activityContent.layoutManager = LinearLayoutManager(this@FragmentNotes.requireContext())
        binding.activityContent.adapter = NoteAdapter(noteList, this@FragmentNotes.requireContext())

        binding.fab.setOnClickListener{
            val intent = Intent(this@FragmentNotes.requireContext(), ActivityAddNotes::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val noteList = databaseHelper.getAllNotes()
        val adapter = binding.activityContent.adapter as NoteAdapter
        adapter.refreshData(noteList)
    }
}