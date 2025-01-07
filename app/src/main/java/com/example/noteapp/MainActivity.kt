package com.example.noteapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.Model.Note
import com.example.noteapp.Dialogs.NoteCreationDialog
import com.example.noteapp.Dialogs.EditNote
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import androidx.appcompat.widget.SearchView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var fabAddNote: FloatingActionButton
    private lateinit var searchView: SearchView

    private val notes = mutableListOf<Note>()
    private val filteredNotes = mutableListOf<Note>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance() // Firebase Authentication instance
    private val userId: String by lazy { auth.currentUser?.uid ?: "defaultUserId" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewNotes)
        fabAddNote = findViewById(R.id.fabAddNote)
        searchView = findViewById(R.id.searchView)

        // Initialize RecyclerView
        noteAdapter = NoteAdapter(filteredNotes,
            onNoteClick = { note ->
                //Edit
                val dialog = EditNote(note)
                dialog.show(supportFragmentManager, "NoteEditDialog")
            },
            onDeleteClick = { note ->
                deleteNoteFromFirebase(note)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = noteAdapter

        fetchNotesFromFirebase()

        //Add
        fabAddNote.setOnClickListener {
            val dialog = NoteCreationDialog()
            dialog.show(supportFragmentManager, "NoteCreationDialog")
        }

        // Handle search query
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterNotes(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterNotes(newText.orEmpty())
                return true
            }
        })
    }

    fun fetchNotesFromFirebase() {
        db.collection("notes")
            .document(userId)
            .collection("userNotes")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error Fetching Notes: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                notes.clear()
                snapshots?.let {
                    for (document in it.documents) {
                        val note = document.toObject(Note::class.java)
                        if (note != null) {
                            note.id = document.id // Assign document ID
                            notes.add(note)
                        }
                    }
                    filteredNotes.clear()
                    filteredNotes.addAll(notes)
                    noteAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun filterNotes(query: String) {
        val lowerCaseQuery = query.lowercase()
        filteredNotes.clear()
        filteredNotes.addAll(
            notes.filter { note ->
                note.title.lowercase().contains(lowerCaseQuery) ||
                        note.content.lowercase().contains(lowerCaseQuery)
            }
        )
        noteAdapter.notifyDataSetChanged()
    }

    private fun deleteNoteFromFirebase(note: Note) {
        val noteRef = db.collection("notes")
            .document(userId)
            .collection("userNotes")
            .document(note.id) // Use the note's unique ID

        noteRef.delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Note Deleted", Toast.LENGTH_SHORT).show()
                fetchNotesFromFirebase() // Refresh notes
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error Deleting Note: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
