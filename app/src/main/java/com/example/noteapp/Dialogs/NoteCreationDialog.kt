package com.example.noteapp.Dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.noteapp.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.noteapp.R

class NoteCreationDialog : DialogFragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String by lazy { auth.currentUser?.uid ?: "defaultUserId" }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_note, null)

        val editTextTitle = dialogView.findViewById<EditText>(R.id.editTextDialogTitle)
        val editTextContent = dialogView.findViewById<EditText>(R.id.editTextDialogContent)

        builder.setView(dialogView)
            .setTitle("Add Note")
            .setPositiveButton("Save") { _, _ ->
                val title = editTextTitle.text.toString().trim()
                val content = editTextContent.text.toString().trim()
                if (title.isEmpty() || content.isEmpty()) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    saveNoteToFirebase(title, content)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        return builder.create()
    }

    private fun saveNoteToFirebase(title: String, content: String) {
        val note = hashMapOf(
            "title" to title,
            "content" to content
        )

        db.collection("notes")
            .document(userId)
            .collection("userNotes")
            .add(note)
            .addOnSuccessListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Note Saved", Toast.LENGTH_SHORT).show()
                    (activity as? MainActivity)?.fetchNotesFromFirebase()
                    dismiss()
                }
            }
            .addOnFailureListener { e ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error Saving Note: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


}
