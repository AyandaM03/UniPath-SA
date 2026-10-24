package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "ProfileActivity"
    }

    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var btnLogout: MaterialButton

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        Log.d(TAG, "onCreate initialized")

        NavigationHelper.setupDrawer(this, R.id.navProfile)

        bindViews()
        setupListeners()
        loadUserProfile()
    }

    // Connect XML views
    private fun bindViews() {
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileEmail = findViewById(R.id.tvProfileEmail)
        btnLogout = findViewById(R.id.btnLogoutProfile)
    }

    // Attach click listeners to "My Tools" rows and actions
    private fun setupListeners() {
        findViewById<LinearLayout>(R.id.btnNavApplications)?.setOnClickListener {
            startActivity(Intent(this, ApplicationsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnNavDocuments)?.setOnClickListener {
            startActivity(Intent(this, DocumentsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnNavNotifications)?.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnNavSaved)?.setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnNavSettings)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnLogout.setOnClickListener {
            confirmLogout()
        }
    }

    // Load user display name and email
    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            tvProfileName.text = if (!user.displayName.isNullOrBlank()) user.displayName else "Student Name"
            tvProfileEmail.text = user.email ?: ""

            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val fullName = doc.getString("fullName") ?: doc.getString("name")
                        if (!fullName.isNullOrBlank()) {
                            tvProfileName.text = fullName
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to load user profile from Firestore", e)
                }
        } else {
            Log.w(TAG, "User not authenticated in ProfileActivity")
        }
    }

    // Confirm Log Out
    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out of UniPath SA?")
            .setPositiveButton("Log Out") { _, _ ->
                Log.d(TAG, "User logging out from ProfileActivity")
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}