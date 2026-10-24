package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class SettingsActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "SettingsActivity"
        private const val PREFS_NAME = "settings"
        private const val KEY_DARK_MODE = "dark_mode"
    }

    private lateinit var etFullName: EditText
    private lateinit var etHighSchool: EditText
    private lateinit var btnSaveProfile: MaterialButton
    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var btnResetPassword: MaterialButton
    private lateinit var btnLogout: MaterialButton

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        Log.d(TAG, "onCreate initialized")

        NavigationHelper.setupDrawer(this, R.id.navSettings)

        bindViews()
        setupListeners()
        loadUserSettingsFromFirestore()
    }

    // Connect XML views
    private fun bindViews() {
        etFullName = findViewById(R.id.etSettingsFullName)
        etHighSchool = findViewById(R.id.etSettingsHighSchool)
        btnSaveProfile = findViewById(R.id.btnSaveProfileSettings)
        switchDarkMode = findViewById(R.id.switchDarkMode)
        btnResetPassword = findViewById(R.id.btnResetPasswordSettings)
        btnLogout = findViewById(R.id.btnLogoutSettings)

        // Initialize dark mode switch state from SharedPreferences
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        switchDarkMode.isChecked = prefs.getBoolean(KEY_DARK_MODE, false)
    }

    // Attach click listeners to settings UI elements
    private fun setupListeners() {
        btnSaveProfile.setOnClickListener {
            saveUserSettingsToFirestore()
        }

        btnResetPassword.setOnClickListener {
            sendPasswordResetEmail()
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            toggleDarkModePreference(isChecked)
        }

        findViewById<LinearLayout>(R.id.llHelpRow)?.setOnClickListener {
            showHelpDialog()
        }

        findViewById<LinearLayout>(R.id.llAboutRow)?.setOnClickListener {
            showAboutDialog()
        }

        btnLogout.setOnClickListener {
            confirmLogout()
        }
    }

    // Load full name and high school from Firestore users/{uid}
    private fun loadUserSettingsFromFirestore() {
        val user = auth.currentUser
        if (user == null) {
            Log.w(TAG, "User not logged in - cannot load settings")
            return
        }

        etFullName.setText(user.displayName ?: "")

        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val school = doc.getString("school") ?: doc.getString("highSchool") ?: ""
                    etHighSchool.setText(school)
                    Log.d(TAG, "Loaded user profile details from Firestore")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error reading profile details from Firestore", e)
            }
    }

    // Save full name and high school to Firestore
    private fun saveUserSettingsToFirestore() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val newName = etFullName.text.toString().trim()
        val newSchool = etHighSchool.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
            return
        }

        // Update Firebase Auth display name
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Firebase Auth display name updated to: $newName")
            }
        }

        // Update Firestore user document
        val updateMap = hashMapOf(
            "fullName" to newName,
            "school" to newSchool,
            "highSchool" to newSchool
        )

        db.collection("users").document(user.uid)
            .set(updateMap, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Saved profile changes to Firestore successfully")
                Toast.makeText(this, "Profile settings saved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save profile changes to Firestore", e)
                Toast.makeText(this, "Failed to save settings: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    // Sends password reset email via Firebase Auth
    private fun sendPasswordResetEmail() {
        val email = auth.currentUser?.email
        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Log.d(TAG, "Password reset email sent to $email")
                Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error sending password reset email", e)
                Toast.makeText(this, "Error sending email: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    // Toggles dark mode immediately and saves to SharedPreferences
    private fun toggleDarkModePreference(enabled: Boolean) {
        Log.d(TAG, "Toggling dark mode preference: $enabled")
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_MODE, enabled)
            .apply()

        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    // Display Help Dialog
    private fun showHelpDialog() {
        AlertDialog.Builder(this)
            .setTitle("Help & Support")
            .setMessage("For assistance with university applications, bursary details or technical issues, email support@unipathsa.co.za or visit www.unipathsa.co.za.")
            .setPositiveButton("OK", null)
            .show()
    }

    // Display About Dialog
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About UniPath SA")
            .setMessage("UniPath SA v1.0\nYour ultimate South African higher education guidance platform.")
            .setPositiveButton("OK", null)
            .show()
    }

    // Confirm Log Out
    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out of UniPath SA?")
            .setPositiveButton("Log Out") { _, _ ->
                Log.d(TAG, "User logging out")
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