package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class ApplicationDetailActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "AppDetailActivity"
    }

    private lateinit var tvUni: TextView
    private lateinit var tvCourse: TextView
    private lateinit var tvStatusChip: TextView
    private lateinit var tvSubmittedAt: TextView
    private lateinit var tvDeadline: TextView

    private lateinit var ivStep1Icon: ImageView
    private lateinit var ivStep2Icon: ImageView
    private lateinit var ivStep3Icon: ImageView
    private lateinit var ivStep4Icon: ImageView

    private lateinit var btnChangeStatus: MaterialButton
    private lateinit var btnManageDocs: MaterialButton
    private lateinit var btnDeleteApp: MaterialButton

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var appId: String? = null
    private var currentApplication: ApplicationModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application_detail)
        Log.d(TAG, "onCreate started")

        appId = intent.getStringExtra("APP_ID")
        if (appId.isNullOrEmpty()) {
            Log.e(TAG, "No APP_ID passed to ApplicationDetailActivity")
            Toast.makeText(this, "Application error", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindViews()
        setupListeners()
        loadApplicationDetails()
    }

    // Connect views to XML
    private fun bindViews() {
        tvUni = findViewById(R.id.tvUniversityDetail)
        tvCourse = findViewById(R.id.tvCourseDetail)
        tvStatusChip = findViewById(R.id.tvStatusChip)
        tvSubmittedAt = findViewById(R.id.tvDetailSubmittedAt)
        tvDeadline = findViewById(R.id.tvDetailDeadline)

        ivStep1Icon = findViewById(R.id.ivStep1Icon)
        ivStep2Icon = findViewById(R.id.ivStep2Icon)
        ivStep3Icon = findViewById(R.id.ivStep3Icon)
        ivStep4Icon = findViewById(R.id.ivStep4Icon)

        btnChangeStatus = findViewById(R.id.btnChangeStatus)
        btnManageDocs = findViewById(R.id.btnManageDocs)
        btnDeleteApp = findViewById(R.id.btnDeleteApp)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
    }

    // Setup click listeners
    private fun setupListeners() {
        btnChangeStatus.setOnClickListener { showChangeStatusDialog() }
        btnManageDocs.setOnClickListener {
            startActivity(Intent(this, DocumentsActivity::class.java))
        }
        btnDeleteApp.setOnClickListener { confirmDeleteApplication() }
    }

    // Load application record from users/{uid}/applications/{appId}
    private fun loadApplicationDetails() {
        val uid = auth.currentUser?.uid ?: return
        val id = appId ?: return

        db.collection("users").document(uid).collection("applications").document(id)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    currentApplication = ApplicationModel.fromFirestore(doc)
                    displayDetails(currentApplication!!)
                    Log.d(TAG, "Loaded application details for ${currentApplication?.university}")
                } else {
                    Log.w(TAG, "Application document does not exist for ID: $id")
                    Toast.makeText(this, "Application not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load application details", e)
                Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    // Display fields and configure step icons
    private fun displayDetails(app: ApplicationModel) {
        tvUni.text = app.university
        tvCourse.text = app.course

        // Configure status chip
        tvStatusChip.text = app.status
        when (app.status.lowercase(Locale.ROOT)) {
            "accepted" -> {
                tvStatusChip.setTextColor(getColor(R.color.status_accepted_fg))
                tvStatusChip.backgroundTintList = getColorStateList(R.color.status_accepted_bg)
            }
            "under review", "in review" -> {
                tvStatusChip.setTextColor(getColor(R.color.status_review_fg))
                tvStatusChip.backgroundTintList = getColorStateList(R.color.status_review_bg)
            }
            "rejected" -> {
                tvStatusChip.setTextColor(getColor(R.color.status_rejected_fg))
                tvStatusChip.backgroundTintList = getColorStateList(R.color.status_rejected_bg)
            }
            else -> {
                tvStatusChip.setTextColor(getColor(R.color.status_submitted_fg))
                tvStatusChip.backgroundTintList = getColorStateList(R.color.status_submitted_bg)
            }
        }

        // Configure date fields
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val submittedStr = if (app.submittedAt != null) sdf.format(app.submittedAt.toDate()) else "Recent"
        tvSubmittedAt.text = "Submitted Date: $submittedStr"
        tvDeadline.text = "Closing Deadline: ${app.deadline.ifBlank { "N/A" }}"

        // Progress step indicators
        val activeTint = getColorStateList(R.color.primary)
        val inactiveTint = getColorStateList(R.color.outline)
        val checkIcon = android.R.drawable.checkbox_on_background
        val emptyIcon = android.R.drawable.checkbox_off_background

        ivStep1Icon.setImageResource(checkIcon)
        ivStep1Icon.imageTintList = activeTint

        val statusLower = app.status.lowercase(Locale.ROOT)
        val isDocsVerified = app.docsUploaded >= app.docsTotal || statusLower != "submitted"
        ivStep2Icon.setImageResource(if (isDocsVerified) checkIcon else emptyIcon)
        ivStep2Icon.imageTintList = if (isDocsVerified) activeTint else inactiveTint

        val isReviewed = statusLower == "under review" || statusLower == "accepted" || statusLower == "rejected"
        ivStep3Icon.setImageResource(if (isReviewed) checkIcon else emptyIcon)
        ivStep3Icon.imageTintList = if (isReviewed) activeTint else inactiveTint

        val isDecisionMade = statusLower == "accepted" || statusLower == "rejected"
        ivStep4Icon.setImageResource(if (isDecisionMade) checkIcon else emptyIcon)
        ivStep4Icon.imageTintList = if (isDecisionMade) activeTint else inactiveTint
    }

    // Dialog to update status
    private fun showChangeStatusDialog() {
        val statuses = arrayOf("Submitted", "Under Review", "Accepted", "Rejected")
        AlertDialog.Builder(this)
            .setTitle("Select New Status")
            .setItems(statuses) { _, which ->
                val newStatus = statuses[which]
                updateStatusInFirestore(newStatus)
            }
            .show()
    }

    // Updates status field in Firestore document
    private fun updateStatusInFirestore(newStatus: String) {
        val uid = auth.currentUser?.uid ?: return
        val id = appId ?: return

        db.collection("users").document(uid).collection("applications").document(id)
            .update("status", newStatus)
            .addOnSuccessListener {
                Log.d(TAG, "Updated status to $newStatus for $id")
                Toast.makeText(this, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                loadApplicationDetails()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update status", e)
                Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show()
            }
    }

    // Confirm deletion of application
    private fun confirmDeleteApplication() {
        AlertDialog.Builder(this)
            .setTitle("Delete Application")
            .setMessage("Are you sure you want to delete this application? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteApplicationFromFirestore()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Delete application from Firestore
    private fun deleteApplicationFromFirestore() {
        val uid = auth.currentUser?.uid ?: return
        val id = appId ?: return

        db.collection("users").document(uid).collection("applications").document(id)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Application $id deleted successfully")
                Toast.makeText(this, "Application deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error deleting application $id", e)
                Toast.makeText(this, "Error deleting application", Toast.LENGTH_SHORT).show()
            }
    }
}