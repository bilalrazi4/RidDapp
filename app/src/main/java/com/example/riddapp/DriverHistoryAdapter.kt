package com.example.riddapp

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

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
        val pickup = Location("")
        pickup.latitude = currentItem.latitude!!
        pickup.longitude = currentItem.longitude!!
        val pickupAddress=getAddressFromLocation(context,pickup)

        val dest = Location("")
        dest.latitude = currentItem.destLatitude!!
        dest.longitude = currentItem.destLongitude!!
        val destAddress=getAddressFromLocation(context,dest)



        holder.cName.text = currentItem.name
        holder.cNumber.text = currentItem.number
        holder.cPickup.text  = pickupAddress
        holder.cDest.text = destAddress

    }
    fun getAddressFromLocation(context: Context, location: Location): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        var addressText: String? = null
        try {
            val addresses: List<Address> = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            ) as List<Address>
            if (addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                addressText = address.getAddressLine(0)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addressText
    }
}