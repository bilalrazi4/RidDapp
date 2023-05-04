package com.example.riddapp


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class EditUser : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var btnResetPassword: Button
    private lateinit var btnChangeName:Button
    private lateinit var editName: EditText

   override fun onBackPressed() {
        // go back to the UserActivity
        val intent = Intent(this, UserActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)
        btnResetPassword=findViewById(R.id.btnResetPass)
        btnChangeName=findViewById(R.id.btnEditName)
        editName = findViewById(R.id.editName)
        firebaseAuth = FirebaseAuth.getInstance()

        // Set click listener for "Reset Password" button
        btnResetPassword.setOnClickListener {
            resetPassword()
        }

        btnChangeName.setOnClickListener {
            editName.visibility = View.VISIBLE

        }
        editName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Get the current user ID
                val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
                currentUser?.let { user ->
                    val userId: String = user.uid

                    // Update the user's name in the Firebase Realtime Database
                    val database = FirebaseDatabase.getInstance()
                    val usersRef = database.getReference("users")
                    val newName = editName.text.toString()
                    usersRef.child(userId).child("name").setValue(newName)
                }

                // Hide the EditText view and show a confirmation message
                editName.visibility = View.INVISIBLE
                Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show()

                // Return true to indicate that the action was handled
                true
            } else {
                // Return false to indicate that the action was not handled
                false
            }
        }
    }

    // Function to handle password reset
    private fun resetPassword() {
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            firebaseAuth.sendPasswordResetEmail(currentUser.email!!)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Password reset email sent successfully
                        Toast.makeText(
                            applicationContext,
                            "Password reset email sent.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Failed to send password reset email
                        Toast.makeText(
                            applicationContext,
                            "Failed to send password reset email.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            // No user is currently logged in
            Toast.makeText(
                applicationContext,
                "No user is currently logged in.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
