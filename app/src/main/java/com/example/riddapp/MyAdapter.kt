package com.example.riddapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.riddapp.DriverActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

class MyAdapter(private val context: Context,private var userList : ArrayList<Rides>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.rides_item,
            parent,false)
        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentitem = userList[position]




            holder.Name.text = currentitem.name
            holder.Number.text = currentitem.number
            holder.RdAccept.setOnClickListener{
                acceptRide(currentitem)
            }






    }

    override fun getItemCount(): Int {

        return userList.size
    }
//adding ride object into pending ride collection
    private fun acceptRide(item:Rides)
    {
        val user = FirebaseAuth.getInstance().currentUser


        val database  = FirebaseDatabase.getInstance()
        val ref = database.reference
        //getcurrentlocationlatlong
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    item.driverLat = location.latitude
                    item.driverLong = location.longitude

                    Log.d("ADALATLNG", "adlatitude: ${item.driverLat}")
                    Log.d("ADALATLNG", "adlongitude: ${item.driverLong}")




                    // Do something with the latitude and longitude values
                    // For example, save them to the database or display on the UI
                } else {
                    // Location is null
                    // Handle accordingly (e.g., show an error message)
                }
            }
            .addOnFailureListener { exception: Exception ->
                // Failed to get location
                // Handle accordingly (e.g., show an error message)
            }
        //end

        val userRef = ref.child("users").child(user!!.uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userr = dataSnapshot.getValue(User::class.java)
                item.driverName = userr!!.name
                item.driverNumber = userr.mobileNo
                item.driverId = userr.id

                val childRef = ref.child("PendingRides").child(item.driverId!!)
                val value = childRef.setValue(item)
                val intent = Intent(context, StartRide::class.java)
                context.startActivity(intent)



                Log.d("Name","Current User: ${item.driverName}")
                Log.d("Name","Current User: ${item.driverNumber}")

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle read error
            }
        })

    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val Name : TextView = itemView.findViewById(R.id.name)
        val Number : TextView = itemView.findViewById(R.id.number)
        val RdAccept: Button = itemView.findViewById(R.id.acceptRide)



    }

}