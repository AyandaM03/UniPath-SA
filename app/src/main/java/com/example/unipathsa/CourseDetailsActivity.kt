package com.example.unipathsa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CourseDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_details)

        val name = intent.getStringExtra("name") ?: ""
        val university = intent.getStringExtra("university") ?: ""
        val category = intent.getStringExtra("category") ?: ""
        val duration = intent.getStringExtra("duration") ?: ""
        val apsRequired = intent.getIntExtra("apsRequired", 0)
        val website = intent.getStringExtra("website") ?: ""
        val description = intent.getStringExtra("description") ?: ""

        findViewById<TextView>(R.id.tvCourseDetailsName).text = name
        findViewById<TextView>(R.id.tvCourseDetailsUniversity).text = university
        findViewById<TextView>(R.id.tvCourseDetailsCategory).text = category
        findViewById<TextView>(R.id.tvCourseDetailsDuration).text = duration
        findViewById<TextView>(R.id.tvCourseDetailsAps).text =
            "You need an APS score of $apsRequired or higher to qualify for this course."

        findViewById<TextView>(R.id.tvCourseDetailsDescription).text =
            description.ifBlank { "No description available for this course yet." }

        val btnWebsite = findViewById<Button>(R.id.btnCourseVisitWebsite)
        if (website.isBlank()) {
            btnWebsite.isEnabled = false
            btnWebsite.text = "No website available"
        } else {
            btnWebsite.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(website)))
            }
        }

        findViewById<ImageButton>(R.id.btnCourseDetailsBack).setOnClickListener {
            finish()
        }
    }
}