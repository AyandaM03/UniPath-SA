package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth


class HomeActivity : AppCompatActivity() {

    private lateinit var cardSearch: MaterialCardView
    private lateinit var cardProfile: MaterialCardView

    // New views for the greeting, notification bell, and Add Marks buttons
    private lateinit var tvStudentName: TextView
    private lateinit var btnNotifications: ImageButton

    private lateinit var btnAddMarksCard: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bindViews()
        setupListeners()
        setupGreeting()
    }

    private fun bindViews() {
        cardSearch = findViewById(R.id.cardSearch)
        cardProfile = findViewById(R.id.cardProfile)

        tvStudentName = findViewById(R.id.tvStudentName)
        btnNotifications = findViewById(R.id.btnNotifications)
        btnAddMarksCard = findViewById(R.id.btnAddMarksCard)
    }

    private fun setupListeners() {
        // Navigation to Search
        cardSearch.setOnClickListener {
            val intent = Intent(this, UniversitySearchActivity::class.java)
            startActivity(intent)
        }

        // Navigation to Profile
        cardProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Both "Add Marks" buttons open the APS Calculator
        val openCalculator = {
            startActivity(Intent(this, ApsCalculatorActivity::class.java))
        }
        btnAddMarksCard.setOnClickListener { openCalculator() }

        // Notification bell (placeholder for now)
        btnNotifications.setOnClickListener {
            // TODO: open notifications screen or dropdown
        }

    }
    private fun setupGreeting() {
        val name = FirebaseAuth.getInstance().currentUser?.displayName
        tvStudentName.text = if (!name.isNullOrEmpty()) name else "Student"
    }

}
