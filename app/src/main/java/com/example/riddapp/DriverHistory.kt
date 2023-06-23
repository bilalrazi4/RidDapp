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



        val ridesRef = ref.child("RidesCompleted")

        val query  = ridesRef.orderByChild("driverId").equalTo(driverId)
        Log.d("driverId","$driverId")

        Log.d("ridesRefffff","$ridesRef")
        query.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                   for(s in snapshot.children)
                   {

                        val rc =RidesCompleted()
                        rc.name = s.child("name").getValue(String::class.java)
                        rc.number = s.child("number").getValue(String::class.java)
                        rc.destLongitude = s.child("destLongitude").getValue(Double::class.java)
                        rc.destLatitude = s.child("destLatitude").getValue(Double::class.java)
                        rc.longitude = s.child("longitude").getValue(Double::class.java)
                        rc.latitude = s.child("latitude").getValue(Double::class.java)
                       driverHistoryArrayList.add(rc)

                   }


                    driverHistoryRecyclerView.adapter = DriverHistoryAdapter(this@DriverHistory,driverHistoryArrayList)

                }



            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }
}