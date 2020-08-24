package com.hms.demo.convertexample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import net.openid.appauth.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private val GOOGLE_SIGN_IN = 100
    private val APP_AUTH_GOOGLE = 200
    private val GOOGLE_CLIENT_ID =
        "971052637314-s6lkgsam2b8s1dt07ep83pl0ca0ue28g.apps.googleusercontent.com"
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        google_sign_in_button.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.google_sign_in_button -> {
                appAuthGoogle()
            }
            else -> {
                return
            }
        }
    }

    private fun gmsSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    private fun appAuthGoogle() {
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("https://accounts.google.com/o/oauth2/auth"), // authorization endpoint
            Uri.parse("https://oauth2.googleapis.com/token")
        ) // token endpoint
        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfig,  // the authorization service configuration
            GOOGLE_CLIENT_ID,  // the client ID, typically pre-registered and static
            ResponseTypeValues.CODE,  //
            Uri.parse("$packageName:/oauth2redirect")
        ) // the redirect URI to which the auth response is sent
        authRequestBuilder.setScope("openid email profile")
        val authRequest = authRequestBuilder.build()
        val authService = AuthorizationService(this)
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        startActivityForResult(authIntent, APP_AUTH_GOOGLE)
        authService.dispose()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GOOGLE_SIGN_IN -> {
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                task.addOnSuccessListener {
                    startActivity(Intent(this, MapActivity::class.java))
                    finish()
                }.addOnFailureListener {
                    Log.e("Failure", it.toString())
                }
            }

            APP_AUTH_GOOGLE -> {
                if (data != null) {
                    val response = AuthorizationResponse.fromIntent(data)
                    val ex = AuthorizationException.fromIntent(data)
                    val authState = AuthState(response, ex)
                    if (response != null) {
                        val service = AuthorizationService(this)
                        service.performTokenRequest(
                            response.createTokenExchangeRequest()
                        ) { tokenResponse, exception ->
                            service.dispose()
                            if (exception != null) {
                                Log.e(TAG, "Token Exchange failed", exception)
                            } else {
                                if (tokenResponse != null) {
                                    authState.update(tokenResponse, exception)
                                    Log.e(
                                        TAG,
                                        "Token Response [ Access Token: ${tokenResponse.accessToken}, ID Token: ${tokenResponse.idToken}"
                                    )
                                    getUserInformation(tokenResponse.accessToken!!)

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getUserInformation(accessToken:String) {
        CoroutineScope(IO).launch {
            val data=getInfo(accessToken)
            val json=JSONObject(data)
            Log.e("JSON",data)
            runOnUiThread{
                Toast.makeText(this@LoginActivity,"Bienvenido: ${json.getString("name")}",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, MapActivity::class.java))
                finish()
            }
        }
    }

    public fun getInfo(accessToken:String):String{
        val url= URL("https://www.googleapis.com/oauth2/v3/userinfo")
        val conn=url.openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", String.format("Bearer %s", accessToken))
        return convertStreamToString(conn.inputStream)
    }
    ///////////////////////////////////////////////////////////////////
    fun convertStreamToString(input: InputStream):String{
        val reader = BufferedReader(InputStreamReader(input))
        val sb = StringBuilder()
        var line: String?
        try {
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append('\n')
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                input.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return sb.toString()
    }


}