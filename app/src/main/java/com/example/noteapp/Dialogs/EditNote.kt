package com.example.noteapp.Dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.noteapp.MainActivity
import com.example.noteapp.Model.Note
import com.example.noteapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditNote(private val note: Note) : DialogFragment() { // Fix inheritance

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String by lazy { auth.currentUser?.uid ?: "defaultUserId" }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_note, null)

        val editTextTitle = dialogView.findViewById<EditText>(R.id.editTextDialogTitle)
        val editTextContent = dialogView.findViewById<EditText>(R.id.editTextDialogContent)

//already hotton bi alba
        editTextTitle.setText(note.title)
        editTextContent.setText(note.content)

        builder.setView(dialogView)
            .setTitle("Edit Note")
            .setPositiveButton("Save") { _, _ ->
                val updatedTitle = editTextTitle.text.toString().trim()
                val updatedContent = editTextContent.text.toString().trim()
                if (updatedTitle.isEmpty() || updatedContent.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
                } else {
                    updateNoteInFirebase(note, updatedTitle, updatedContent)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        return builder.create()
    }

    private fun updateNoteInFirebase(note: Note, updatedTitle: String, updatedContent: String) {
        val noteRef = db.collection("notes")
            .document(userId)
            .collection("userNotes")
            .document(note.id)

        noteRef.update("title", updatedTitle, "content", updatedContent)
            .addOnSuccessListener {

                //to refresh fast
                if (isAdded) {
                    Toast.makeText(requireContext(), "Note Updated", Toast.LENGTH_SHORT).show()
                    (activity as? MainActivity)?.fetchNotesFromFirebase() // Refresh notes
                    dismiss() // Close the dialog
                }
            }
            .addOnFailureListener { e ->
                // Check if the fragment is still attached before interacting with the context
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error Updating Note: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

}
