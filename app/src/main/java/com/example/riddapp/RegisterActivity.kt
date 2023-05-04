package com.example.riddapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etReenterPassword: EditText
    private lateinit var etMobileNo: EditText
    private lateinit var etDriverLicense: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnRegisterAsDriver: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etReenterPassword = findViewById(R.id.etReenterPassword)
        etMobileNo = findViewById(R.id.etMobileNo)
        etDriverLicense = findViewById(R.id.etDriverLicense)
       // etLocation = findViewById(R.id.etLocation)
        btnRegister = findViewById(R.id.btnRegister)
        btnRegisterAsDriver = findViewById(R.id.btnRegisterAsDriver)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        btnRegister.setOnClickListener {
            registerUser()
        }

        btnRegisterAsDriver.setOnClickListener {
            registerDriver()
        }
    }

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val reenterPassword = etReenterPassword.text.toString().trim()
        val mobileNo = etMobileNo.text.toString().trim()


        val emailPattern = "[a-zA-Z\\d._-]+@[a-z]+\\.+[a-z]+"

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || reenterPassword.isEmpty() || mobileNo.isEmpty()) {
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!email.matches(emailPattern.toRegex())) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (mobileNo.length != 11 || !mobileNo.startsWith("0")) {
            Toast.makeText(this, "Enter Valid Mobile Number", Toast.LENGTH_SHORT).show()
            return
        }

// Rest of the code for registering the user


        if (password != reenterPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Register user with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val user = User(userId, name, email, mobileNo,  false)

                    // Add user data to Firebase Realtime Database
                    database.reference.child("users").child(userId!!).setValue(user)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Registration successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Failed to register user in database",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun registerDriver() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val reenterPassword = etReenterPassword.text.toString().trim()
        val mobileNo = etMobileNo.text.toString().trim()
        val driverLicense = etDriverLicense.text.toString().trim()


        val emailPattern = "[a-zA-Z\\d._-]+@[a-z]+\\.+[a-z]+"

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || reenterPassword.isEmpty() || mobileNo.isEmpty() || driverLicense.isEmpty()) {
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!email.matches(emailPattern.toRegex())) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (mobileNo.length != 11 || !mobileNo.startsWith("0")) {
            Toast.makeText(this, "Enter Valid Mobile Number", Toast.LENGTH_SHORT).show()
            return
        }

// Rest of the code for registering the user


// Rest of the code for registering the user

        if (password != reenterPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Register user with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val user = User(userId, name, email, mobileNo, true, driverLicense)

                    // Add user data to Firebase Realtime Database
                    database.reference.child("users").child(userId!!).setValue(user)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Registration successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Failed to register driver in database",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

