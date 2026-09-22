package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast

class HomeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val tvStudentName = findViewById<TextView>(R.id.tvStudentName)
        val name = FirebaseAuth.getInstance().currentUser?.displayName
        tvStudentName.text = if (!name.isNullOrEmpty()) name else "Student"

        findViewById<Button>(R.id.btnAddMarksCard).setOnClickListener {
            startActivity(Intent(this, ApsCalculatorActivity::class.java))
        }

        findViewById<TextView>(R.id.tvSeeAllInstitutions).setOnClickListener {
            startActivity(Intent(this, InstitutionsActivity::class.java))
        }
        findViewById<TextView>(R.id.tvSeeAllCourses).setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }
        findViewById<TextView>(R.id.tvSeeAllBursaries).setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnNotifications).setOnClickListener {
            Toast.makeText(this, "Notifications — coming soon!", Toast.LENGTH_SHORT).show()
        }

        loadFeaturedInstitutions()
        loadPopularCourses()
        loadBursaries()

        BottomNavHelper.setup(this, R.id.nav_home)
    }

    private fun loadFeaturedInstitutions() {
        val recycler = findViewById<RecyclerView>(R.id.recyclerHomeInstitutions)
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        db.collection("institutions")
            .limit(6)
            .get()
            .addOnSuccessListener { documents ->
                val list = documents.map { doc ->
                    val institution = doc.toObject(Institution::class.java)
                    institution.id = doc.id
                    institution
                }
                recycler.adapter = HomeInstitutionAdapter(list) { institution ->
                    val intent = Intent(this, InstitutionDetailsActivity::class.java)
                    intent.putExtra("name", institution.name)
                    intent.putExtra("province", institution.province)
                    intent.putExtra("type", institution.type)
                    intent.putExtra("description", institution.description)
                    intent.putExtra("imageUrl", institution.imageUrl)
                    intent.putExtra("website", institution.website)
                    intent.putExtra("freeToApply", institution.freeToApply)
                    startActivity(intent)
                }
            }
    }

    private fun loadPopularCourses() {
        val recycler = findViewById<RecyclerView>(R.id.recyclerHomeCourses)
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        db.collection("courses")
            .limit(6)
            .get()
            .addOnSuccessListener { documents ->
                val list = documents.map { doc ->
                    val course = doc.toObject(Course::class.java)
                    course.id = doc.id
                    course
                }
                recycler.adapter = HomeCourseAdapter(list)
            }
    }

    private fun loadBursaries() {
        val recycler = findViewById<RecyclerView>(R.id.recyclerHomeBursaries)
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        db.collection("bursaries")
            .limit(6)
            .get()
            .addOnSuccessListener { documents ->
                val list = documents.map { doc ->
                    val bursary = doc.toObject(Bursary::class.java)
                    bursary.id = doc.id
                    bursary
                }
                recycler.adapter = HomeBursaryAdapter(list)
            }
    }
}