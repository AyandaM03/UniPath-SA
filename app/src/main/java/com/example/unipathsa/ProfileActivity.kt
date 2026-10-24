package com.example.unipathsa

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "ProfileActivity"
    }

    private lateinit var tvAvatar: TextView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvGradeSchool: TextView

    private lateinit var tvApsStat: TextView
    private lateinit var tvAppsStat: TextView
    private lateinit var tvSavedStat: TextView

    private lateinit var tvCompletionPercent: TextView
    private lateinit var pbCompletion: ProgressBar

    private lateinit var llEmptyResults: LinearLayout
    private lateinit var rvSubjects: RecyclerView
    private lateinit var cgAchievements: ChipGroup

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val prepCalculator = PreparationCalculator()
    private val subjectsList = mutableListOf<ApsCalculatorActivity.SubjectInput>()
    private lateinit var subjectAdapter: ProfileSubjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        Log.d(TAG, "onCreate initialized")

        NavigationHelper.setupDrawer(this, R.id.navProfile)

        bindViews()
        setupListeners()
        loadProfileData()
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    // Connect XML views
    private fun bindViews() {
        tvAvatar = findViewById(R.id.tvProfileLargeAvatar)
        tvName = findViewById(R.id.tvProfileName)
        tvEmail = findViewById(R.id.tvProfileEmail)
        tvGradeSchool = findViewById(R.id.tvProfileGradeSchool)

        tvApsStat = findViewById(R.id.tvProfileAps)
        tvAppsStat = findViewById(R.id.tvProfileAppsCount)
        tvSavedStat = findViewById(R.id.tvProfileSavedCount)

        tvCompletionPercent = findViewById(R.id.tvProfileCompletionPercentage)
        pbCompletion = findViewById(R.id.pbProfileCompletion)

        llEmptyResults = findViewById(R.id.llEmptyResults)
        rvSubjects = findViewById(R.id.rvProfileSubjects)
        cgAchievements = findViewById(R.id.cgAchievementBadges)

        subjectAdapter = ProfileSubjectAdapter(subjectsList)
        rvSubjects.layoutManager = LinearLayoutManager(this)
        rvSubjects.adapter = subjectAdapter
    }

    // Attach click listeners to "My Tools" rows and actions
    private fun setupListeners() {
        findViewById<View>(R.id.btnEditProfile)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<View>(R.id.btnAddResultsPrompt)?.setOnClickListener {
            startActivity(Intent(this, ApsCalculatorActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnNavApplications)?.setOnClickListener {
            startActivity(Intent(this, ApplicationsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnNavDocuments)?.setOnClickListener {
            startActivity(Intent(this, DocumentsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnNavNotifications)?.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnNavSaved)?.setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnNavSettings)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    // Loads user profile, stats, subjects and achievements from Firestore
    private fun loadProfileData() {
        val user = auth.currentUser
        if (user == null) {
            Log.w(TAG, "User not authenticated in ProfileActivity")
            return
        }

        tvEmail.text = user.email ?: ""

        // Fetch User Record
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val fullName = doc.getString("fullName") ?: doc.getString("name") ?: user.displayName ?: "Learner Name"
                    val school = doc.getString("school") ?: doc.getString("highSchool") ?: "High School"
                    val grade = doc.getString("grade") ?: "Grade 12"
                    val apsScore = (doc.getLong("aps") ?: 0L).toInt()

                    tvName.text = fullName
                    tvAvatar.text = fullName.trim().take(1).uppercase()
                    tvGradeSchool.text = "$grade • $school"

                    tvApsStat.text = if (apsScore > 0) apsScore.toString() else "Not calculated"

                    // Fetch Subcollections for stats, subjects, and achievements
                    fetchSubcollectionsAndCalculateCompletion(user.uid, fullName.isNotBlank(), school.isNotBlank(), apsScore > 0)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load user profile from Firestore", e)
            }
    }

    // Fetches subcollections (applications, savedItems, subjects, documents) to compute stats & achievements
    private fun fetchSubcollectionsAndCalculateCompletion(
        uid: String,
        isProfileSaved: Boolean,
        isSchoolAdded: Boolean,
        hasAps: Boolean
    ) {
        db.collection("users").document(uid).collection("applications").get()
            .addOnSuccessListener { appsSnap ->
                val appsCount = appsSnap?.size() ?: 0
                tvAppsStat.text = appsCount.toString()

                db.collection("users").document(uid).collection("savedItems").get()
                    .addOnSuccessListener { savedSnap ->
                        val savedCount = savedSnap?.size() ?: 0
                        tvSavedStat.text = savedCount.toString()

                        db.collection("users").document(uid).collection("subjects").get()
                            .addOnSuccessListener { subSnap ->
                                subjectsList.clear()
                                val hasSubjects = subSnap != null && !subSnap.isEmpty

                                if (hasSubjects) {
                                    for (doc in subSnap!!.documents) {
                                        val subName = doc.getString("subjectName") ?: doc.id
                                        val pct = (doc.getLong("percentage") ?: 0L).toInt()
                                        subjectsList.add(ApsCalculatorActivity.SubjectInput(doc.id, subName, pct))
                                    }
                                    llEmptyResults.visibility = View.GONE
                                    rvSubjects.visibility = View.VISIBLE
                                    subjectAdapter.notifyDataSetChanged()
                                } else {
                                    llEmptyResults.visibility = View.VISIBLE
                                    rvSubjects.visibility = View.GONE
                                }

                                db.collection("users").document(uid).collection("documents").get()
                                    .addOnSuccessListener { docSnap ->
                                        val hasDocs = docSnap != null && !docSnap.isEmpty

                                        // Calculate completion via PreparationCalculator
                                        val taskState = PreparationTaskState(
                                            isProfileSaved = isProfileSaved,
                                            isSchoolAdded = isSchoolAdded,
                                            hasSubjects = hasSubjects,
                                            hasAps = hasAps,
                                            hasSavedItems = savedCount > 0,
                                            hasApplications = appsCount > 0,
                                            hasDocuments = hasDocs
                                        )

                                        val result = prepCalculator.calculate(taskState)
                                        tvCompletionPercent.text = "${result.percentage}% Profile Completed"
                                        pbCompletion.progress = result.percentage

                                        populateAchievementBadges(taskState)
                                    }
                            }
                    }
            }
    }

    // Populate achievement badges (unlocked vs locked)
    private fun populateAchievementBadges(state: PreparationTaskState) {
        cgAchievements.removeAllViews()

        val badges = listOf(
            Pair("Profile Created", state.isProfileSaved),
            Pair("APS Calculated", state.hasAps),
            Pair("Subjects Added", state.hasSubjects),
            Pair("Application Tracked", state.hasApplications),
            Pair("Documents Uploaded", state.hasDocuments),
            Pair("Items Saved", state.hasSavedItems)
        )

        for ((title, isUnlocked) in badges) {
            val chip = Chip(this).apply {
                text = if (isUnlocked) "🏆 $title" else "🔒 $title"
                if (isUnlocked) {
                    chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.status_accepted_bg))
                    setTextColor(getColor(R.color.status_accepted_fg))
                } else {
                    chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.background))
                    setTextColor(getColor(R.color.text_tertiary))
                }
                isClickable = false
            }
            cgAchievements.addView(chip)
        }
    }

    // RecyclerView Adapter for subject results on Profile
    private inner class ProfileSubjectAdapter(
        private val list: List<ApsCalculatorActivity.SubjectInput>
    ) : RecyclerView.Adapter<ProfileSubjectAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvSubjectName)
            val pbMark: ProgressBar = view.findViewById(R.id.pbDocsProgress)
            val tvMark: TextView = view.findViewById(R.id.tvDocsCount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_application, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvName.text = item.name

            // Hide unused views from item_application layout
            holder.itemView.findViewById<View>(R.id.tvUniversity)?.visibility = View.GONE
            holder.itemView.findViewById<View>(R.id.tvCourse)?.visibility = View.GONE
            holder.itemView.findViewById<View>(R.id.tvStatusChip)?.visibility = View.GONE
            holder.itemView.findViewById<View>(R.id.tvSubmittedAt)?.visibility = View.GONE
            holder.itemView.findViewById<View>(R.id.tvDeadline)?.visibility = View.GONE

            holder.tvMark.visibility = View.VISIBLE
            holder.pbMark.visibility = View.VISIBLE

            holder.tvMark.text = "${item.mark}% (${percentToPoints(item.mark)} pts)"
            holder.pbMark.max = 100
            holder.pbMark.progress = item.mark
        }

        override fun getItemCount(): Int = list.size

        private fun percentToPoints(mark: Int): Int {
            return when {
                mark >= 80 -> 7
                mark >= 70 -> 6
                mark >= 60 -> 5
                mark >= 50 -> 4
                mark >= 40 -> 3
                mark >= 30 -> 2
                mark >= 0 -> 1
                else -> 0
            }
        }
    }
}