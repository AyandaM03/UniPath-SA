package com.example.unipathsa

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApplyActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "ApplyActivity"
    }

    private lateinit var tvHeaderTitle: TextView
    private lateinit var actvCourse: AutoCompleteTextView
    private lateinit var llCourseDetails: LinearLayout
    private lateinit var tvCourseTitle: TextView
    private lateinit var tvCourseMeta: TextView

    // Application Summary Views
    private lateinit var tvPersonalSummary: TextView
    private lateinit var btnCopyPersonal: MaterialButton
    private lateinit var btnCompleteProfile: MaterialButton

    private lateinit var tvApsSummary: TextView
    private lateinit var tvSubjectsSummary: TextView
    private lateinit var tvApsBadge: TextView
    private lateinit var btnCopyResults: MaterialButton
    private lateinit var btnCompleteAps: MaterialButton

    private lateinit var tvDocsSummary: TextView
    private lateinit var btnManageDocs: MaterialButton

    private lateinit var tvFeeSummary: TextView
    private lateinit var btnCopyFee: MaterialButton

    private lateinit var switchSubmitted: SwitchMaterial
    private lateinit var tvDuplicateWarning: TextView
    private lateinit var btnCreateTrack: MaterialButton
    private lateinit var pbExportProgress: ProgressBar

    // Application Pack Tools Actions
    private lateinit var llPostActions: LinearLayout
    private lateinit var btnExportPdfPack: MaterialButton
    private lateinit var btnShareDocs: MaterialButton
    private lateinit var btnOpenWeb: MaterialButton
    private lateinit var btnViewTracker: MaterialButton

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var currentUniversity: University? = null
    private val institutionCourses = mutableListOf<Course>()
    private var selectedCourse: Course? = null

    // User Profile Snapshot Data
    private var userFullName = ""
    private var userEmail = ""
    private var userGrade = "Grade 12"
    private var userSchool = ""
    private var userAps = 0
    private val userSubjects = mutableListOf<Map<String, Any>>()
    private val userDocs = mutableListOf<Map<String, String>>()
    private var docsUploadedCount = 0

    private var isDuplicate = false
    private var applicationFeeStr = "Free to Apply"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apply)
        Log.d(TAG, "onCreate initialized")

        currentUniversity = intent.getSerializableExtra("UNIVERSITY_DATA") as? University

        if (currentUniversity == null) {
            Toast.makeText(this, "Institution details not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindViews()
        setupListeners()
        loadLearnerSummaryAndCourses()
    }

    private fun bindViews() {
        tvHeaderTitle = findViewById(R.id.tvApplyHeaderTitle)
        actvCourse = findViewById(R.id.actvCourseApply)
        llCourseDetails = findViewById(R.id.llSelectedCourseDetails)
        tvCourseTitle = findViewById(R.id.tvApplyCourseTitle)
        tvCourseMeta = findViewById(R.id.tvApplyCourseMeta)

        tvPersonalSummary = findViewById(R.id.tvPersonalSummary)
        btnCopyPersonal = findViewById(R.id.btnCopyPersonal)
        btnCompleteProfile = findViewById(R.id.btnCompleteProfile)

        tvApsSummary = findViewById(R.id.tvApsSummary)
        tvSubjectsSummary = findViewById(R.id.tvSubjectsSummary)
        tvApsBadge = findViewById(R.id.tvApplyApsBadge)
        btnCopyResults = findViewById(R.id.btnCopyResults)
        btnCompleteAps = findViewById(R.id.btnCompleteAps)

        tvDocsSummary = findViewById(R.id.tvDocsSummary)
        btnManageDocs = findViewById(R.id.btnManageDocsApply)

        tvFeeSummary = findViewById(R.id.tvFeeSummary)
        btnCopyFee = findViewById(R.id.btnCopyFee)

        switchSubmitted = findViewById(R.id.switchAlreadySubmitted)
        tvDuplicateWarning = findViewById(R.id.tvDuplicateWarning)
        btnCreateTrack = findViewById(R.id.btnCreateTrackApplication)
        pbExportProgress = findViewById(R.id.pbExportProgress)

        llPostActions = findViewById(R.id.llPostCreationActions)
        btnExportPdfPack = findViewById(R.id.btnExportPdfPack)
        btnShareDocs = findViewById(R.id.btnShareDocs)
        btnOpenWeb = findViewById(R.id.btnOpenOfficialWebPage)
        btnViewTracker = findViewById(R.id.btnViewInTracker)

        tvHeaderTitle.text = "Apply: ${currentUniversity?.name}"

        applicationFeeStr = if (currentUniversity?.freeToApply == true) "Free to Apply" else "R100"
        tvFeeSummary.text = "Application Fee: $applicationFeeStr"
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnBackApply)?.setOnClickListener { finish() }

        btnCopyPersonal.setOnClickListener { copyToClipboard("Personal Details", tvPersonalSummary.text.toString()) }
        btnCopyResults.setOnClickListener { copyToClipboard("Subject Results", "${tvApsSummary.text}\n${tvSubjectsSummary.text}") }
        btnCopyFee.setOnClickListener { copyToClipboard("Application Fee", tvFeeSummary.text.toString()) }

        btnCompleteProfile.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        btnCompleteAps.setOnClickListener { startActivity(Intent(this, ApsCalculatorActivity::class.java)) }
        btnManageDocs.setOnClickListener { startActivity(Intent(this, DocumentsActivity::class.java)) }

        btnCreateTrack.setOnClickListener { createApplicationSnapshot() }

        btnExportPdfPack.setOnClickListener { exportPdfApplicationPack() }
        btnShareDocs.setOnClickListener { confirmAndShareDocuments() }

        btnOpenWeb.setOnClickListener { openOfficialWebsite() }
        btnViewTracker.setOnClickListener {
            startActivity(Intent(this, ApplicationsActivity::class.java))
            finish()
        }
    }

    // Loads complete learner profile, subjects, documents, and courses for this institution
    private fun loadLearnerSummaryAndCourses() {
        val uid = auth.currentUser?.uid ?: return
        val uniName = currentUniversity?.name ?: ""

        // 1. Load User Document
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                userFullName = doc.getString("fullName") ?: doc.getString("name") ?: auth.currentUser?.displayName ?: "Learner Name"
                userEmail = auth.currentUser?.email ?: ""
                userGrade = doc.getString("grade") ?: "Grade 12"
                userSchool = doc.getString("school") ?: doc.getString("highSchool") ?: ""
                userAps = (doc.getLong("aps") ?: 0L).toInt()

                tvPersonalSummary.text = "Name: $userFullName\nEmail: $userEmail\nGrade: $userGrade • School: ${userSchool.ifBlank { "Not specified" }}"

                if (userFullName.isBlank() || userSchool.isBlank()) {
                    btnCompleteProfile.visibility = View.VISIBLE
                } else {
                    btnCompleteProfile.visibility = View.GONE
                }

                updateApsSummary()
            }
        }

        // 2. Load User Subjects
        db.collection("users").document(uid).collection("subjects").get().addOnSuccessListener { snap ->
            userSubjects.clear()
            val sb = StringBuilder("Subjects: ")
            if (snap != null && !snap.isEmpty) {
                for (d in snap.documents) {
                    val subName = d.getString("subjectName") ?: d.id
                    val pct = (d.getLong("percentage") ?: 0L).toInt()
                    userSubjects.add(mapOf("subjectName" to subName, "percentage" to pct))
                    sb.append("$subName $pct%, ")
                }
                tvSubjectsSummary.text = sb.toString().removeSuffix(", ")
            } else {
                tvSubjectsSummary.text = "No subjects added yet."
                btnCompleteAps.visibility = View.VISIBLE
            }
        }

        // 3. Load User Documents
        db.collection("users").document(uid).collection("documents").get().addOnSuccessListener { snap ->
            userDocs.clear()
            docsUploadedCount = snap?.size() ?: 0
            val sb = StringBuilder("Documents: ")
            if (snap != null && !snap.isEmpty) {
                for (d in snap.documents) {
                    val title = d.getString("title") ?: d.id
                    val fileUrl = d.getString("fileUrl") ?: ""
                    userDocs.add(mapOf("title" to title, "fileUrl" to fileUrl, "docName" to title))
                    sb.append("$title ✔, ")
                }
                tvDocsSummary.text = sb.toString().removeSuffix(", ")
            } else {
                tvDocsSummary.text = "No documents uploaded yet (0 of 4)."
            }
        }

        // 4. Load Courses for this Institution
        db.collection("courses").get().addOnSuccessListener { snapshot ->
            institutionCourses.clear()
            val courseNames = mutableListOf<String>()

            if (snapshot != null && !snapshot.isEmpty) {
                for (d in snapshot.documents) {
                    val c = Course.fromFirestore(d)
                    if (c.institution.contains(uniName, ignoreCase = true) || uniName.contains(c.institution, ignoreCase = true)) {
                        institutionCourses.add(c)
                        courseNames.add("${c.name} (APS ${c.apsRequirement})")
                    }
                }
            }

            if (institutionCourses.isEmpty()) {
                for (d in snapshot?.documents ?: emptyList()) {
                    val c = Course.fromFirestore(d)
                    institutionCourses.add(c)
                    courseNames.add("${c.name} - ${c.institution}")
                }
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, courseNames)
            actvCourse.setAdapter(adapter)

            actvCourse.setOnItemClickListener { _, _, position, _ ->
                if (position in institutionCourses.indices) {
                    onCourseSelected(institutionCourses[position])
                }
            }
        }
    }

    private fun onCourseSelected(course: Course) {
        selectedCourse = course
        llCourseDetails.visibility = View.VISIBLE
        tvCourseTitle.text = course.name
        tvCourseMeta.text = "Duration: ${course.duration} • Required APS: ${course.apsRequirement}"

        updateApsSummary()
        checkDuplicateApplication(course)
    }

    private fun updateApsSummary() {
        val course = selectedCourse
        val reqAps = course?.apsRequirement ?: 0

        tvApsSummary.text = "Your APS: ${if (userAps > 0) userAps else "Not calculated"} | Required: ${if (reqAps > 0) reqAps else "N/A"}"

        if (userAps > 0 && reqAps > 0) {
            btnCompleteAps.visibility = View.GONE
            if (userAps >= reqAps) {
                tvApsBadge.text = "✔ You qualify"
                tvApsBadge.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.status_accepted_bg))
                tvApsBadge.setTextColor(ContextCompat.getColor(this, R.color.status_accepted_fg))
            } else {
                val diff = reqAps - userAps
                tvApsBadge.text = "⚠️ $diff points short"
                tvApsBadge.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.status_review_bg))
                tvApsBadge.setTextColor(ContextCompat.getColor(this, R.color.status_review_fg))
            }
        } else {
            tvApsBadge.text = "ℹ️ APS Check Needed"
            tvApsBadge.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.status_review_bg))
            tvApsBadge.setTextColor(ContextCompat.getColor(this, R.color.status_review_fg))
            btnCompleteAps.visibility = View.VISIBLE
        }
    }

    private fun checkDuplicateApplication(course: Course) {
        val uid = auth.currentUser?.uid ?: return
        val uniName = currentUniversity?.name ?: ""

        db.collection("users").document(uid).collection("applications")
            .whereEqualTo("university", uniName)
            .get()
            .addOnSuccessListener { snap ->
                isDuplicate = false
                if (snap != null) {
                    for (doc in snap.documents) {
                        val existingCourse = doc.getString("course") ?: ""
                        if (existingCourse.equals(course.name, ignoreCase = true)) {
                            isDuplicate = true
                            break
                        }
                    }
                }

                if (isDuplicate) {
                    tvDuplicateWarning.visibility = View.VISIBLE
                    btnCreateTrack.text = "View Existing Application in Tracker"
                } else {
                    tvDuplicateWarning.visibility = View.GONE
                    btnCreateTrack.text = "Confirm & Save Application Snapshot"
                }
            }
    }

    // Creates application record in users/{uid}/applications with full snapshot data
    private fun createApplicationSnapshot() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            return
        }

        val course = selectedCourse
        if (course == null) {
            Toast.makeText(this, "Please choose a course first", Toast.LENGTH_SHORT).show()
            return
        }

        if (isDuplicate) {
            startActivity(Intent(this, ApplicationsActivity::class.java))
            finish()
            return
        }

        val statusStr = if (switchSubmitted.isChecked) "Submitted" else "Draft"
        val uniName = currentUniversity?.name ?: ""

        val appData = hashMapOf(
            "university" to uniName,
            "course" to course.name,
            "status" to statusStr,
            "docsUploaded" to docsUploadedCount,
            "docsTotal" to 4,
            "deadline" to "30 Sep 2025",
            "createdAt" to FieldValue.serverTimestamp(),
            "submittedAt" to FieldValue.serverTimestamp(),
            "officialWebsiteUrl" to (currentUniversity?.websiteUrl ?: ""),
            "courseId" to course.id,
            "institutionId" to (currentUniversity?.id ?: ""),
            "apsAtApplication" to userAps,
            "subjectsSnapshot" to userSubjects,
            "documentsSnapshot" to userDocs,
            "applicationFee" to applicationFeeStr
        )

        btnCreateTrack.isEnabled = false

        db.collection("users").document(uid).collection("applications")
            .add(appData)
            .addOnSuccessListener { docRef ->
                Log.d(TAG, "Successfully created application snapshot: ${docRef.id}")
                Toast.makeText(this, "Application Snapshot Saved!", Toast.LENGTH_SHORT).show()

                btnCreateTrack.visibility = View.GONE
                llPostActions.visibility = View.VISIBLE
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving application snapshot", e)
                btnCreateTrack.isEnabled = true
                Toast.makeText(this, "Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    // Export PDF Application Pack using Android PdfDocument
    private fun exportPdfApplicationPack() {
        pbExportProgress.visibility = View.VISIBLE
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 page size
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            paint.color = Color.BLACK
            paint.textSize = 12f

            val titlePaint = Paint().apply {
                color = Color.rgb(11, 31, 77) // Navy dark
                textSize = 20f
                isFakeBoldText = true
            }

            val headerPaint = Paint().apply {
                color = Color.rgb(26, 35, 126) // Primary blue
                textSize = 14f
                isFakeBoldText = true
            }

            var y = 40f

            canvas.drawText("UniPath SA - Official Application Pack", 40f, y, titlePaint)
            y += 20f
            val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
            canvas.drawText("Generated: $dateStr", 40f, y, paint)
            y += 30f

            canvas.drawText("1. LEARNER PERSONAL DETAILS", 40f, y, headerPaint)
            y += 20f
            canvas.drawText("Full Name: $userFullName", 50f, y, paint)
            y += 16f
            canvas.drawText("Email: $userEmail", 50f, y, paint)
            y += 16f
            canvas.drawText("Grade: $userGrade | High School: ${userSchool.ifBlank { "N/A" }}", 50f, y, paint)
            y += 30f

            canvas.drawText("2. APPLICATION TARGET", 40f, y, headerPaint)
            y += 20f
            canvas.drawText("Institution: ${currentUniversity?.name}", 50f, y, paint)
            y += 16f
            canvas.drawText("Course: ${selectedCourse?.name ?: "N/A"}", 50f, y, paint)
            y += 16f
            canvas.drawText("Learner APS: $userAps | Course Required APS: ${selectedCourse?.apsRequirement ?: 0}", 50f, y, paint)
            y += 16f
            canvas.drawText("Application Fee: $applicationFeeStr", 50f, y, paint)
            y += 30f

            canvas.drawText("3. SUBJECT RESULTS & MARKS", 40f, y, headerPaint)
            y += 20f
            if (userSubjects.isNotEmpty()) {
                for (sub in userSubjects) {
                    val sName = sub["subjectName"]?.toString() ?: ""
                    val sPct = sub["percentage"]?.toString() ?: "0"
                    canvas.drawText("• $sName: $sPct%", 50f, y, paint)
                    y += 16f
                }
            } else {
                canvas.drawText("No subject marks recorded.", 50f, y, paint)
                y += 16f
            }
            y += 14f

            canvas.drawText("4. DOCUMENTS CHECKLIST", 40f, y, headerPaint)
            y += 20f
            if (userDocs.isNotEmpty()) {
                for (doc in userDocs) {
                    val dTitle = doc["title"] ?: "Document"
                    canvas.drawText("• $dTitle (Uploaded ✔)", 50f, y, paint)
                    y += 16f
                }
            } else {
                canvas.drawText("No documents uploaded yet.", 50f, y, paint)
                y += 16f
            }

            pdfDocument.finishPage(page)

            val pdfFile = File(cacheDir, "Application_Pack.pdf")
            pdfDocument.writeTo(FileOutputStream(pdfFile))
            pdfDocument.close()

            pbExportProgress.visibility = View.GONE
            Log.d(TAG, "Successfully generated PDF pack: ${pdfFile.absolutePath}")

            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", pdfFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Application Pack - $userFullName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Application Pack PDF"))

        } catch (e: Exception) {
            pbExportProgress.visibility = View.GONE
            Log.e(TAG, "Error generating PDF pack", e)
            Toast.makeText(this, "Failed to generate PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // Shares uploaded documents via Android Share Sheet using FileProvider
    private fun confirmAndShareDocuments() {
        val hasSensitiveDoc = userDocs.any { (it["title"] ?: "").contains("ID", ignoreCase = true) || (it["title"] ?: "").contains("Passport", ignoreCase = true) }

        if (hasSensitiveDoc) {
            AlertDialog.Builder(this)
                .setTitle("Sensitive Document Warning")
                .setMessage("The ID Document / Passport contains sensitive personal data. Do you want to include it in the shared documents pack?")
                .setPositiveButton("Include All") { _, _ -> downloadAndShareDocs(includeSensitive = true) }
                .setNegativeButton("Exclude ID") { _, _ -> downloadAndShareDocs(includeSensitive = false) }
                .setNeutralButton("Cancel", null)
                .show()
        } else {
            downloadAndShareDocs(includeSensitive = true)
        }
    }

    private fun downloadAndShareDocs(includeSensitive: Boolean) {
        if (userDocs.isEmpty()) {
            Toast.makeText(this, "No uploaded documents found to share", Toast.LENGTH_SHORT).show()
            return
        }

        pbExportProgress.visibility = View.VISIBLE
        val sharedDocsDir = File(cacheDir, "shared_docs").apply { mkdirs() }
        val sharedUris = ArrayList<Uri>()

        var pendingDownloads = 0
        val targetDocs = userDocs.filter { doc ->
            val title = doc["title"] ?: ""
            if (!includeSensitive && (title.contains("ID", ignoreCase = true) || title.contains("Passport", ignoreCase = true))) {
                false
            } else {
                !doc["fileUrl"].isNullOrBlank()
            }
        }

        if (targetDocs.isEmpty()) {
            pbExportProgress.visibility = View.GONE
            Toast.makeText(this, "No documents available for sharing", Toast.LENGTH_SHORT).show()
            return
        }

        pendingDownloads = targetDocs.size

        for (doc in targetDocs) {
            val fileUrl = doc["fileUrl"]!!
            val title = (doc["title"] ?: "document").replace("[^a-zA-Z0-9]".toRegex(), "_")
            val localFile = File(sharedDocsDir, "$title.pdf")

            try {
                val ref = storage.getReferenceFromUrl(fileUrl)
                ref.getFile(localFile)
                    .addOnSuccessListener {
                        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", localFile)
                        sharedUris.add(uri)
                        pendingDownloads--

                        if (pendingDownloads == 0) {
                            pbExportProgress.visibility = View.GONE
                            launchDocsShareIntent(sharedUris)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error downloading doc $title", e)
                        pendingDownloads--
                        if (pendingDownloads == 0 && sharedUris.isNotEmpty()) {
                            pbExportProgress.visibility = View.GONE
                            launchDocsShareIntent(sharedUris)
                        } else if (pendingDownloads == 0) {
                            pbExportProgress.visibility = View.GONE
                            Toast.makeText(this, "Failed to download documents", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing file URL $fileUrl", e)
                pendingDownloads--
            }
        }
    }

    private fun launchDocsShareIntent(uris: ArrayList<Uri>) {
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            putExtra(Intent.EXTRA_SUBJECT, "Application Documents - $userFullName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Application Documents"))
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "$label copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
    }

    private fun openOfficialWebsite() {
        val webUrl = currentUniversity?.websiteUrl?.trim() ?: ""
        if (webUrl.isBlank()) {
            Toast.makeText(this, "Official website URL not found", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val url = if (webUrl.startsWith("http")) webUrl else "https://$webUrl"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open website", Toast.LENGTH_SHORT).show()
        }
    }
}