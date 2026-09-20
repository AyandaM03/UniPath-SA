package com.example.unipathsa

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore

class ExploreActivity : AppCompatActivity() {

    private lateinit var recyclerCourses: RecyclerView
    private lateinit var adapter: ExploreCourseAdapter
    private lateinit var tvNoCourses: android.widget.TextView

    private val allCourses = mutableListOf<Course>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        recyclerCourses = findViewById(R.id.recyclerCourses)
        tvNoCourses = findViewById(R.id.tvNoCourses)
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupCategory)

        adapter = ExploreCourseAdapter(emptyList())
        recyclerCourses.layoutManager = LinearLayoutManager(this)
        recyclerCourses.adapter = adapter

        loadCourses()

        // Whenever a different chip is selected, re-filter the list
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val selectedCategory = when (checkedIds[0]) {
                R.id.chipIT -> "IT"
                R.id.chipCommerce -> "Commerce"
                R.id.chipEngineering -> "Engineering"
                R.id.chipLaw -> "Law"
                else -> "All"
            }
            applyFilter(selectedCategory)
        }

        BottomNavHelper.setup(this, R.id.nav_explore)
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
                applyFilter("All")
            }
            .addOnFailureListener {
                tvNoCourses.visibility = View.VISIBLE
            }
    }

    private fun applyFilter(category: String) {
        val filtered = if (category == "All") {
            allCourses
        } else {
            allCourses.filter { it.category.equals(category, ignoreCase = true) }
        }

        adapter.updateList(filtered)
        tvNoCourses.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}