package com.example.riddapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class UserThankyou : AppCompatActivity() {
    val fare=UserActivity.MyAppData.fare.toInt()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_thankyou)
        val fareTextView = findViewById<TextView>(R.id.fareTextView)
        fareTextView.text = "Thank you for choosing Ridapp. Your fare is Rs$fare"
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            // Navigate back to MainActivity
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}