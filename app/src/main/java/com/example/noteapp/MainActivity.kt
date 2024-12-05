package com.example.noteapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextContent: EditText
    private lateinit var buttonSave: Button
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance() // Firebase Authentication instance
    private val userId: String by lazy { auth.currentUser?.uid ?: "defaultUserId" } // Get User ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Views
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextContent = findViewById(R.id.editTextContent)
        buttonSave = findViewById(R.id.buttonSave)

        // Fetch notes for the current user
        fetchNotesFromFirebase()

        // Save Button Click Listener
        buttonSave.setOnClickListener {
            val title = editTextTitle.text.toString().trim()
            val content = editTextContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            } else {
                saveNoteToFirebase(title, content)
            }
        }
    }

    private fun fetchNotesFromFirebase() {
        // Fetch the document for the current user ID
        db.collection("notes")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val title = document.getString("title")
                    val content = document.getString("content")
                    Toast.makeText(this, "Title: $title\nContent: $content", Toast.LENGTH_LONG).show()
                    editTextTitle.setText(title)
                    editTextContent.setText(content)
                } else {
                    Toast.makeText(this, "No notes found for this user.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error Fetching Notes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveNoteToFirebase(title: String, content: String) {
        // Save the note using the user ID as the document ID
        val note = hashMapOf(
            "title" to title,
            "content" to content
        )

        db.collection("notes")
            .document(userId)
            .set(note) // Use `set` to overwrite or create the document
            .addOnSuccessListener {
                Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error Saving Note: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
