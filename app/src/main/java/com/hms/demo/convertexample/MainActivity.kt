package com.hms.demo.convertexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.android.gms.auth.api.signin.GoogleSignIn

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val account = GoogleSignIn.getLastSignedInAccount(this)
        //if(account==null){
        Handler().postDelayed({
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        },1000)

        /*}
        else{

            startActivity(Intent(this,MapActivity::class.java))
            finish()
        }*/
    }
}