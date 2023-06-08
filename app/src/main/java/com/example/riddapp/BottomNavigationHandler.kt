package com.example.riddapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavigationHandler(private val context: Context) {

    fun setupWithActivity(activity: AppCompatActivity) {
        val bottomNavigationView = activity.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_history -> navigateToHistory()

            }
            true
        }
    }

    private fun navigateToHistory() {
        // Handle the "Home" button click
        // Navigate to the home view
        val intent = Intent(context, DriverHistory::class.java)
        context.startActivity(intent)
    }



}
