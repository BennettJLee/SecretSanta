package com.example.secretsanta

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity


class SplashActivity : ComponentActivity() {

    // Shared preferences
    private lateinit var sharedPreferences : LocalSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Set shared preferences
        sharedPreferences = LocalSharedPreferences(this)

        // Add 2000 milliseconds (2 seconds) delay before launching an activity
        Handler(Looper.getMainLooper()).postDelayed({

            // if in any rooms, go to a room and skip launch activity
            if(sharedPreferences.loadRoomNamesPref()){
                val intent = Intent(this, LocalHomeActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, LaunchActivity::class.java)
                startActivity(intent)
            }
            finish() // Close the splash screen activity
        }, 2000)

    }
}