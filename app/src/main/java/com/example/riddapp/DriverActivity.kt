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
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.google.firebase.firestore.FirebaseFirestore


class DriverActivity() : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private lateinit var rideRecyclerView: RecyclerView
    private lateinit var rideArrayList:ArrayList<Rides>
    private lateinit var empty:TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)
        Log.d("tag", "in driver")
        val bottomNavigationHandler = BottomNavigationHandler(this)
        bottomNavigationHandler.setupWithActivity(this)
        // Initialize Firebase database
        database = FirebaseDatabase.getInstance()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()


        // Set up search button click listener
    empty=findViewById(R.id.empty)
        rideRecyclerView = findViewById(R.id.rideList)
        rideRecyclerView.layoutManager = LinearLayoutManager(this)
        rideRecyclerView.setHasFixedSize(true)
        rideArrayList = arrayListOf<Rides>()

        getRides()

    }


    private fun getRides() {


        database = FirebaseDatabase.getInstance()
        val ref = database.reference
        val ridesRef = ref.child("rides")
        ridesRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    for(childSnapshot in snapshot.children)
                    {
                        val rd = childSnapshot.getValue(Rides::class.java)
                        rideArrayList.add(rd!!)

                        val name  = rd?.name
                        val number  = rd?.number
                        Log.d("Name", "Name: $name")
                        Log.d("Number", "Name: $number")

                    }

                        rideRecyclerView.adapter = MyAdapter(this@DriverActivity,rideArrayList)

                }
                else
                {
                    empty.visibility = View.VISIBLE
                }


            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }



}


