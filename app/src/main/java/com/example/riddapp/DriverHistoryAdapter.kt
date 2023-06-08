package com.example.riddapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DriverHistoryAdapter(private val context: Context, private val driverhistoryList: ArrayList<RidesCompleted>):RecyclerView.Adapter<DriverHistoryAdapter.MyViewHolder>(){


    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val cName : TextView = itemView.findViewById(R.id.customerName)
        val cNumber : TextView = itemView.findViewById(R.id.customerNumber)
        val cPickup: TextView = itemView.findViewById(R.id.customerPickup)
        val cDest: TextView = itemView.findViewById(R.id.customerDestination)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.driver_history_item,
            parent,false)
        return DriverHistoryAdapter.MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return driverhistoryList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = driverhistoryList[position]
        holder.cName.text = currentItem.name
        holder.cNumber.text = currentItem.number
        holder.cPickup.text  = "ichra"
        holder.cDest.text = "dhaphs5"

    }
}