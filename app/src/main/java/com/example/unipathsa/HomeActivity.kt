package com.example.unipathsa

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import android.Manifest
import android.os.Build
import androidx.core.app.ActivityCompat

class HomeActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "HomeActivity"
    }

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var btnNotifications: RelativeLayout
    private lateinit var tvNotifBadge: TextView

    private lateinit var tvGreeting: TextView
    private lateinit var tvSlogan: TextView

    // Preparation Hero Card views
    private lateinit var cvPrepCard: MaterialCardView
    private lateinit var tvPrepPercent: TextView
    private lateinit var tvPrepSummary: TextView
    private lateinit var pbPrepProgress: ProgressBar
    private lateinit var cgPrepChips: ChipGroup
    private lateinit var btnNextStep: MaterialButton

    // 2x2 Stat Cards views
    private lateinit var cvStatAps: MaterialCardView
    private lateinit var tvStatApsValue: TextView
    private lateinit var btnStatApsAction: TextView

    private lateinit var cvStatEligible: MaterialCardView
    private lateinit var tvStatEligibleValue: TextView

    private lateinit var cvStatApplications: MaterialCardView
    private lateinit var tvStatApplicationsValue: TextView

    private lateinit var cvStatSaved: MaterialCardView
    private lateinit var tvStatSavedValue: TextView

    // Upcoming Deadlines views
    private lateinit var cvEmptyDeadlines: MaterialCardView
    private lateinit var rvUpcomingDeadlines: RecyclerView

    // Recommended Courses views
    private lateinit var cvApsPrompt: MaterialCardView
    private lateinit var rvRecommendedCourses: RecyclerView

    // Explore Institutions views
    private lateinit var rvHomeInstitutions: RecyclerView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val prepCalculator = PreparationCalculator()

    private var userApsScore = 0
    private val allCoursesList = mutableListOf<Course>()
    private val allInstitutionsList = mutableListOf<Institution>()
    private var firstPendingTask: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        Log.d(TAG, "onCreate started")

        NavigationHelper.setupDrawer(this, R.id.navHome)

        bindViews()
        setupListeners()
        loadDashboardData()
        // Ask for notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }

        // TEMPORARY test: delete this line once you've seen the notification
        //NotificationHelper.show(this, "Test", "If you see this, notifications work")
    }



    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: refreshing dashboard data")
        loadDashboardData()
    }

    private fun bindViews() {
        swipeRefresh = findViewById(R.id.swipeRefreshHome)
        btnNotifications = findViewById(R.id.btnNotifications)
        tvNotifBadge = findViewById(R.id.tvNotifBadge)

        tvGreeting = findViewById(R.id.tvHomeGreeting)
        tvSlogan = findViewById(R.id.tvHomeSlogan)

        cvPrepCard = findViewById(R.id.cvPreparationCard)
        tvPrepPercent = findViewById(R.id.tvPreparationPercentage)
        tvPrepSummary = findViewById(R.id.tvPreparationSummary)
        pbPrepProgress = findViewById(R.id.pbPreparation)
        cgPrepChips = findViewById(R.id.cgPreparationChips)
        btnNextStep = findViewById(R.id.btnNextStep)

        cvStatAps = findViewById(R.id.cvStatAps)
        tvStatApsValue = findViewById(R.id.tvStatApsValue)
        btnStatApsAction = findViewById(R.id.btnStatApsAction)

        cvStatEligible = findViewById(R.id.cvStatEligible)
        tvStatEligibleValue = findViewById(R.id.tvStatEligibleValue)

        cvStatApplications = findViewById(R.id.cvStatApplications)
        tvStatApplicationsValue = findViewById(R.id.tvStatApplicationsValue)

        cvStatSaved = findViewById(R.id.cvStatSaved)
        tvStatSavedValue = findViewById(R.id.tvStatSavedValue)

        cvEmptyDeadlines = findViewById(R.id.cvEmptyDeadlines)
        rvUpcomingDeadlines = findViewById(R.id.rvUpcomingDeadlines)
        rvUpcomingDeadlines.layoutManager = LinearLayoutManager(this)

        cvApsPrompt = findViewById(R.id.cvApsPrompt)
        rvRecommendedCourses = findViewById(R.id.rvRecommendedCourses)
        rvRecommendedCourses.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        rvHomeInstitutions = findViewById(R.id.rvHomeInstitutions)
        rvHomeInstitutions.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupListeners() {
        swipeRefresh.setOnRefreshListener {
            Log.d(TAG, "SwipeRefresh triggered")
            loadDashboardData()
        }

        btnNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        cvStatAps.setOnClickListener {
            startActivity(Intent(this, ApsCalculatorActivity::class.java))
        }

        cvStatEligible.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        cvStatApplications.setOnClickListener {
            startActivity(Intent(this, ApplicationsActivity::class.java))
        }

        cvStatSaved.setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }

        btnNextStep.setOnClickListener {
            openTaskDestination(firstPendingTask ?: "save profile")
        }

        findViewById<View>(R.id.btnAddDeadlineApp)?.setOnClickListener {
            startActivity(Intent(this, ApplicationsActivity::class.java))
        }

        findViewById<View>(R.id.btnCalculateApsPrompt)?.setOnClickListener {
            startActivity(Intent(this, ApsCalculatorActivity::class.java))
        }

        findViewById<TextView>(R.id.tvSeeAllDeadlines)?.setOnClickListener {
            startActivity(Intent(this, ApplicationsActivity::class.java))
        }

        findViewById<TextView>(R.id.tvSeeAllCourses)?.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        findViewById<TextView>(R.id.tvSeeAllInstitutions)?.setOnClickListener {
            startActivity(Intent(this, InstitutionsActivity::class.java))
        }
    }

    private fun loadDashboardData() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "User not authenticated in HomeActivity")
            swipeRefresh.isRefreshing = false
            return
        }

        updateGreetingHeader()
        fetchUnreadNotificationsCount(uid)
        fetchUserProfileAndPreparation(uid)
        fetchCoursesAndRecommendations()
        fetchFeaturedInstitutions()
    }

    private fun updateGreetingHeader() {
        val user = auth.currentUser
        val fullName = if (!user?.displayName.isNullOrBlank()) user?.displayName!! else getString(R.string.drawer_default_user)
        val firstName = fullName.trim().split(" ").firstOrNull() ?: getString(R.string.drawer_default_user)

        tvGreeting.text = getString(R.string.greeting_format, firstName)
        tvSlogan.text = getString(R.string.slogan)
    }

    private fun fetchUnreadNotificationsCount(uid: String) {
        db.collection("users").document(uid).collection("notifications")
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                val unreadCount = snapshot?.size() ?: 0
                if (unreadCount > 0) {
                    tvNotifBadge.visibility = View.VISIBLE
                    tvNotifBadge.text = if (unreadCount > 9) "9+" else unreadCount.toString()
                } else {
                    tvNotifBadge.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to read unread notifications count", e)
            }
    }

    private fun fetchUserProfileAndPreparation(uid: String) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val fullName = doc.getString("fullName") ?: doc.getString("name") ?: auth.currentUser?.displayName ?: getString(R.string.drawer_default_user)
                    val firstName = if (fullName.isNotBlank()) fullName.trim().split(" ").first() else getString(R.string.drawer_default_user)
                    tvGreeting.text = getString(R.string.greeting_format, firstName)

                    val phone = doc.getString("phone") ?: ""
                    val school = doc.getString("school") ?: doc.getString("highSchool") ?: ""
                    userApsScore = (doc.getLong("aps") ?: 0L).toInt()

                    updateApsStatCard(userApsScore)
                    fetchTaskStatesAndCalculatePreparation(uid, fullName.isNotBlank() || phone.isNotBlank(), school.isNotBlank(), userApsScore > 0)
                } else {
                    updateApsStatCard(0)
                    fetchTaskStatesAndCalculatePreparation(uid, false, false, false)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching user profile in HomeActivity", e)
                swipeRefresh.isRefreshing = false
            }
    }

    private fun updateApsStatCard(aps: Int) {
        if (aps > 0) {
            tvStatApsValue.text = aps.toString()
            btnStatApsAction.text = "Recalculate →"
        } else {
            tvStatApsValue.text = "Not calculated"
            btnStatApsAction.text = "Calculate APS →"
        }
    }

    private fun fetchTaskStatesAndCalculatePreparation(
        uid: String,
        isProfileSaved: Boolean,
        isSchoolAdded: Boolean,
        hasAps: Boolean
    ) {
        db.collection("users").document(uid).collection("subjects").get()
            .addOnSuccessListener { subjectsSnap ->
                val hasSubjects = subjectsSnap != null && !subjectsSnap.isEmpty

                db.collection("users").document(uid).collection("savedItems").get()
                    .addOnSuccessListener { savedSnap ->
                        val hasSavedItems = savedSnap != null && !savedSnap.isEmpty

                        db.collection("users").document(uid).collection("applications").get()
                            .addOnSuccessListener { appsSnap ->
                                val hasApplications = appsSnap != null && !appsSnap.isEmpty
                                val appsCount = appsSnap?.size() ?: 0
                                tvStatApplicationsValue.text = appsCount.toString()

                                updateUpcomingDeadlines(appsSnap?.documents?.map { ApplicationModel.fromFirestore(it) } ?: emptyList())

                                db.collection("users").document(uid).collection("documents").get()
                                    .addOnSuccessListener { docsSnap ->
                                        val hasDocuments = docsSnap != null && !docsSnap.isEmpty
                                        val savedCount = (savedSnap?.size() ?: 0)
                                        tvStatSavedValue.text = savedCount.toString()

                                        val state = PreparationTaskState(
                                            isProfileSaved = isProfileSaved,
                                            isSchoolAdded = isSchoolAdded,
                                            hasSubjects = hasSubjects,
                                            hasAps = hasAps,
                                            hasSavedItems = savedCount > 0,
                                            hasApplications = appsCount > 0,
                                            hasDocuments = hasDocuments
                                        )

                                        val result = prepCalculator.calculate(state)
                                        Log.d(TAG, "Preparation score: ${result.percentage}% (${result.completedCount}/7)")

                                        tvPrepPercent.text = "${result.percentage}%"
                                        pbPrepProgress.progress = result.percentage

                                        if (result.percentage == 100) {
                                            tvPrepSummary.text = getString(R.string.all_tasks_completed)
                                            btnNextStep.visibility = View.GONE
                                            runCelebrationAnimation()
                                        } else {
                                            tvPrepSummary.text = getString(R.string.tasks_completed_format, result.completedCount)
                                            btnNextStep.visibility = View.VISIBLE
                                            firstPendingTask = result.pendingTasks.firstOrNull()
                                            btnNextStep.text = getString(R.string.next_step_format, firstPendingTask ?: "Complete Setup")
                                        }

                                        populatePreparationChips(result)
                                        swipeRefresh.isRefreshing = false
                                    }
                            }
                    }
            }
    }

    private fun runCelebrationAnimation() {
        val scaleX = ObjectAnimator.ofFloat(cvPrepCard, View.SCALE_X, 1.0f, 1.04f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(cvPrepCard, View.SCALE_Y, 1.0f, 1.04f, 1.0f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 450
            start()
        }
    }

    private fun populatePreparationChips(result: PreparationResult) {
        cgPrepChips.removeAllViews()

        for (task in result.completedTasks) {
            val chip = Chip(this).apply {
                text = "✔ $task"
                chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.status_accepted_bg))
                setTextColor(getColor(R.color.status_accepted_fg))
                isClickable = false
            }
            cgPrepChips.addView(chip)
        }

        for (task in result.pendingTasks) {
            val chip = Chip(this).apply {
                text = "+ $task"
                chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.surface))
                setTextColor(getColor(R.color.primary))
                isClickable = true
                setOnClickListener { openTaskDestination(task) }
            }
            cgPrepChips.addView(chip)
        }
    }

    private fun openTaskDestination(taskName: String) {
        when (taskName.lowercase(Locale.ROOT)) {
            "save profile", "add school" -> startActivity(Intent(this, SettingsActivity::class.java))
            "add marks", "calculate aps", "subject marks", "aps calculated" -> startActivity(Intent(this, ApsCalculatorActivity::class.java))
            "save courses", "saved items" -> startActivity(Intent(this, ExploreActivity::class.java))
            "add applications", "applications" -> startActivity(Intent(this, ApplicationsActivity::class.java))
            "upload documents", "documents" -> startActivity(Intent(this, DocumentsActivity::class.java))
            else -> startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun updateUpcomingDeadlines(apps: List<ApplicationModel>) {
        if (apps.isEmpty()) {
            cvEmptyDeadlines.visibility = View.VISIBLE
            rvUpcomingDeadlines.visibility = View.GONE
        } else {
            val upcoming = apps.take(2)
            cvEmptyDeadlines.visibility = View.GONE
            rvUpcomingDeadlines.visibility = View.VISIBLE
            rvUpcomingDeadlines.adapter = DeadlineAdapter(upcoming) { selectedApp ->
                val intent = Intent(this, ApplicationDetailActivity::class.java)
                intent.putExtra("APP_ID", selectedApp.id)
                startActivity(intent)
            }
        }
    }

    private fun fetchCoursesAndRecommendations() {
        db.collection("courses")
            .get()
            .addOnSuccessListener { snapshot ->
                allCoursesList.clear()
                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        allCoursesList.add(Course.fromFirestore(doc))
                    }
                }

                if (userApsScore > 0) {
                    val eligible = allCoursesList.filter { it.apsRequirement in 1..userApsScore }
                    tvStatEligibleValue.text = eligible.size.toString()

                    cvApsPrompt.visibility = View.GONE
                    rvRecommendedCourses.visibility = View.VISIBLE

                    val recommended = if (eligible.isNotEmpty()) eligible.take(6) else allCoursesList.take(6)
                    rvRecommendedCourses.adapter = HomeCourseAdapter(recommended) { course ->
                        val intent = Intent(this, CourseDetailActivity::class.java)
                        intent.putExtra("COURSE_DATA", course)
                        startActivity(intent)
                    }
                } else {
                    tvStatEligibleValue.text = "0"
                    cvApsPrompt.visibility = View.VISIBLE
                    rvRecommendedCourses.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load courses from Firestore", e)
            }
    }

    private fun fetchFeaturedInstitutions() {
        db.collection("institutions")
            .limit(8)
            .get()
            .addOnSuccessListener { snapshot ->
                allInstitutionsList.clear()
                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        val inst = Institution.fromFirestore(doc)
                        allInstitutionsList.add(inst)
                    }
                }

                rvHomeInstitutions.adapter = HomeInstitutionAdapter(allInstitutionsList) { inst ->
                    val intent = Intent(this, UniversityDetailActivity::class.java)
                    intent.putExtra("UNIVERSITY_DATA", inst.toUniversity())
                    startActivity(intent)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load institutions from Firestore", e)
            }
    }

    private inner class DeadlineAdapter(
        private val list: List<ApplicationModel>,
        private val onItemClick: (ApplicationModel) -> Unit
    ) : RecyclerView.Adapter<DeadlineAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvUni: TextView = view.findViewById(R.id.tvDeadlineUni)
            val tvCourse: TextView = view.findViewById(R.id.tvDeadlineCourse)
            val tvDate: TextView = view.findViewById(R.id.tvDeadlineDate)
            val tvStatus: TextView = view.findViewById(R.id.tvDeadlineStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_deadline, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvUni.text = item.university
            holder.tvCourse.text = item.course
            holder.tvDate.text = "Closes: ${item.deadline.ifBlank { "30 Sep 2025" }}"

            // Map English status string from Firestore to localized display string
            holder.tvStatus.text = when (item.status.lowercase(Locale.ROOT)) {
                "draft" -> getString(R.string.status_draft)
                "submitted" -> getString(R.string.status_submitted)
                "under review", "in review" -> getString(R.string.status_under_review)
                "accepted" -> getString(R.string.status_accepted)
                "rejected" -> getString(R.string.status_rejected)
                else -> item.status
            }

            holder.itemView.setOnClickListener { onItemClick(item) }
        }

        override fun getItemCount(): Int = list.size


    }
}