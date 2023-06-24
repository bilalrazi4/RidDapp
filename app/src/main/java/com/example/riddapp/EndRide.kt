package com.example.riddapp

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Integer.parseInt

class EndRide : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_ride)

        val doc = intent.getStringExtra("docId")

        val fare = findViewById<TextView>(R.id.totalFare)

        val cash  = findViewById<Button>(R.id.cash)
        val ep  = findViewById<Button>(R.id.ep)
        val jc  = findViewById<Button>(R.id.jc)
        val cry = findViewById<Button>(R.id.crypto)
        val mainmewnu = findViewById<Button>(R.id.menu)




        val db = FirebaseDatabase.getInstance().getReference("RidesCompleted")
        val row = db.child(doc!!)
        row.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                var price =  snapshot.child("tvFare").getValue() as String
                val fareFloat = price.toFloat()
                val fareInt = fareFloat.toInt()
                fare.text = fareInt.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })



        cash.setOnClickListener {
            Toast.makeText(this,"Kindly receive the money from user", Toast.LENGTH_LONG).show()
            val intent = Intent(this,DriverActivity::class.java)
            startActivity(intent)
        }


        mainmewnu.setOnClickListener {
            val intent= Intent(this,DriverActivity::class.java)
            startActivity(intent)
        }

        ep.setOnClickListener {
            showEp()
        }

        jc.setOnClickListener {
            showJc()
        }
        cry.setOnClickListener {
            showCrypto()
        }







    }

    private fun showEp() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_ep)

        val imageView = dialog.findViewById<ImageView>(R.id.imageView)
        // Set the image resource or load the image into the ImageView

        dialog.show()
    }
    private fun showJc() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_jc)

        val imageView = dialog.findViewById<ImageView>(R.id.imageView)
        // Set the image resource or load the image into the ImageView

        dialog.show()
    }

    private fun showCrypto() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_crypto)

        val imageView = dialog.findViewById<ImageView>(R.id.imageView)
        // Set the image resource or load the image into the ImageView

        dialog.show()
    }

}