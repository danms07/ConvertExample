package com.hms.demo.convertexample

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*


class GPS(val context:Context, val listener: GPSListener?): LocationCallback() {
    private val SECOND:Long = 1000
    private val MINUTE:Long = SECOND * 60
    private val INTERVAL = MINUTE * 2.toLong()
    public var isEnabled:Boolean=false


    private val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)


    @SuppressLint("MissingPermission")
    fun startLocationRequests() {
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.setInterval(MINUTE)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            this,
            Looper.getMainLooper()
        ).addOnSuccessListener { isEnabled=true }
    }

    fun stopLocationRequests() {
        fusedLocationProviderClient.removeLocationUpdates(this)
            .addOnSuccessListener { isEnabled=false }
    }

    override fun onLocationResult(locationResult: LocationResult?) {
        //super.onLocationResult(locationResult);
        if (locationResult == null) {
            return
        }
        for (location in locationResult.locations) {
            listener?.onLocationChanged(location.latitude,location.longitude)
        }
    }


}