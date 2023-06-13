package com.example.riddapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.maps.DirectionsApi
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import kotlinx.coroutines.*
//import kotlinx.coroutines.DefaultExecutor.isActive
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


class UserActivity() : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private var currentLocation: Location? = null
    private lateinit var etLocationSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnBook: Button
    private lateinit var btnShowDirection: Button
    private lateinit var tvDistance: TextView
    private lateinit var destination: LatLng
    private lateinit var tvFare: TextView
    private var currentLocationDirection: LatLng?= null
    private  var destinationLocation: LatLng?=null
    private  var rideBooking: Boolean =false
    private lateinit var findingDriverTextView: TextView
    private lateinit var btnCancel:Button
    private var backgroundJob: Job? = null
    private lateinit var daatabase: DatabaseReference
    private var currentUser: FirebaseUser? = null
    private var fare by Delegates.notNull<Float>()



    constructor(parcel: Parcel) : this() {
        currentLocation = parcel.readParcelable(Location::class.java.classLoader)
    }

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        etLocationSearch = findViewById(R.id.etLocationSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnBook = findViewById(R.id.btnBookRide);
        tvDistance = findViewById(R.id.tvDistance);
        tvFare = findViewById(R.id.Fare);
        btnShowDirection = findViewById(R.id.btnShowDirections);
        findingDriverTextView = findViewById(R.id.findingDriverTextView)
        btnCancel=findViewById(R.id.btnCancel)
        daatabase = FirebaseDatabase.getInstance().reference
        currentUser = FirebaseAuth.getInstance().currentUser

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowHomeEnabled(false)
            supportActionBar?.setDisplayShowTitleEnabled(false)

        }

        // Initialize Firebase database
        database = FirebaseDatabase.getInstance()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()


        // Check for location permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Initialize map
            initMap()
        }


        // Set up search button click listener
        btnSearch.setOnClickListener {
            val locationName = etLocationSearch.text.toString()
            if (locationName.isNotEmpty()) {
                searchLocation(locationName)
            }
        }
        btnBook.setOnClickListener {
            if(!rideBooking) {
                // Get the current user ID
                val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
                currentUser?.let { user ->
                    val userId: String = user.uid

                    // Fetch the necessary data from the Firebase Realtime Database
                    val database = FirebaseDatabase.getInstance()
                    val usersRef = database.getReference("users")
                    usersRef.child(userId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    // Extract the necessary fields from the snapshot
                                    val latitude =
                                        snapshot.child("latitude").value as? Double ?: 0.0
                                    val longitude =
                                        snapshot.child("longitude").value as? Double ?: 0.0
                                    val destLatitude =
                                        snapshot.child("Destination latitude").value as? Double
                                            ?: 0.0
                                    val destLongitude =
                                        snapshot.child("Destination longitude").value as? Double
                                            ?: 0.0
                                    val name = snapshot.child("name").value as? String ?: ""
                                    val number = snapshot.child("mobileNo").value as? String ?: ""

                                    val ridesRef = database.getReference("rides")
                                    ridesRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()) {
                                                Toast.makeText(this@UserActivity, "Ride is already booked press cancel to cancel the previous booking", Toast.LENGTH_SHORT).show()
                                                rideBooking = true
                                                findingDriverTextView.visibility = View.VISIBLE
                                                btnCancel.visibility = View.VISIBLE
                                            } else {
                                                // Add the data to the rides table
                                                addToRidesTable(userId, latitude, longitude, destLatitude, destLongitude, name, number)
                                                rideBooking = true
                                                findingDriverTextView.visibility = View.VISIBLE
                                                btnCancel.visibility = View.VISIBLE
                                            }

                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            // Handle the error
                                        }
                                    })
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle the error
                            }
                        })
                }
            }
            else
            {
                Toast.makeText(this, "Ride is already booked press cancel to cancel the previous booking", Toast.LENGTH_SHORT).show()
                btnCancel.visibility = View.VISIBLE
            }
        }

        val showDirectionsButton = findViewById<Button>(R.id.btnShowDirections)

        btnCancel.setOnClickListener {
            // Get the current user ID
            val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
            currentUser?.let { user ->
                val userId: String = user.uid

                // Remove the ride from the rides table
                val database = FirebaseDatabase.getInstance()
                val ridesRef = database.getReference("rides")
                ridesRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach {
                            it.ref.removeValue()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle the error
                    }
                })

                rideBooking = false
                findingDriverTextView.visibility = View.GONE
                btnCancel.visibility = View.INVISIBLE
            }
        }
        showDirectionsButton.setOnClickListener {
            if(destinationLocation==null||currentLocationDirection==null) {

                Toast.makeText(
                    this@UserActivity,
                    "Location not set.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else{
                showDirections()
            }

        }
        startBackgroundTask()

    }
    private fun startBackgroundTask() {
        backgroundJob = CoroutineScope(Dispatchers.Default).launch {
            // Perform the background task in a loop
            while (isActive) {
                // Do your background task here
                println("Background task is running...")
                checkPendingRidesTable()

                // Delay for a certain period of time
                delay(1000) // Delay of 1 second
            }
        }
    }
    private fun checkPendingRidesTable() {
        val pendingRidesRef = daatabase.child("PendingRides")
        pendingRidesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isTablePresent = snapshot.exists()
                if (isTablePresent) {
                    // PendingRides table exists
                    deleteCurrentUserRideData()
                } else {
                    // PendingRides table does not exist
                    println("PendingRides table is not present")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to check PendingRides table: ${error.message}")
            }
        })
    }

    private fun deleteCurrentUserRideData() {
        val ridesRef = daatabase.child("Rides")
        val currentUserID = currentUser?.uid

        if (currentUserID != null) {
            ridesRef.child(currentUserID).removeValue()
                .addOnSuccessListener {
                    println("Current user ride data deleted")
                    val database = FirebaseDatabase.getInstance()
                    val ridesRef = database.getReference("rides")
                    ridesRef.orderByChild("userId").equalTo(currentUserID)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                snapshot.children.forEach {
                                    it.ref.removeValue()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle the error
                            }
                        })
                    backgroundJob?.cancel()

                    val intent = Intent(this, RideAcceptUser::class.java)
                    startActivity(intent)

                }
                .addOnFailureListener { error ->
                    println("Failed to delete current user ride data: ${error.message}")
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the background task when the activity is destroyed
        backgroundJob?.cancel()
    }
    object MyAppData {
        var fare: Float=0.00f
    }

    private fun addToRidesTable(userId: String, latitude: Double, longitude: Double, destLatitude: Double, destLongitude: Double, name: String, number: String) {
        // Get a reference to the rides table in Firebase Realtime Database
        val database = FirebaseDatabase.getInstance()
        val ridesRef = database.getReference("rides")

        // Create a new ride object with the necessary fields
        val ride = Ride(userId, latitude, longitude, destLatitude, destLongitude, name, number,fare.toString())

        // Add the ride object to the rides table
        ridesRef.push().setValue(ride)
    }


    // Ride class to represent the ride object
    data class Ride(
        val userId: String,
        val latitude: Double,
        val longitude: Double,
        val destLatitude: Double,
        val destLongitude: Double,
        val name: String,
        val number: String,
        val tvFare: String? = "",
        val driverNumber: String? = "",
        val accepted: Boolean=false
    )



    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val lati = 12.34 // replace with actual latitude value
        val longi = 56.78 // replace with actual longitude value
        destination = LatLng(lati, longi)

        // Enable my location button
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        map.isMyLocationEnabled = true
        map.setOnMapLongClickListener { latLng ->
            // Clear existing markers on map
            map.clear()


            // Add new marker at long touched location
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Custom Location")
            )


            // Update location to Firebase Realtime Database for current user
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val userRef = database.getReference("users").child(userId)
                userRef.child("Destination latitude").setValue(latLng.latitude)
                userRef.child("Destination longitude").setValue(latLng.longitude)
                destinationLocation = LatLng(latLng.latitude, latLng.longitude)

            }
            currentLocation?.let {
                val results = FloatArray(1)
                Location.distanceBetween(
                    it.latitude,
                    it.longitude,
                    latLng.latitude,
                    latLng.longitude,

                    results
                )
                currentLocationDirection = LatLng(it.latitude, it.longitude)
                val distance = results[0] / 1000 // Convert to kilometers
                fare = distance * 50 // Calculate fare (Rs20/km)
                MyAppData.fare=fare

                // Update text views
                tvDistance.text = "Distance: ${String.format("%.2f", distance)} km"
                tvFare.text = "Fare: Rs${String.format("%.2f", fare)}"

            }
        }

        // Get current location and move camera to that location
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLocation = location
                    val latLng = LatLng(location.latitude, location.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                    // Update location to Firebase Realtime Database for current user
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val userRef = database.getReference("users").child(userId)
                        userRef.child("latitude").setValue(location.latitude)
                        userRef.child("longitude").setValue(location.longitude)
                    }
                }
            }
        map.setOnMarkerClickListener { marker ->
            destination = marker.position
            true

        }

    }

    private fun initMap() {
        // Initialize map fragment

        val mapfragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapfragment.getMapAsync(this)
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun searchLocation(locationName: String) {
        // Use Geocoder to get location coordinates from location name
        val geocoder = Geocoder(this)
        try {
            val addresses = geocoder.getFromLocationName(locationName, 1)
            if (addresses!!.isNotEmpty()) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)

                // Clear existing markers on map
                map.clear()

                // Add marker for searched location
                map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(locationName)
                )

                // Move camera to searched location
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                // Calculate distance between current location and searched location
                currentLocation?.let {
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        it.latitude,
                        it.longitude,
                        address.latitude,
                        address.longitude,
                        results
                    )
                    currentLocationDirection = LatLng(it.latitude, it.longitude)
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val userRef = database.getReference("users").child(userId)
                        userRef.child("Destination latitude").setValue(latLng.latitude)
                        userRef.child("Destination longitude").setValue(latLng.longitude)
                        destinationLocation = LatLng(latLng.latitude, latLng.longitude)
                    }
                    val distance = results[0] / 1000 // Convert to kilometers
                    fare = distance * 50 // Calculate fare (Rs20/km)
                    MyAppData.fare=fare

                    // Update text views
                    tvDistance.text = "Distance: ${String.format("%.2f", distance)} km"
                    tvFare.text = "Fare: Rs${String.format("%.2f", fare)}"

                }
            } else {
                tvDistance.text = "Location not found"
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.usermenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_logout -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
            R.id.menu_item_history -> {
                // Handle history action
                return true
            }
            R.id.menu_item_edit_profile -> {

                val intent = Intent(this, EditUser::class.java)
                startActivity(intent)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Initialize map
                initMap()
            }
        }
    }


    private fun showDirections() {
        if(destinationLocation==null||currentLocationDirection==null) {

            Toast.makeText(
                this@UserActivity,
                "Location not set",
                Toast.LENGTH_SHORT
            ).show()
        }
        else{val uri =
            Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${currentLocationDirection!!.latitude},${currentLocationDirection!!.longitude}&destination=${destinationLocation!!.latitude},${destinationLocation!!.longitude}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)

        }
    }
}


