package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.facebook.login.LoginManager
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
        private const val KEY_LANGUAGE = "app_language"
    }

    private lateinit var etFullName: EditText
    private lateinit var etHighSchool: EditText
    private lateinit var btnSaveProfile: MaterialButton
    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var btnResetPassword: MaterialButton
    private lateinit var btnLogout: MaterialButton

    private lateinit var llLanguageRow: LinearLayout
    private lateinit var tvCurrentLanguage: TextView

    private lateinit var btnNotif: RelativeLayout
    private lateinit var tvNotifBadge: TextView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        Log.d(TAG, "onCreate initialized")

        NavigationHelper.setupDrawer(this, R.id.navSettings)

        bindViews()
        setupListeners()
        updateLanguageDisplay()
        loadUnreadNotifications()
        loadUserSettingsFromFirestore()
    }

    private fun bindViews() {
        etFullName = findViewById(R.id.etSettingsFullName)
        etHighSchool = findViewById(R.id.etSettingsHighSchool)
        btnSaveProfile = findViewById(R.id.btnSaveProfileSettings)
        switchDarkMode = findViewById(R.id.switchDarkMode)
        btnResetPassword = findViewById(R.id.btnResetPasswordSettings)
        btnLogout = findViewById(R.id.btnLogoutSettings)

        llLanguageRow = findViewById(R.id.llLanguageRow)
        tvCurrentLanguage = findViewById(R.id.tvCurrentLanguage)

        btnNotif = findViewById(R.id.btnNotifications)
        tvNotifBadge = findViewById(R.id.tvNotifBadge)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        switchDarkMode.isChecked = prefs.getBoolean(KEY_DARK_MODE, false)
    }

    private fun setupListeners() {
        btnNotif.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        llLanguageRow.setOnClickListener {
            showLanguageSelectionDialog()
        }

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

    // Updates the current displayed language name in Settings
    private fun updateLanguageDisplay() {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        val currentTag = if (!currentLocales.isEmpty) currentLocales.get(0)?.language ?: "en" else "en"

        tvCurrentLanguage.text = when (currentTag) {
            "zu" -> "isiZulu"
            "xh" -> "isiXhosa"
            "st" -> "Sesotho"
            else -> "English"
        }
    }

    // Displays in-app language picker dialog
    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("English", "isiZulu", "isiXhosa", "Sesotho")
        val tags = arrayOf("en", "zu", "xh", "st")

        val currentLocales = AppCompatDelegate.getApplicationLocales()
        val currentTag = if (!currentLocales.isEmpty) currentLocales.get(0)?.language ?: "en" else "en"
        var selectedIndex = tags.indexOf(currentTag)
        if (selectedIndex == -1) selectedIndex = 0

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_language_dialog_title))
            .setSingleChoiceItems(languages, selectedIndex) { dialog, which ->
                val chosenTag = tags[which]
                Log.d(TAG, "Selected language tag: $chosenTag")

                // Apply locale instantly across the app
                val appLocales = LocaleListCompat.forLanguageTags(chosenTag)
                AppCompatDelegate.setApplicationLocales(appLocales)

                // Save to SharedPreferences and Firestore users/{uid}.language
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString(KEY_LANGUAGE, chosenTag)
                    .apply()

                val uid = auth.currentUser?.uid
                if (uid != null) {
                    db.collection("users").document(uid)
                        .set(mapOf("language" to chosenTag), SetOptions.merge())
                }

                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun loadUnreadNotifications() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("notifications")
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snap ->
                val count = snap?.size() ?: 0
                if (count > 0) {
                    tvNotifBadge.visibility = View.VISIBLE
                    tvNotifBadge.text = if (count > 9) "9+" else count.toString()
                } else {
                    tvNotifBadge.visibility = View.GONE
                }
            }
    }

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

                    val savedLang = doc.getString("language")
                    if (!savedLang.isNullOrBlank()) {
                        val currentLocales = AppCompatDelegate.getApplicationLocales()
                        val currentTag = if (!currentLocales.isEmpty) currentLocales.get(0)?.language ?: "en" else "en"
                        if (currentTag != savedLang) {
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(savedLang))
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error reading profile details from Firestore", e)
            }
    }

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

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Firebase Auth display name updated to: $newName")
            }
        }

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

    private fun showHelpDialog() {
        AlertDialog.Builder(this)
            .setTitle("Help & Support")
            .setMessage("For assistance with university applications, bursary details or technical issues, email support@unipathsa.co.za or visit www.unipathsa.co.za.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About UniPath SA")
            .setMessage("UniPath SA v1.0\nYour ultimate South African higher education guidance platform.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out of UniPath SA?")
            .setPositiveButton("Log Out") { _, _ ->
                Log.d(TAG, "User logging out")
                try {
                    LoginManager.getInstance().logOut()
                } catch (e: Exception) {
                    Log.w(TAG, "Error logging out of Facebook SDK", e)
                }

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