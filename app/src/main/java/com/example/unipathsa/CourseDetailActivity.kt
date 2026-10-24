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
import com.google.firebase.firestore.SetOptions

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
            Toast.makeText(this, "Course data not found", Toast.LENGTH_SHORT).show()
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
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("savedItems").document(course.id).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    isSaved = true
                    updateSaveButtonUI()
                }
            }
    }

    private fun updateSaveButtonUI() {
        btnSave.text = if (isSaved) "Saved" else "Save"
        btnSave.setIconResource(if (isSaved) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
    }

    private fun displayDetails(course: Course) {
        tvTitle.text = course.name
        tvInstitution.text = course.institution
        tvDescription.text = if (course.description.isNotEmpty()) course.description else "Comprehensive programme offered at ${course.institution}."
        tvDuration.text = course.duration
        tvField.text = if (course.field.isNotEmpty()) course.field else "General"
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
            tvSubjects.text = "Standard Matric pass requirements apply."
        }

        updateMatchStatus(course)
    }

    private fun updateMatchStatus(course: Course) {
        if (course.apsRequirement == 0) {
            tvStatus.text = "Eligible"
            tvDetail.text = "No minimum APS threshold specified."
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
                tvStatus.text = "You are Eligible"
                tvDetail.text = "You meet the APS (${userAps}) and subject requirements."
                ivIcon.imageTintList = ColorStateList.valueOf(getColor(android.R.color.holo_green_dark))
            }
            userAps > 0 && apsGap >= -2 && subjectsPossible -> {
                tvStatus.text = "Possibly Eligible"
                tvDetail.text = "Your APS is ${userAps}. You are close to the requirement of ${course.apsRequirement}."
                ivIcon.imageTintList = ColorStateList.valueOf(getColor(android.R.color.holo_orange_dark))
            }
            userAps > 0 -> {
                tvStatus.text = "Not Eligible"
                tvDetail.text = "Your current APS is ${userAps}. Requirement is ${course.apsRequirement}."
                ivIcon.imageTintList = ColorStateList.valueOf(getColor(android.R.color.holo_red_dark))
            }
            else -> {
                tvStatus.text = "Check APS Score"
                tvDetail.text = "Add your subject marks in the APS Calculator to check eligibility."
                ivIcon.imageTintList = ColorStateList.valueOf(getColor(android.R.color.holo_blue_dark))
            }
        }
    }

    private fun setupListeners(course: Course) {
        btnBack.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "Please log in to save courses", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val itemRef = db.collection("users").document(uid).collection("savedItems").document(course.id)

            if (isSaved) {
                itemRef.delete().addOnSuccessListener {
                    isSaved = false
                    updateSaveButtonUI()
                    Toast.makeText(this, "Course removed from saved items.", Toast.LENGTH_SHORT).show()
                }
            } else {
                val savedData = hashMapOf(
                    "id" to course.id,
                    "itemType" to "course",
                    "title" to course.name,
                    "subtitle" to course.institution,
                    "details" to "APS: ${course.apsRequirement} • ${course.field}",
                    "savedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                itemRef.set(savedData, SetOptions.merge()).addOnSuccessListener {
                    isSaved = true
                    updateSaveButtonUI()
                    Toast.makeText(this, "Course saved to your items!", Toast.LENGTH_SHORT).show()
                }
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