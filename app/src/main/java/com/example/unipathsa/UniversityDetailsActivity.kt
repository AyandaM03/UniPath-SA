package com.example.unipathsa

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class UniversityDetailsActivity : AppCompatActivity() {

    private lateinit var ivUniImage: ImageView
    private lateinit var tvUniName: TextView
    private lateinit var tvUniLocation: TextView
    private lateinit var tvUniDescription: TextView
    private lateinit var tvProgrammes: TextView
    private lateinit var tvRequirements: TextView
    private lateinit var tvAppInfo: TextView
    private lateinit var tvImportantInfo: TextView
    private lateinit var btnApply: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_university_details)

        bindViews()

        val university = intent.getSerializableExtra("university") as? University
        university?.let {
            displayUniversityDetails(it)
        }

        setupListeners()
    }

    private fun bindViews() {
        ivUniImage = findViewById(R.id.ivUniImage)
        tvUniName = findViewById(R.id.tvUniName)
        tvUniLocation = findViewById(R.id.tvUniLocation)
        tvUniDescription = findViewById(R.id.tvUniDescription)
        tvProgrammes = findViewById(R.id.tvProgrammes)
        tvRequirements = findViewById(R.id.tvRequirements)
        tvAppInfo = findViewById(R.id.tvAppInfo)
        tvImportantInfo = findViewById(R.id.tvImportantInfo)
        btnApply = findViewById(R.id.btnApply)
    }

    private fun displayUniversityDetails(uni: University) {
        tvUniName.text = uni.name
        tvUniLocation.text = uni.location
        tvUniDescription.text = uni.longDescription
        tvProgrammes.text = uni.programmes
        tvRequirements.text = uni.requirements
        tvAppInfo.text = uni.applicationInfo
        tvImportantInfo.text = uni.importantInfo
        ivUniImage.setImageResource(uni.logoResId)
    }

    private fun setupListeners() {
        btnApply.setOnClickListener {
            Toast.makeText(this, "Application process will be implemented soon!", Toast.LENGTH_SHORT).show()
        }
    }
}
