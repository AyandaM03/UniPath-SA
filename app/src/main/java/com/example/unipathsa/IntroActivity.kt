package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class IntroActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "IntroActivity"
        private const val PREFS_NAME = "settings"
        private const val KEY_HAS_SEEN_INTRO = "hasSeenIntro"
    }

    private lateinit var btnGetStarted: MaterialButton
    private lateinit var tvLoginLink: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        Log.d(TAG, "onCreate initialized")

        auth = FirebaseAuth.getInstance()

        btnGetStarted = findViewById(R.id.btnGetStarted)
        tvLoginLink = findViewById(R.id.tvLoginLink)

        btnGetStarted.setOnClickListener {
            markIntroSeenAndNavigate(
                if (auth.currentUser != null) HomeActivity::class.java else SignUpActivity::class.java
            )
        }

        tvLoginLink.setOnClickListener {
            markIntroSeenAndNavigate(
                if (auth.currentUser != null) HomeActivity::class.java else LoginActivity::class.java
            )
        }
    }

    // Stores hasSeenIntro flag in SharedPreferences and navigates to target activity
    private fun markIntroSeenAndNavigate(targetClass: Class<*>) {
        Log.d(TAG, "Setting hasSeenIntro = true and navigating to ${targetClass.simpleName}")
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HAS_SEEN_INTRO, true)
            .apply()

        startActivity(Intent(this, targetClass))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}