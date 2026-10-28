package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class SavedActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "SavedActivity"
    }

    private lateinit var recyclerSaved: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var llEmptyContainer: LinearLayout
    private lateinit var btnExplorePrompt: MaterialButton
    private lateinit var btnNotif: RelativeLayout
    private lateinit var tvNotifBadge: TextView
    private lateinit var pbLoading: ProgressBar

    private lateinit var btnTabInstitutions: MaterialButton
    private lateinit var btnTabCourses: MaterialButton
    private lateinit var btnTabBursaries: MaterialButton

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val liveInstitutions = mutableListOf<Institution>()
    private val liveCourses = mutableListOf<Course>()
    private val liveBursaries = mutableListOf<Bursary>()

    private lateinit var institutionAdapter: InstitutionAdapter
    private lateinit var courseAdapter: ExploreCourseAdapter
    private lateinit var bursaryAdapter: BursaryAdapter

    private var activeTab = "institution" // "institution", "course", "bursary"
    private var userApsScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved)
        Log.d(TAG, "onCreate initialized")

        NavigationHelper.setupDrawer(this, R.id.navSaved)

        bindViews()
        setupAdapters()
        setupListeners()
        loadUnreadNotifications()
        loadUserAps()
        loadLiveSavedItems()
    }

    override fun onResume() {
        super.onResume()
        loadLiveSavedItems()
    }

    private fun bindViews() {
        recyclerSaved = findViewById(R.id.recyclerSaved)
        tvEmpty = findViewById(R.id.tvEmptySaved)
        llEmptyContainer = findViewById(R.id.llEmptySavedContainer)
        btnExplorePrompt = findViewById(R.id.btnExploreSavedPrompt)
        btnNotif = findViewById(R.id.btnNotifications)
        tvNotifBadge = findViewById(R.id.tvNotifBadge)
        pbLoading = findViewById(R.id.pbSavedLoading)

        btnTabInstitutions = findViewById(R.id.btnSavedInstitutions)
        btnTabCourses = findViewById(R.id.btnSavedCourses)
        btnTabBursaries = findViewById(R.id.btnSavedBursaries)

        recyclerSaved.layoutManager = LinearLayoutManager(this)
    }

    private fun setupAdapters() {
        // Reuses exact same adapters from source screens
        institutionAdapter = InstitutionAdapter(liveInstitutions) { inst ->
            val intent = Intent(this, UniversityDetailActivity::class.java)
            intent.putExtra("UNIVERSITY_DATA", inst.toUniversity())
            startActivity(intent)
        }

        courseAdapter = ExploreCourseAdapter(liveCourses, this, userApsScore) { course ->
            val intent = Intent(this, CourseDetailActivity::class.java)
            intent.putExtra("COURSE_DATA", course)
            startActivity(intent)
        }

        bursaryAdapter = BursaryAdapter(liveBursaries, this) { bursary ->
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
    }

    private fun setupListeners() {
        btnNotif.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        btnTabInstitutions.setOnClickListener {
            activeTab = "institution"
            highlightActiveTab()
            displayActiveTab()
        }

        btnTabCourses.setOnClickListener {
            activeTab = "course"
            highlightActiveTab()
            displayActiveTab()
        }

        btnTabBursaries.setOnClickListener {
            activeTab = "bursary"
            highlightActiveTab()
            displayActiveTab()
        }

        btnExplorePrompt.setOnClickListener {
            val intent = when (activeTab) {
                "institution" -> Intent(this, InstitutionsActivity::class.java)
                else -> Intent(this, ExploreActivity::class.java)
            }
            startActivity(intent)
        }
    }

    private fun highlightActiveTab() {
        val white = getColor(R.color.white)
        val primary = getColor(R.color.primary)
        val surface = getColor(R.color.surface)

        btnTabInstitutions.setBackgroundColor(if (activeTab == "institution") primary else surface)
        btnTabInstitutions.setTextColor(if (activeTab == "institution") white else primary)

        btnTabCourses.setBackgroundColor(if (activeTab == "course") primary else surface)
        btnTabCourses.setTextColor(if (activeTab == "course") white else primary)

        btnTabBursaries.setBackgroundColor(if (activeTab == "bursary") primary else surface)
        btnTabBursaries.setTextColor(if (activeTab == "bursary") white else primary)
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
                    courseAdapter.updateList(liveCourses, userApsScore)
                }
            }
    }

    // Reads users/{uid}/savedItems references and fetches live documents from primary collections
    private fun loadLiveSavedItems() {
        val uid = auth.currentUser?.uid ?: return
        pbLoading.visibility = View.VISIBLE

        db.collection("users").document(uid).collection("savedItems")
            .get()
            .addOnSuccessListener { snapshot ->
                liveInstitutions.clear()
                liveCourses.clear()
                liveBursaries.clear()

                if (snapshot == null || snapshot.isEmpty) {
                    pbLoading.visibility = View.GONE
                    displayActiveTab()
                    return@addOnSuccessListener
                }

                val references = snapshot.documents
                var pendingFetches = references.size

                for (doc in references) {
                    val rawId = doc.getString("id") ?: doc.id
                    val rawType = (doc.getString("type") ?: doc.getString("itemType") ?: "").lowercase(Locale.ROOT)
                    val rawTitle = doc.getString("title") ?: doc.getString("name") ?: ""

                    val targetType = when {
                        rawType.contains("institution") || rawType.contains("university") -> "institution"
                        rawType.contains("course") -> "course"
                        rawType.contains("bursary") -> "bursary"
                        else -> "institution"
                    }

                    fetchLiveDocument(targetType, rawId, rawTitle) {
                        pendingFetches--
                        if (pendingFetches <= 0) {
                            pbLoading.visibility = View.GONE
                            displayActiveTab()
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching saved items list", e)
                pbLoading.visibility = View.GONE
                displayActiveTab()
            }
    }

    // Fetches live document from primary collection by ID or name fallback
    private fun fetchLiveDocument(
        type: String,
        itemId: String,
        fallbackTitle: String,
        onComplete: () -> Unit
    ) {
        val collectionName = when (type) {
            "course" -> "courses"
            "bursary" -> "bursaries"
            else -> "institutions"
        }

        db.collection(collectionName).document(itemId).get()
            .addOnSuccessListener { liveDoc ->
                if (liveDoc.exists()) {
                    parseAndAddLiveModel(type, liveDoc)
                    onComplete()
                } else {
                    // Try fallback match by title/name if ID lookup failed
                    if (fallbackTitle.isNotBlank()) {
                        db.collection(collectionName).whereEqualTo("name", fallbackTitle).get()
                            .addOnSuccessListener { matchSnap ->
                                if (matchSnap != null && !matchSnap.isEmpty) {
                                    val matchedDoc = matchSnap.documents[0]
                                    parseAndAddLiveModel(type, matchedDoc)
                                } else {
                                    Log.d(TAG, "Live item $itemId ($fallbackTitle) no longer exists in $collectionName, skipping quietly.")
                                }
                                onComplete()
                            }
                            .addOnFailureListener { onComplete() }
                    } else {
                        Log.d(TAG, "Live item $itemId no longer exists in $collectionName, skipping quietly.")
                        onComplete()
                    }
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to fetch live item $itemId from $collectionName, skipping.")
                onComplete()
            }
    }

    private fun parseAndAddLiveModel(type: String, doc: com.google.firebase.firestore.DocumentSnapshot) {
        when (type) {
            "institution" -> {
                val inst = Institution.fromFirestore(doc)
                if (liveInstitutions.none { it.id == inst.id }) {
                    liveInstitutions.add(inst)
                }
            }
            "course" -> {
                val course = Course.fromFirestore(doc)
                if (liveCourses.none { it.id == course.id }) {
                    liveCourses.add(course)
                }
            }
            "bursary" -> {
                val bursary = Bursary.fromFirestore(doc)
                if (liveBursaries.none { it.id == bursary.id }) {
                    liveBursaries.add(bursary)
                }
            }
        }
    }

    private fun displayActiveTab() {
        highlightActiveTab()

        when (activeTab) {
            "institution" -> {
                institutionAdapter.updateList(liveInstitutions)
                recyclerSaved.adapter = institutionAdapter
                if (liveInstitutions.isEmpty()) {
                    llEmptyContainer.visibility = View.VISIBLE
                    recyclerSaved.visibility = View.GONE
                    tvEmpty.text = "No saved institutions yet — tap the bookmark icon on any institution card to save it here."
                } else {
                    llEmptyContainer.visibility = View.GONE
                    recyclerSaved.visibility = View.VISIBLE
                }
            }
            "course" -> {
                courseAdapter.updateList(liveCourses, userApsScore)
                recyclerSaved.adapter = courseAdapter
                if (liveCourses.isEmpty()) {
                    llEmptyContainer.visibility = View.VISIBLE
                    recyclerSaved.visibility = View.GONE
                    tvEmpty.text = "No saved courses yet — tap the bookmark icon on any course card to save it here."
                } else {
                    llEmptyContainer.visibility = View.GONE
                    recyclerSaved.visibility = View.VISIBLE
                }
            }
            "bursary" -> {
                bursaryAdapter.updateList(liveBursaries)
                recyclerSaved.adapter = bursaryAdapter
                if (liveBursaries.isEmpty()) {
                    llEmptyContainer.visibility = View.VISIBLE
                    recyclerSaved.visibility = View.GONE
                    tvEmpty.text = "No saved bursaries yet — tap the bookmark icon on any bursary card to save it here."
                } else {
                    llEmptyContainer.visibility = View.GONE
                    recyclerSaved.visibility = View.VISIBLE
                }
            }
        }
    }
}