package com.example.unipathsa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class InstitutionDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_institution_details)

        // Pull the data passed in from the Intent
        val name = intent.getStringExtra("name") ?: ""
        val province = intent.getStringExtra("province") ?: ""
        val type = intent.getStringExtra("type") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val website = intent.getStringExtra("website") ?: ""
        val freeToApply = intent.getBooleanExtra("freeToApply", false)

        findViewById<TextView>(R.id.tvDetailName).text = name
        findViewById<TextView>(R.id.tvDetailProvinceType).text = "$type • $province"
        findViewById<TextView>(R.id.tvDetailFreeToApply).text =
            if (freeToApply) "Free to apply" else "Application fee applies"
        findViewById<TextView>(R.id.tvDetailDescription).text =
            description.ifBlank { "No description available yet." }

        Glide.with(this)
            .load(imageUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_launcher_background)
            .into(findViewById<ImageView>(R.id.imgDetailInstitution))

        findViewById<Button>(R.id.btnVisitWebsite).setOnClickListener {
            if (website.isNotBlank()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(website)))
            }
        }
    }
}