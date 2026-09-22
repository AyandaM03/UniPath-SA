package com.example.unipathsa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BursaryDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bursary_details)

        val name = intent.getStringExtra("name") ?: ""
        val provider = intent.getStringExtra("provider") ?: ""
        val amount = intent.getStringExtra("amount") ?: ""
        val closingDate = intent.getStringExtra("closingDate") ?: ""
        val category = intent.getStringExtra("category") ?: ""
        val website = intent.getStringExtra("website") ?: ""
        val description = intent.getStringExtra("description") ?: ""

        findViewById<TextView>(R.id.tvBursaryDetailsName).text = name
        findViewById<TextView>(R.id.tvBursaryDetailsProvider).text = provider
        findViewById<TextView>(R.id.tvBursaryDetailsAmount).text = amount
        findViewById<TextView>(R.id.tvBursaryDetailsClosing).text = "Closes: $closingDate"
        findViewById<TextView>(R.id.tvBursaryDetailsCategory).text = category
        findViewById<TextView>(R.id.tvBursaryDetailsDescription).text =
            description.ifBlank { "No description available for this bursary yet." }

        val btnApply = findViewById<Button>(R.id.btnBursaryApply)
        if (website.isBlank()) {
            btnApply.isEnabled = false
            btnApply.text = "No application link available"
        } else {
            btnApply.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(website)))
            }
        }

        findViewById<ImageButton>(R.id.btnBursaryDetailsBack).setOnClickListener {
            finish()
        }
    }
}