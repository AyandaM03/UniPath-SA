package com.example.unipathsa

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CourseDetailActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvInstitution: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvField: TextView
    private lateinit var tvApsReq: TextView
    private lateinit var tvSubjects: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvDetail: TextView
    private lateinit var ivIcon: ImageView
    private lateinit var ivHeaderLogo: ImageView
    private lateinit var btnBack: View
    private lateinit var btnSave: MaterialButton
    private lateinit var btnApply: Button
    private lateinit var btnCalc: Button

    private var isSaved = false
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var userAps = 0
    private val userSubjects = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_detail)

        val course = intent.getSerializableExtra("COURSE_DATA") as? Course
        if (course == null) {
            Toast.makeText(this, getString(R.string.msg_course_not_found), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindViews()
        loadUserData(course)
        checkSavedStatus(course)
        displayDetails(course)
        setupListeners(course)
    }

    private fun bindViews() {
        tvTitle = findViewById(R.id.tvCourseTitle)
        tvInstitution = findViewById(R.id.tvInstitutionTitle)
        tvDescription = findViewById(R.id.tvDescription)
        tvDuration = findViewById(R.id.tvDuration)
        tvField = findViewById(R.id.tvFieldDetail)
        tvApsReq = findViewById(R.id.tvApsRequirement)
        tvSubjects = findViewById(R.id.tvSubjectRequirements)
        tvStatus = findViewById(R.id.tvMatchStatus)
        tvDetail = findViewById(R.id.tvMatchDetail)
        ivIcon = findViewById(R.id.ivStatusIcon)
        ivHeaderLogo = findViewById(R.id.ivCourseUniHeaderLogo)
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSaveCourse)
        btnApply = findViewById(R.id.btnApplyInfo)
        btnCalc = findViewById(R.id.btnOpenCalc)
    }

    private fun loadUserData(course: Course) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    userAps = (doc.getLong("aps") ?: 0L).toInt()
                }
                updateMatchStatus(course)
            }

        db.collection("users").document(uid).collection("subjects").get()
            .addOnSuccessListener { snapshot ->
                userSubjects.clear()
                for (doc in snapshot.documents) {
                    val name = doc.getString("subjectName") ?: doc.id
                    val pct = (doc.getLong("percentage") ?: 0L).toInt()
                    userSubjects[name] = pct
                }
                updateMatchStatus(course)
            }
    }

    private fun checkSavedStatus(course: Course) {
        BookmarkHelper.isSaved(course.id) { saved ->
            isSaved = saved
            updateSaveButtonUI()
        }
    }

    private fun updateSaveButtonUI() {
        btnSave.text = if (isSaved) getString(R.string.saved) else getString(R.string.save)
        btnSave.setIconResource(if (isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline)
    }

    private fun displayDetails(course: Course) {
        tvTitle.text = course.name
        tvInstitution.text = course.institution
        tvDescription.text = if (course.description.isNotEmpty()) course.description else "Comprehensive programme offered at ${course.institution}."
        tvDuration.text = if (course.duration.isNotBlank()) course.duration else getString(R.string.default_duration_3_years)
        tvField.text = if (course.field.isNotEmpty()) course.field else getString(R.string.default_general_category)
        tvApsReq.text = if (course.apsRequirement > 0) course.apsRequirement.toString() else "N/A"

        val instKey = course.institution.lowercase().trim()
        if (instKey.isNotEmpty()) {
            db.collection("institutions").get().addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        try {
                            val uni = University.fromFirestore(doc)
                            val uName = uni.name.lowercase().trim()
                            if (instKey.contains(uName) || uName.contains(instKey) || (uni.id.isNotEmpty() && instKey.contains(uni.id.lowercase()))) {
                                val logo = uni.getBestLogoUrl()
                                if (logo.isNotBlank()) {
                                    Glide.with(this@CourseDetailActivity)
                                        .load(logo)
                                        .placeholder(R.drawable.unipath_logo)
                                        .error(R.drawable.unipath_logo)
                                        .into(ivHeaderLogo)
                                }
                                break
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        if (course.requiredSubjects.isNotEmpty()) {
            val sb = StringBuilder()
            for (req in course.requiredSubjects) {
                sb.append("• ${req.subjectName}: ${req.minPercentage}%\n")
            }
            tvSubjects.text = sb.toString().trim()
        } else {
            tvSubjects.text = getString(R.string.standard_matric_pass_msg)
        }

        updateMatchStatus(course)
    }

    private fun updateMatchStatus(course: Course) {
        if (course.apsRequirement == 0) {
            tvStatus.text = getString(R.string.status_eligible)
            tvDetail.text = getString(R.string.no_min_aps_threshold_msg)
            ivIcon.imageTintList = ColorStateList.valueOf(getColor(android.R.color.holo_green_dark))
            return
        }

        val apsGap = userAps - course.apsRequirement
        var subjectsMet = true
        var subjectsPossible = true

        for (req in course.requiredSubjects) {
            val learnerMark = userSubjects[req.subjectName]
            if (learnerMark == null || learnerMark < req.minPercentage) {
                subjectsMet = false
                if (learnerMark != null && learnerMark < req.minPercentage - 10) {
                    subjectsPossible = false
                }
            }
        }

        when {
            userAps > 0 && apsGap >= 0 && subjectsMet -> {
                tvStatus.text = getString(R.string.you_are_eligible)
                tvDetail.text = getString(R.string.eligible_aps_and_subjects_msg, userAps)
                ivIcon.imageTintList = ColorStateList.valueOf(getColor(android.R.color.holo_green_dark))
            }
            userAps > 0 && apsGap >= -2 && subjectsPossible -> {
                tvStatus.text = getString(R.string.status_possibly_eligible)
                tvDetail.text = getString(R.string.possibly_eligible_msg, userAps, course.apsRequirement)
                ivIcon.imageTintList = ColorStateList.valueOf(getColor(android.R.color.holo_orange_dark))
            }
            userAps > 0 -> {
                tvStatus.text = getString(R.string.status_not_eligible)
                tvDetail.text = getString(R.string.not_eligible_msg, userAps, course.apsRequirement)
                ivIcon.imageTintList = ColorStateList.valueOf(getColor(android.R.color.holo_red_dark))
            }
            else -> {
                tvStatus.text = getString(R.string.status_check_aps_score)
                tvDetail.text = getString(R.string.check_aps_score_sub)
                ivIcon.imageTintList = ColorStateList.valueOf(getColor(android.R.color.holo_blue_dark))
            }
        }
    }

    private fun setupListeners(course: Course) {
        btnBack.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val itemDataMap = mapOf<String, Any>(
                "id" to course.id,
                "title" to course.name,
                "name" to course.name,
                "subtitle" to course.institution,
                "category" to course.field,
                "type" to "course"
            )

            BookmarkHelper.toggleBookmark(
                activity = this,
                anchorView = btnSave,
                bookmarkImageView = null,
                itemId = course.id,
                itemType = "course",
                itemData = itemDataMap
            ) { newSavedState ->
                isSaved = newSavedState
                updateSaveButtonUI()
            }
        }

        btnApply.setOnClickListener {
            val intent = Intent(this, ApplicationsActivity::class.java)
            startActivity(intent)
        }

        btnCalc.setOnClickListener {
            startActivity(Intent(this, ApsCalculatorActivity::class.java))
        }
    }
}