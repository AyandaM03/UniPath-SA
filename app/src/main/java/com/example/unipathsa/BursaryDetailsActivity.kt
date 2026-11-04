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
        findViewById<TextView>(R.id.tvBursaryDetailsProvider).text = provider.ifBlank { getString(R.string.default_education_trust) }
        findViewById<TextView>(R.id.tvBursaryDetailsAmount).text = amount.ifBlank { getString(R.string.default_full_tuition) }
        findViewById<TextView>(R.id.tvBursaryDetailsClosing).text = getString(R.string.closes_date_format, closingDate.ifBlank { "31 Oct" })
        findViewById<TextView>(R.id.tvBursaryDetailsCategory).text = category.ifBlank { getString(R.string.default_general_category) }
        findViewById<TextView>(R.id.tvBursaryDetailsDescription).text =
            description.ifBlank { getString(R.string.no_bursary_desc_msg) }

        val btnApply = findViewById<Button>(R.id.btnBursaryApply)
        if (website.isBlank()) {
            btnApply.isEnabled = false
            btnApply.text = getString(R.string.no_app_link_msg)
        } else {
            btnApply.setOnClickListener {
                try {
                    val url = if (website.startsWith("http")) website else "https://$website"
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (e: Exception) {
                    btnApply.isEnabled = false
                    btnApply.text = getString(R.string.no_app_link_msg)
                }
            }
        }

        findViewById<ImageButton>(R.id.btnBursaryDetailsBack).setOnClickListener {
            finish()
        }
    }
}