package com.example.unipathsa

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class SplashActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "SplashActivity"
        private const val SPLASH_DELAY: Long = 1800 // 1.8 seconds
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle Android 12+ system splash screen initialization
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Log.d(TAG, "onCreate started")

        runSplashAnimation()

        // Navigate directly to IntroActivity after splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d(TAG, "Splash finished, navigating into IntroActivity")
            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, SPLASH_DELAY)
    }

    // Runs logo scale-up and text fade-in animation
    private fun runSplashAnimation() {
        val logoCard = findViewById<View>(R.id.cvSplashLogo)
        val tvAppName = findViewById<View>(R.id.tvAppName)
        val tvTagline = findViewById<View>(R.id.tvTagline)

        if (logoCard == null) return

        // Initial state before animation
        logoCard.scaleX = 0.5f
        logoCard.scaleY = 0.5f
        logoCard.alpha = 0f
        tvAppName?.alpha = 0f
        tvTagline?.alpha = 0f

        // Animators
        val logoScaleX = ObjectAnimator.ofFloat(logoCard, View.SCALE_X, 0.5f, 1.0f)
        val logoScaleY = ObjectAnimator.ofFloat(logoCard, View.SCALE_Y, 0.5f, 1.0f)
        val logoAlpha = ObjectAnimator.ofFloat(logoCard, View.ALPHA, 0f, 1.0f)
        val titleAlpha = ObjectAnimator.ofFloat(tvAppName, View.ALPHA, 0f, 1.0f)
        val taglineAlpha = ObjectAnimator.ofFloat(tvTagline, View.ALPHA, 0f, 1.0f)

        val animatorSet = AnimatorSet().apply {
            playTogether(logoScaleX, logoScaleY, logoAlpha, titleAlpha, taglineAlpha)
            duration = 750
            interpolator = DecelerateInterpolator()
        }
        animatorSet.start()
    }
}