package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var isEditing = false

    private lateinit var etPhone: EditText
    private lateinit var etSchool: EditText
    private lateinit var btnEditProfile: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        BottomNavHelper.setup(this, R.id.nav_profile)

        btnEditProfile = findViewById(R.id.btnEditProfile)
        val btnSettings = findViewById<Button>(R.id.btnSettings)
        etPhone = findViewById(R.id.etPhone)
        etSchool = findViewById(R.id.etSchool)

        val user = FirebaseAuth.getInstance().currentUser
        findViewById<TextView>(R.id.tvProfileName).text = user?.displayName ?: "Student"
        findViewById<TextView>(R.id.tvProfileEmail).text = user?.email ?: ""

        // Start with fields locked (read-only) until the user taps Edit
        setFieldsEditable(false)

        // Load any previously saved phone/school from Firestore
        loadProfileDetails()

        btnEditProfile.setOnClickListener {
            if (isEditing) {
                saveProfileDetails()
            } else {
                setFieldsEditable(true)
            }
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.btnSaved).setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }
    }

    // Toggles whether the phone/school fields can be typed into, and updates the button label
    private fun setFieldsEditable(editable: Boolean) {
        isEditing = editable
        etPhone.isEnabled = editable
        etSchool.isEnabled = editable
        btnEditProfile.text = if (editable) "Save Profile" else "Edit Profile"
    }

    // Pulls the user's saved phone/school from Firestore, if they've saved it before
    private fun loadProfileDetails() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                etPhone.setText(doc.getString("phone") ?: "")
                etSchool.setText(doc.getString("school") ?: "")
            }
    }

    // Saves the phone/school fields to Firestore under this user's document
    private fun saveProfileDetails() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val details = mapOf(
            "phone" to etPhone.text.toString().trim(),
            "school" to etSchool.text.toString().trim()
        )

        db.collection("users").document(userId)
            .set(details, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                setFieldsEditable(false)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show()
            }
    }
}