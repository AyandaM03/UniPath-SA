package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ExploreActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "ExploreActivity"
    }

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var etSearch: EditText
    private lateinit var recyclerCourses: RecyclerView
    private lateinit var tvNoCourses: TextView
    private lateinit var pbLoading: ProgressBar
    private lateinit var btnCoursesTab: MaterialButton
    private lateinit var btnBursariesTab: MaterialButton
    private lateinit var btnNotif: RelativeLayout
    private lateinit var tvNotifBadge: TextView

    private lateinit var courseAdapter: ExploreCourseAdapter
    private lateinit var bursaryAdapter: BursaryAdapter

    private val allCourses = mutableListOf<Course>()
    private val allBursaries = mutableListOf<Bursary>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var userApsScore = 0
    private var isShowingCourses = true
    private var selectedCategory = "All"
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)
        Log.d(TAG, "onCreate initialized")

        NavigationHelper.setupDrawer(this, R.id.navExplore)

        bindViews()
        setupAdapters()
        setupListeners()
        loadUnreadNotifications()
        loadUserAps()
        loadCourses()
        loadBursaries()
    }

    private fun bindViews() {
        swipeRefresh = findViewById(R.id.swipeRefreshExplore)
        etSearch = findViewById(R.id.etSearchExplore)
        recyclerCourses = findViewById(R.id.recyclerCourses)
        tvNoCourses = findViewById(R.id.tvNoCourses)
        pbLoading = findViewById(R.id.pbExploreLoading)
        btnCoursesTab = findViewById(R.id.btnCoursesTab)
        btnBursariesTab = findViewById(R.id.btnBursariesTab)
        btnNotif = findViewById(R.id.btnNotifications)
        tvNotifBadge = findViewById(R.id.tvNotifBadge)

        recyclerCourses.layoutManager = LinearLayoutManager(this)
    }

    private fun setupAdapters() {
        courseAdapter = ExploreCourseAdapter(emptyList(), this, userApsScore) { course ->
            val intent = Intent(this, CourseDetailActivity::class.java)
            intent.putExtra("COURSE_DATA", course)
            startActivity(intent)
        }

        bursaryAdapter = BursaryAdapter(emptyList(), this) { bursary ->
            val intent = Intent(this, BursaryDetailsActivity::class.java)
            intent.putExtra("name", bursary.name)
            intent.putExtra("provider", bursary.provider)
            intent.putExtra("amount", bursary.amount)
            intent.putExtra("closingDate", bursary.closingDate)
            intent.putExtra("category", bursary.category)
            intent.putExtra("website", bursary.website)
            intent.putExtra("description", bursary.description)
            startActivity(intent)
        }

        recyclerCourses.adapter = courseAdapter
    }

    private fun setupListeners() {
        btnNotif.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        swipeRefresh.setOnRefreshListener {
            loadCourses()
            loadBursaries()
        }

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

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                applyFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<ChipGroup>(R.id.chipGroupCategory)?.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) {
                selectedCategory = "All"
            } else {
                selectedCategory = when (checkedIds[0]) {
                    R.id.chipScience -> "Science"
                    R.id.chipCommerce -> "Commerce"
                    R.id.chipEngineering -> "Engineering"
                    R.id.chipHealth -> "Health"
                    R.id.chipArts -> "Arts"
                    else -> "All"
                }
            }
            applyFilter()
        }
    }

    private fun loadUnreadNotifications() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("notifications")
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snap ->
                val count = snap?.size() ?: 0
                if (count > 0) {
                    tvNotifBadge.visibility = View.VISIBLE
                    tvNotifBadge.text = if (count > 9) "9+" else count.toString()
                } else {
                    tvNotifBadge.visibility = View.GONE
                }
            }
    }

    private fun loadUserAps() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    userApsScore = (doc.getLong("aps") ?: 0L).toInt()
                    courseAdapter.updateList(allCourses, userApsScore)
                }
            }
    }

    private fun highlightActiveTab() {
        if (isShowingCourses) {
            btnCoursesTab.setBackgroundColor(getColor(R.color.primary))
            btnCoursesTab.setTextColor(getColor(R.color.white))
            btnBursariesTab.setBackgroundColor(getColor(R.color.surface))
            btnBursariesTab.setTextColor(getColor(R.color.primary))
        } else {
            btnBursariesTab.setBackgroundColor(getColor(R.color.primary))
            btnBursariesTab.setTextColor(getColor(R.color.white))
            btnCoursesTab.setBackgroundColor(getColor(R.color.surface))
            btnCoursesTab.setTextColor(getColor(R.color.primary))
        }
    }

    private fun loadCourses() {
        pbLoading.visibility = View.VISIBLE
        db.collection("courses")
            .get()
            .addOnSuccessListener { documents ->
                allCourses.clear()
                for (doc in documents) {
                    val course = Course.fromFirestore(doc)
                    allCourses.add(course)
                }
                pbLoading.visibility = View.GONE
                swipeRefresh.isRefreshing = false
                Log.d(TAG, "Loaded ${allCourses.size} courses using Course.fromFirestore")
                if (isShowingCourses) applyFilter()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load courses from Firestore", e)
                pbLoading.visibility = View.GONE
                swipeRefresh.isRefreshing = false
                if (isShowingCourses) tvNoCourses.visibility = View.VISIBLE
            }
    }

    private fun loadBursaries() {
        db.collection("bursaries")
            .get()
            .addOnSuccessListener { documents ->
                allBursaries.clear()
                for (doc in documents) {
                    val bursary = Bursary.fromFirestore(doc)
                    allBursaries.add(bursary)
                }
                Log.d(TAG, "Loaded ${allBursaries.size} bursaries using Bursary.fromFirestore")
                if (!isShowingCourses) applyFilter()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load bursaries from Firestore", e)
                if (!isShowingCourses) tvNoCourses.visibility = View.VISIBLE
            }
    }

    private fun applyFilter() {
        if (isShowingCourses) {
            val filtered = allCourses.filter { course ->
                val matchesCategory = if (selectedCategory.equals("Science", ignoreCase = true)) {
                    course.field.contains("Science", ignoreCase = true) ||
                            course.field.contains("IT", ignoreCase = true) ||
                            course.field.contains("Computer", ignoreCase = true) ||
                            course.field.contains("Technology", ignoreCase = true) ||
                            course.field.contains("Info", ignoreCase = true) ||
                            course.name.contains("Science", ignoreCase = true) ||
                            course.name.contains("Computer", ignoreCase = true) ||
                            course.name.contains("BSc", ignoreCase = true)
                } else if (selectedCategory != "All") {
                    course.field.contains(selectedCategory, ignoreCase = true)
                } else {
                    true
                }

                val matchesSearch = searchQuery.isEmpty() ||
                        course.name.contains(searchQuery, ignoreCase = true) ||
                        course.institution.contains(searchQuery, ignoreCase = true) ||
                        course.field.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }
            courseAdapter.updateList(filtered, userApsScore)
            tvNoCourses.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        } else {
            val filtered = allBursaries.filter { bursary ->
                val matchesCategory = if (selectedCategory.equals("Science", ignoreCase = true)) {
                    bursary.category.contains("Science", ignoreCase = true) ||
                            bursary.category.contains("IT", ignoreCase = true) ||
                            bursary.category.contains("Tech", ignoreCase = true) ||
                            bursary.name.contains("Science", ignoreCase = true) ||
                            bursary.name.contains("IT", ignoreCase = true)
                } else if (selectedCategory != "All") {
                    bursary.category.contains(selectedCategory, ignoreCase = true)
                } else {
                    true
                }

                val matchesSearch = searchQuery.isEmpty() ||
                        bursary.name.contains(searchQuery, ignoreCase = true) ||
                        bursary.provider.contains(searchQuery, ignoreCase = true) ||
                        bursary.category.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }
            bursaryAdapter.updateList(filtered)
            tvNoCourses.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}