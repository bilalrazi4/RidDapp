package com.example.riddapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions


class StartRide : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    val locationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION
    val permissionCode = 1

    private var driverLatLng: LatLng = LatLng(0.0, 0.0)
    private var clientLatLng: LatLng = LatLng(0.0, 0.0)
    private lateinit var startRide: Button
    private lateinit var showDirection: Button
    private lateinit var showDirectiondrop: Button
    private lateinit var Iarrived: Button
    private lateinit var endRide: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_ride)
        FirebaseApp.initializeApp(this)
        startRide = findViewById(R.id.stRide)
        showDirection = findViewById(R.id.shDirection)
        showDirectiondrop = findViewById(R.id.shDirectionDest)
        Iarrived = findViewById(R.id.arrived)
        endRide = findViewById(R.id.endRide)
        startRide.visibility = View.GONE
        endRide.visibility = View.GONE
        showDirectiondrop.visibility = View.GONE
        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                locationPermission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted, proceed to get the location
            Log.d("ContextCompat.checkSelfPermission getloc call", "grant")
            getLocation()

        } else {
            // Request permission from the user
            ActivityCompat.requestPermissions(this, arrayOf(locationPermission), permissionCode)
            Log.d("ContextCompat.checkSelfPermission", "reject")
        }
        ongoingRide()
        mapView = findViewById(R.id.mapView)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)


        showDirection.setOnClickListener {
            showDirections()
        }
        showDirectiondrop.setOnClickListener {
            showDirectionsdestination()
        }

        Iarrived.setOnClickListener { StarttheRide()


        }


        startRide.setOnClickListener {
            Toast.makeText(this,"Ride has Started",Toast.LENGTH_LONG).show()
            endRide.visibility = View.VISIBLE
            startRide.visibility = View.GONE
        }


        endRide.setOnClickListener { EndtheRide() }


    }
    override fun onBackPressed() {
        // Do nothing to disable the back button
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun showDirections() {

        val uri =
            Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${driverLatLng!!.latitude},${driverLatLng!!.longitude}&destination=${clientLatLng!!.latitude},${clientLatLng!!.longitude}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)


    }

    private fun showDirectionsdestination() {

        val uri =
            Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${driverLatLng!!.latitude},${driverLatLng!!.longitude}&destination=${clientLatLng!!.latitude},${clientLatLng!!.longitude}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)


    }
    override fun onMapReady(map: GoogleMap) {

        googleMap = map

        // Set up initial camera position
        val cameraPosition = CameraUpdateFactory.newLatLngZoom(driverLatLng, 12f)
        Log.d("tag", "after reading from model: ${driverLatLng.longitude}")
        Log.d("tag", "after reading from model: ${driverLatLng.latitude}")
        googleMap.moveCamera(cameraPosition)

        // Add markers for driver and client
        googleMap.addMarker(MarkerOptions().position(driverLatLng).title("Driver"))
        googleMap.addMarker(MarkerOptions().position(clientLatLng).title("Client"))

        // Draw path line between driver and client
        val polylineOptions = PolylineOptions()
            .add(driverLatLng)
            .add(clientLatLng)
            .color(android.graphics.Color.RED)
            .width(5f)
        googleMap.addPolyline(polylineOptions)
    }


    fun StarttheRide() {

        startRide.visibility = View.VISIBLE
        Iarrived.visibility = View.GONE
        showDirection.visibility = View.GONE
        showDirectiondrop.visibility = View.VISIBLE
        val database = FirebaseDatabase.getInstance()
        val ref = database.reference
        val user = FirebaseAuth.getInstance().currentUser
        val driverId = user!!.uid
        val pendRides = ref.child("PendingRides").child(driverId)

        pendRides.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {
                Log.d("DataSnapshot", datasnapshot.toString())
                val item = datasnapshot.getValue(Rides::class.java)


                clientLatLng = LatLng(item?.destLatitude!!, item.destLongitude!!)
                driverLatLng = LatLng(item.latitude!!, item.longitude!!)

                Log.d("destLat", "driverLatLnglatitude: ${driverLatLng.latitude}")
                Log.d("destLong", "driverLatLnglongitude: ${driverLatLng.longitude}")


                updateMapWithDestination(item.destLatitude!!, item.destLongitude!!)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun updateMapWithDestination(destinationLat: Double, destinationLng: Double) {
        // Clear previous markers and polylines from the map
        googleMap.clear()

        // Add markers for driver, client, and destination
        googleMap.addMarker(MarkerOptions().position(driverLatLng).title("Driver"))
        googleMap.addMarker(MarkerOptions().position(clientLatLng).title("Client"))
        googleMap.addMarker(
            MarkerOptions().position(LatLng(destinationLat, destinationLng)).title("Destination")
        )

        // Draw path line between driver, client, and destination
        val polylineOptions = PolylineOptions()
            .add(driverLatLng)
            .add(clientLatLng)
            .add(LatLng(destinationLat, destinationLng))
            .color(android.graphics.Color.RED)
            .width(5f)
        googleMap.addPolyline(polylineOptions)

        // Adjust camera position to fit all markers
        val bounds = LatLngBounds.builder()
            .include(driverLatLng)
            .include(clientLatLng)
            .include(LatLng(destinationLat, destinationLng))
            .build()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    fun EndtheRide() {
        val database = FirebaseDatabase.getInstance()
        val ref = database.reference
        val user = FirebaseAuth.getInstance().currentUser
        val driverId = user!!.uid
        val pendRides = ref.child("PendingRides").child(driverId)
        var docId:String? = null



        pendRides.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {
                Log.d("DataSnapshot", datasnapshot.toString())
                val item = datasnapshot.getValue(Rides::class.java)
                val rd = ref.child("rides").orderByChild("userId").equalTo(item?.userId)

                val RdCompleteReference =
                    database.getReference("RidesCompleted")
                val RdCompleted = RdCompleteReference.push()
                docId = RdCompleted.key

                RdCompleted.setValue(item).addOnSuccessListener {


                    pendRides.removeValue().addOnSuccessListener {
                        rd.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (rideSnapshot in dataSnapshot.children) {
                                    rideSnapshot.ref.removeValue()

                                }
                                val intent = Intent(this@StartRide, EndRide::class.java)

                                intent.putExtra("docId",docId)
                                startActivity(intent)
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle the error if needed
                            }
                        })

                    }.addOnFailureListener {}



                }.addOnFailureListener { }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed to get the location
                Log.d("onRequestPermissionsResult getloc call", "granted")
                getLocation()

            } else {
                // Permission denied
                // Handle accordingly (e.g., show an error message)
                Log.d("onRequestPermissionsResult", "rejected")

            }
        }
    }

    private fun getLocation() {
        Log.d("getLocation", "start")

        Log.d("getLocation", "end")

    }


    fun ongoingRide() {
        val database = FirebaseDatabase.getInstance()
        val ref = database.reference
        val user = FirebaseAuth.getInstance().currentUser
        val driverId = user!!.uid
        val pendRides = ref.child("PendingRides").child(driverId)

        pendRides.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {
                Log.d("DataSnapshot", datasnapshot.toString())
                val item = datasnapshot.getValue(Rides::class.java)


                clientLatLng = LatLng(item?.latitude!!, item.longitude!!)
                driverLatLng = LatLng(item.driverLat!!, item.driverLong!!)
                Log.d("tag", "clientName: ${item.name}")
                Log.d("tag", "driverId: ${item.driverId}")
                Log.d("tag", "userID: ${item.userId}")
                Log.d("tag", "itemLong: ${item.longitude}")
                Log.d("tag", "itemLat: ${item.latitude}")
                Log.d("tag", "itemDestLong: ${item.destLongitude}")
                Log.d("tag", "itemDestLat: ${item.destLatitude}")
                Log.d("tag", "driverLat: ${item.driverLat}")
                Log.d("tag", "driverLong: ${item.driverLong}")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

}