package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class HomeActivity : AppCompatActivity() {

    private lateinit var cardSearch: MaterialCardView
    private lateinit var cardProfile: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bindViews()
        setupListeners()
    }

    private fun bindViews() {
        cardSearch = findViewById(R.id.cardSearch)
        cardProfile = findViewById(R.id.cardProfile)
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
    }
}
