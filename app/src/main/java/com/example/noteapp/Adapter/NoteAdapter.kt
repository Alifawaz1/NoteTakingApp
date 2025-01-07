package com.example.noteapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.Model.Note

class NoteAdapter(
    private val notes: List<Note>,
    private val onNoteClick: (Note) -> Unit, // Callback for click
    private val onDeleteClick: (Note) -> Unit // Callback for delete
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textTitle) // Title TextView
        val contentTextView: TextView = itemView.findViewById(R.id.textContent) // Content TextView
        val deleteButton: ImageView = itemView.findViewById(R.id.imageViewDelete) // Delete button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]

        // Set the note title and content
        holder.titleTextView.text = note.title
        holder.contentTextView.text = note.content

        // Set click listeners
        holder.itemView.setOnClickListener {
            onNoteClick(note) // Open the note for editing
        }
        holder.deleteButton.setOnClickListener {
            onDeleteClick(note) // Delete the note
        }
    }

    override fun getItemCount(): Int = notes.size
}
