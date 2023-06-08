package com.example.riddapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class DriverHistory : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var driverHistoryRecyclerView: RecyclerView
    private lateinit var driverHistoryArrayList:ArrayList<RidesCompleted>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_history)
        driverHistoryRecyclerView = findViewById(R.id.driverHistoryList)
        driverHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        driverHistoryRecyclerView.setHasFixedSize(true)
        driverHistoryArrayList = arrayListOf<RidesCompleted>()

        getDriverHistory()
    }
    private fun getDriverHistory(){
        val auth = FirebaseAuth.getInstance().currentUser
        database = FirebaseDatabase.getInstance()
        val ref = database.reference
        val driverId = auth?.uid!!



        val ridesRef = ref.child("RidesCompleted").child(driverId)
        Log.d("driverId","$driverId")

        Log.d("ridesRefffff","$ridesRef")
        ridesRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    val rc= RidesCompleted(destLongitude =snapshot.child("destLongitude").value.toString().toDouble() ,destLatitude = snapshot.child("destLatitude").value.toString().toDouble(), number = snapshot.child("number").value.toString(),name=snapshot.child("name").value.toString(),
                    latitude = snapshot.child("latitude").value.toString().toDouble(),longitude = snapshot.child("longitude").value.toString().toDouble())

                    driverHistoryArrayList.add(rc)

                    driverHistoryRecyclerView.adapter = DriverHistoryAdapter(this@DriverHistory,driverHistoryArrayList)

                }



            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }
}