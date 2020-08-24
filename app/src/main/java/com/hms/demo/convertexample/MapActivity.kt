package com.hms.demo.convertexample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity() , OnMapReadyCallback,GPSListener, View.OnClickListener {

    lateinit var googleMap: GoogleMap
    var gps: GPS?=null
    private val TAG="MapActivity"
    var lat:Double=0.0
    var lng:Double=0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val account = GoogleSignIn.getLastSignedInAccount(this)
        getFCMToken()
        Toast.makeText(this,"Bienvenido: ${account?.displayName}",Toast.LENGTH_SHORT).show()
        mapView.onCreate(null)
        mapView.onResume()
        mapView.getMapAsync(this)
        myLocation.setOnClickListener(this)

    }

    override fun onResume() {
        super.onResume()
        if(checkLocationPermissions()){
            setupGPS()
        } else requestGPSPermission()
    }

    private fun requestGPSPermission() {

        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),100)
    }

    private fun checkLocationPermissions():Boolean{
        val coarse=checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        val fine=checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        return coarse==PackageManager.PERMISSION_GRANTED||fine==PackageManager.PERMISSION_GRANTED
    }

    fun setupGPS(){
        if(gps==null)
            gps=GPS(this,this)
        gps?.startLocationRequests()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap=googleMap
    }

    override fun onLocationChanged(lat: Double, lng: Double) {
        if(this.lat==0.0&&this.lng==0.0){//if is the first location obtained
            val cameraUpdate=CameraUpdateFactory.newLatLngZoom(LatLng(lat,lng),14.0f)
            googleMap.animateCamera(cameraUpdate)
        }
        this.lat=lat
        this.lng=lng
    }

    fun getFCMToken(){
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e(TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                val msg = "FCM Token: $token"
                Log.e(TAG, msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(checkLocationPermissions()){
            setupGPS()
        }
    }

    override fun onClick(v: View?) {
        if(checkLocationPermissions()){
            if(gps?.isEnabled!!){
                googleMap.clear()
                val position =LatLng(this.lat,lng)
                val marker=MarkerOptions().position(position)
                val cameraUpdate=CameraUpdateFactory.newLatLngZoom(position,18.0f)
                googleMap.addMarker(marker)
                googleMap.animateCamera(cameraUpdate)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if(gps?.isEnabled!!){
            gps?.stopLocationRequests()
        }
    }
}