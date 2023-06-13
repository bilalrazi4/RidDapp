package com.example.riddapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import android.widget.TextView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import kotlinx.coroutines.*
import java.lang.Math.*

class RideAcceptUser : AppCompatActivity() {
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var currentUserID: String
    private lateinit var driverNameTextView: TextView
    private lateinit var driverNumberTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var carNameTextView: TextView
    private lateinit var carColorTextView: TextView
    private lateinit var carModelTextView: TextView
    private lateinit var numberPlateTextView: TextView
    private var backgroundJob: Job? = null
    private lateinit var daatabase: DatabaseReference
    private val EARTH_RADIUS = 6371
    private lateinit var ridesRef:DatabaseReference
    private val handler = Handler()
    private val delayMillis = 1000L // 1 second

    private val runnable = object : Runnable {
        override fun run() {
            // Call your function here
            searchPendingRidesTableForCurrentUser(currentUserID)

            // Schedule the next execution
            handler.postDelayed(this, delayMillis)
        }
    }
    private fun searchPendingRidesTableForCurrentUser(currentUserId: String) {
        val database = FirebaseDatabase.getInstance()
        val ridesRef = database.getReference("PendingRides")

        ridesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Iterate through each child node in the "pending_rides" table
                    for (rideSnapshot in snapshot.children) {
                        val userId = rideSnapshot.child("userId").value.toString()

                        // Check if the current user ID matches the user ID in the table
                        if (currentUserId == userId) {
                            // Current user ID found in the "userId" field
                            println("Current user ID found in the pending rides table.")

                            // Perform any further actions or retrieve data as needed
                        }
                        else
                        {
                            handler.removeCallbacks(runnable)
                            val intent = Intent(this@RideAcceptUser, UserThankyou::class.java)
                            startActivity(intent)
                        }
                    }
                } else {
                    // The "pending_rides" table does not exist
                    println("The pending rides table does not exist.")
                    handler.removeCallbacks(runnable)
                    val intent = Intent(this@RideAcceptUser, UserThankyou::class.java)
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error if necessary
                println("Error occurred: ${error.message}")
            }
        })
    }

    private fun checkPendingRidesTables() {
        Toast.makeText(this@RideAcceptUser, "working", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ride_accept_user)


        currentUserID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        driverNameTextView = findViewById(R.id.driverNameTextView)
        driverNumberTextView = findViewById(R.id.driverNumberTextView)
        distanceTextView = findViewById(R.id.distanceTextView)
        carColorTextView=findViewById(R.id.carColorTextView)
        carModelTextView=findViewById(R.id.carModelTextView)
        carNameTextView=findViewById(R.id.carNameTextView)
        numberPlateTextView=findViewById(R.id.numberPlateTextView)
        val database = FirebaseDatabase.getInstance()
        ridesRef = database.getReference("PendingRides")


        // Check if current user ID is present in the pending rides table
        checkIfCurrentUserExistsInPendingRides()
        handler.postDelayed(runnable, delayMillis)



    }
    override fun onDestroy() {
        super.onDestroy()

        // Stop the repeated execution when the activity is destroyed
        handler.removeCallbacks(runnable)
    }


    private fun checkIfCurrentUserExistsInPendingRides() {
        databaseReference.child("PendingRides").addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (rideSnapshot in snapshot.children) {
                            val userID = rideSnapshot.child("userId").value.toString()
                            if (userID == currentUserID) {
                                // Data with current user ID exists in the pending rides table
                                println("Data with user ID $currentUserID exists in the pending rides table.")
                                val driverId = rideSnapshot.child("driverId").value.toString()
                                val driverLat = rideSnapshot.child("driverLat").value.toString().toDouble()
                                val driverLong = rideSnapshot.child("driverLong").value.toString().toDouble()
                                val driverName = rideSnapshot.child("driverName").value.toString()
                                val driverNumber = rideSnapshot.child("driverNumber").value.toString()
                                val latitude = rideSnapshot.child("latitude").value.toString().toDouble()
                                val longitude = rideSnapshot.child("longitude").value.toString().toDouble()

                                val distance = calculateDistance(driverLat, driverLong, latitude, longitude)

                                // Use the retrieved data as needed
                                println("Driver ID: $driverId")
                                println("Driver Latitude: $driverLat")
                                println("Driver Longitude: $driverLong")
                                println("Driver Name: $driverName")
                                println("Driver Number: $driverNumber")
                                println("Latitude: $latitude")
                                println("Longitude: $longitude")

                                driverNameTextView.text = "Driver Name: $driverName"
                                driverNumberTextView.text = "Driver Number: $driverNumber"
                                distanceTextView.text = "Distance: $distance km"

                                retrieveDriverInformation(driverId, driverLat, driverLong, latitude, longitude)
                                break
                            }
                        }
                    } else {
                        // Data with current user ID does not exist in the pending rides table
                        println("Data with user ID $currentUserID does not exist in the pending rides table.")
                    }
                }


            override fun onCancelled(error: DatabaseError) {
                // Handle the error if necessary
                println("Error occurred: ${error.message}")
            }
        })
    }
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = EARTH_RADIUS * c
        return (distance * 100).toFloat() / 100 // Round to 2 decimal places and return as Float
    }
    private fun retrieveDriverInformation(driverId: String, driverLat: Double, driverLong: Double, latitude: Double, longitude: Double) {
        databaseReference.child("Vehicles").child(driverId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").value.toString()
                    val model = snapshot.child("model").value.toString()
                    val color = snapshot.child("color").value.toString()
                    val numberPlate = snapshot.child("numberPlater").value.toString()

                    carNameTextView.text = "Car Name: $name"
                    carModelTextView.text = "Car Model: $model"
                    carColorTextView.text = "Car Color: $color"
                    numberPlateTextView.text = "Number Plate: $numberPlate"



                } else {
                    // The driver's vehicle information does not exist
                    println("Driver's vehicle information does not exist.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error if necessary
                println("Error occurred: ${error.message}")
            }
        })
    }
}