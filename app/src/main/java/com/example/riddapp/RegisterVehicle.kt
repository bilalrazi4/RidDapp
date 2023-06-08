package com.example.riddapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.FirebaseError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class RegisterVehicle : AppCompatActivity() {
    private lateinit var Name:EditText
    private lateinit var Color:EditText
    private lateinit var Model:EditText
    private lateinit var LiscenceNumber:EditText
    private lateinit var EnginePowerCC:EditText
    private lateinit var NumberPlater:EditText

    private lateinit var btnRegister:Button




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_vehicle)
        Name = findViewById(R.id.carName)
        Color = findViewById(R.id.carColor)
        Model = findViewById(R.id.carModel)
        LiscenceNumber = findViewById(R.id.carLiscence)
        EnginePowerCC = findViewById(R.id.carEnginePowerCC)
        NumberPlater = findViewById(R.id.carNumberPlate)

        btnRegister = findViewById(R.id.btnRegisterCar)

        btnRegister.setOnClickListener {


            var Name = Name.text.toString().trim()
            var Color = Color.text.toString().trim()
            var Model = Model.text.toString().trim()
            var LiscenceNumber = LiscenceNumber.text.toString().trim()
            var EnginePowerCC = EnginePowerCC.text.toString().trim()
            var NumberPlater = NumberPlater.text.toString().trim()

            var veh = Vehicle(Name,Color,Model,LiscenceNumber,EnginePowerCC, NumberPlater)

            val user = FirebaseAuth.getInstance().currentUser
            val driverId = user!!.uid
            val database = FirebaseDatabase.getInstance()
            val ref = database.reference
            val vehicleRef = ref.child("Vehicles").child(driverId)
            Log.d("vehicle","${veh.Name}")
            Log.d("vehicle","${veh.Color}")
            val v = vehicleRef.setValue(veh)

            Toast.makeText(this, "Vehicle Successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)




        }



    }
}