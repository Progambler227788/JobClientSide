package com.talhaatif.jobportalclient.firebase

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class Variables {
    companion object {
        // for dealing with user sessions, authentication details
        val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

        @get:SuppressLint("StaticFieldLeak")
        // for dealing with CRUD on db
        val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

        // for dealing with files on db storage like user profile image, or video images
        val storageRef by lazy { FirebaseStorage.getInstance().reference }

        // Function to display error messages using Toast
        fun displayErrorMessage(message: String, context: Context) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        // Function to validate email format
        fun isEmailValid(email: String): Boolean {
            val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
            return emailRegex.matches(email)
        }

        // Additional utility function to handle user sign out
        fun signOutUser() {
            auth.signOut()
        }

        // Function to upload a file to Firebase Storage and return the download URL
        fun uploadFile(
            context: Context,
            filePath: Uri,
            onComplete: (String?) -> Unit
        ) {
            val fileRef = storageRef.child("uploads/${filePath.lastPathSegment}")
            fileRef.putFile(filePath)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        onComplete(uri.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    displayErrorMessage("Upload failed: ${exception.message}", context)
                    onComplete(null)
                }
        }

        // Function to delete a file from Firebase Storage
        fun deleteFile(
            context: Context,
            fileUrl: String,
            onComplete: (Boolean) -> Unit
        ) {
            val fileRef = storageRef.storage.getReferenceFromUrl(fileUrl)
            fileRef.delete()
                .addOnSuccessListener {
                    onComplete(true)
                }
                .addOnFailureListener { exception ->
                    displayErrorMessage("Delete failed: ${exception.message}", context)
                    onComplete(false)
                }
        }
    }
}
