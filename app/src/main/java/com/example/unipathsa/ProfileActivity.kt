package com.example.unipathsa

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.widget.TextView
import android.content.Intent

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        BottomNavHelper.setup(this, R.id.nav_profile)

        val btnEditProfile = findViewById<Button>(R.id.btnEditProfile)
        val btnSettings = findViewById<Button>(R.id.btnSettings)

        // Show the logged-in user's real name and email
        val user = FirebaseAuth.getInstance().currentUser
        findViewById<TextView>(R.id.tvProfileName).text = user?.displayName ?: "Student"
        findViewById<TextView>(R.id.tvProfileEmail).text = user?.email ?: ""


        btnEditProfile.setOnClickListener {
            Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show()
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.btnSaved).setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }
    }
}
