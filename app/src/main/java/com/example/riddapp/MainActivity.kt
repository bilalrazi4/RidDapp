package com.example.riddapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth and Realtime Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        usersRef = database.getReference("users")

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnResetPassword=findViewById<Button>(R.id.btnResetPass)


        btnResetPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email)
            } else {
                // Email input field is empty
                // You can show an error message or take other actions as needed
                Toast.makeText(
                    this@MainActivity,
                    "Email field is empty",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {



            // Sign in with email and password
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Login successful
                        val user = auth.currentUser
                        // Check if user is a driver or not
                        val userId = user?.uid
                        if (userId != null) {
                            usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val isDriver = snapshot.child("driver").getValue(Boolean::class.java)
                                    if (isDriver == true) {
                                        // User is a driver
                                        // Perform driver specific actions
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Logged in as Driver",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        startActivity(Intent(this@MainActivity, DriverActivity::class.java))
                                    } else {
                                        // User is not a driver
                                        // Perform user specific actions
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Logged in as User",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        startActivity(Intent(this@MainActivity, UserActivity::class.java))
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Handle error
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Failed to fetch user data",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                        }
                    } else {
                        // Login failed
                        // Handle error
                        Toast.makeText(
                            this@MainActivity,
                            "Login failed. Please check your credentials.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                // Email input field is empty
                // You can show an error message or take other actions as needed
                Toast.makeText(
                    this@MainActivity,
                    "Enter Email/Password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnRegister.setOnClickListener {
            // Navigate to RegisterActivity
            startActivity(Intent(this, RegisterActivity::class.java))
        }

    }
    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Password reset email sent successfully
                    // You can show a success message or take other actions as needed
                    println("Password reset email sent successfully")
                } else {
                    // Password reset email sending failed
                    // You can show an error message or take other actions as needed
                    println("Failed to send password reset email: ${task.exception?.message}")
                }
            }
    }
}
