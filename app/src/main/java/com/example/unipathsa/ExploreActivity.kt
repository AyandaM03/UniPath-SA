package com.example.unipathsa

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore

class ExploreActivity : AppCompatActivity() {

    private lateinit var recyclerCourses: RecyclerView
    private lateinit var courseAdapter: ExploreCourseAdapter
    private lateinit var bursaryAdapter: BursaryAdapter
    private lateinit var tvNoCourses: TextView
    private lateinit var btnCoursesTab: Button
    private lateinit var btnBursariesTab: Button

    private val allCourses = mutableListOf<Course>()
    private val allBursaries = mutableListOf<Bursary>()
    private val db = FirebaseFirestore.getInstance()

    private var isShowingCourses = true
    private var selectedCategory = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        recyclerCourses = findViewById(R.id.recyclerCourses)
        tvNoCourses = findViewById(R.id.tvNoCourses)
        btnCoursesTab = findViewById(R.id.btnCoursesTab)
        btnBursariesTab = findViewById(R.id.btnBursariesTab)
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupCategory)

        courseAdapter = ExploreCourseAdapter(emptyList()) { course ->
            val intent = android.content.Intent(this, CourseDetailsActivity::class.java)
            intent.putExtra("name", course.name)
            intent.putExtra("university", course.university)
            intent.putExtra("category", course.category)
            intent.putExtra("duration", course.duration)
            intent.putExtra("apsRequired", course.apsRequired)
            intent.putExtra("website", course.website)
            intent.putExtra("description", course.description)
            startActivity(intent)
        }

        bursaryAdapter = BursaryAdapter(emptyList()) { bursary ->
            val intent = android.content.Intent(this, BursaryDetailsActivity::class.java)
            intent.putExtra("name", bursary.name)
            intent.putExtra("provider", bursary.provider)
            intent.putExtra("amount", bursary.amount)
            intent.putExtra("closingDate", bursary.closingDate)
            intent.putExtra("category", bursary.category)
            intent.putExtra("website", bursary.website)
            intent.putExtra("description", bursary.description)
            startActivity(intent)
        }

        recyclerCourses.layoutManager = LinearLayoutManager(this)
        recyclerCourses.adapter = courseAdapter

        loadCourses()
        loadBursaries()

        btnCoursesTab.setOnClickListener {
            isShowingCourses = true
            highlightActiveTab()
            recyclerCourses.adapter = courseAdapter
            applyFilter()
        }

        btnBursariesTab.setOnClickListener {
            isShowingCourses = false
            highlightActiveTab()
            recyclerCourses.adapter = bursaryAdapter
            applyFilter()
        }

        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            selectedCategory = when (checkedIds[0]) {
                R.id.chipIT -> "IT"
                R.id.chipCommerce -> "Commerce"
                R.id.chipEngineering -> "Engineering"
                R.id.chipLaw -> "Law"
                else -> "All"
            }
            applyFilter()
        }

        BottomNavHelper.setup(this, R.id.nav_explore)
    }

    private fun highlightActiveTab() {
        if (isShowingCourses) {
            btnCoursesTab.setBackgroundColor(android.graphics.Color.WHITE)
            btnCoursesTab.setTextColor(resources.getColor(R.color.header_blue_dark, theme))
            btnBursariesTab.setBackgroundColor(android.graphics.Color.parseColor("#3B4A9C"))
            btnBursariesTab.setTextColor(android.graphics.Color.WHITE)
        } else {
            btnBursariesTab.setBackgroundColor(android.graphics.Color.WHITE)
            btnBursariesTab.setTextColor(resources.getColor(R.color.header_blue_dark, theme))
            btnCoursesTab.setBackgroundColor(android.graphics.Color.parseColor("#3B4A9C"))
            btnCoursesTab.setTextColor(android.graphics.Color.WHITE)
        }
    }

    private fun loadCourses() {
        db.collection("courses")
            .get()
            .addOnSuccessListener { documents ->
                allCourses.clear()
                for (document in documents) {
                    val course = document.toObject(Course::class.java)
                    course.id = document.id
                    allCourses.add(course)
                }
                if (isShowingCourses) applyFilter()
            }
            .addOnFailureListener {
                if (isShowingCourses) tvNoCourses.visibility = View.VISIBLE
            }
    }

    private fun loadBursaries() {
        db.collection("bursaries")
            .get()
            .addOnSuccessListener { documents ->
                allBursaries.clear()
                for (document in documents) {
                    val bursary = document.toObject(Bursary::class.java)
                    bursary.id = document.id
                    allBursaries.add(bursary)
                }
                if (!isShowingCourses) applyFilter()
            }
            .addOnFailureListener {
                if (!isShowingCourses) tvNoCourses.visibility = View.VISIBLE
            }
    }

    private fun applyFilter() {
        if (isShowingCourses) {
            val filtered = if (selectedCategory == "All") {
                allCourses
            } else {
                allCourses.filter { it.category.equals(selectedCategory, ignoreCase = true) }
            }
            courseAdapter.updateList(filtered)
            tvNoCourses.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        } else {
            val filtered = if (selectedCategory == "All") {
                allBursaries
            } else {
                allBursaries.filter { it.category.equals(selectedCategory, ignoreCase = true) }
            }
            bursaryAdapter.updateList(filtered)
            tvNoCourses.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}