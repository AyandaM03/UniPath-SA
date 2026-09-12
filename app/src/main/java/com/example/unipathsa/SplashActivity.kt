package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    // How long the splash screen stays visible before moving on (in milliseconds)
    private val SPLASH_DELAY: Long = 2000 // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Schedule the navigation to Login to happen after SPLASH_DELAY,
        // running on the main UI thread
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // removes Splash from the back stack so back button exits the app instead
        }, SPLASH_DELAY)
    }
}