package com.example.attendify

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(
    private var noteList: List<Note>, context: Context): RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

        private val db: DatabaseHelperNote = DatabaseHelperNote(context)

        class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contentTextView: TextView = itemView.findViewById(R.id.content)
            val editNote: ImageView = itemView.findViewById(R.id.editNote)
            val deleteNote: ImageView = itemView.findViewById(R.id.deleteNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun getItemCount(): Int = noteList.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = noteList[position]
        holder.contentTextView.text = note.content

        holder.editNote.setOnClickListener {
            val intent = Intent(holder.itemView.context, ActivityAddNotes::class.java)
            intent.putExtra("note_id", note.id)
            intent.putExtra("note_content", note.content)
            holder.itemView.context.startActivity(intent)
        }

        holder.deleteNote.setOnClickListener {
            db.deleteNote(note.id)
            Toast.makeText(holder.itemView.context, "Note deleted", Toast.LENGTH_SHORT).show()
            refreshData(db.getAllNotes())
        }
    }

    fun refreshData(newNoteList: List<Note>) {
        noteList = newNoteList
        notifyDataSetChanged()
    }

}