package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class ApplicationsActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "ApplicationsActivity"
    }

    private lateinit var tvTotalCount: TextView
    private lateinit var tvAcceptedCount: TextView
    private lateinit var tvInProgressCount: TextView
    private lateinit var rvApplications: RecyclerView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var fabAdd: FloatingActionButton

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val applicationList = mutableListOf<ApplicationModel>()
    private lateinit var adapter: ApplicationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applications)
        Log.d(TAG, "onCreate initialized")

        NavigationHelper.setupDrawer(this, R.id.navApplications)

        bindViews()
        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: loading applications from Firestore")
        loadApplicationsFromFirestore()
    }

    // Connect XML views to activity variables
    private fun bindViews() {
        tvTotalCount = findViewById(R.id.tvTotalCount)
        tvAcceptedCount = findViewById(R.id.tvAcceptedCount)
        tvInProgressCount = findViewById(R.id.tvInProgressCount)
        rvApplications = findViewById(R.id.rvApplications)
        llEmptyState = findViewById(R.id.llEmptyState)
        fabAdd = findViewById(R.id.fabAddApplication)
    }

    // Setup RecyclerView with LayoutManager and Adapter
    private fun setupRecyclerView() {
        adapter = ApplicationAdapter(applicationList) { selectedApp ->
            val intent = Intent(this, ApplicationDetailActivity::class.java)
            intent.putExtra("APP_ID", selectedApp.id)
            startActivity(intent)
        }
        rvApplications.layoutManager = LinearLayoutManager(this)
        rvApplications.adapter = adapter
    }

    // Attach click listeners to buttons
    private fun setupListeners() {
        fabAdd.setOnClickListener {
            showAddApplicationDialog()
        }

        findViewById<Button>(R.id.btnAddApplicationHeader)?.setOnClickListener {
            showAddApplicationDialog()
        }
    }

    // Reads user applications from Firestore path users/{uid}/applications
    private fun loadApplicationsFromFirestore() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "User not authenticated - cannot load applications")
            updateSummaryCards(0, 0, 0)
            showEmptyState(true)
            return
        }

        db.collection("users").document(uid).collection("applications")
            .get()
            .addOnSuccessListener { snapshot ->
                applicationList.clear()
                var accepted = 0
                var inProgress = 0

                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        val app = ApplicationModel.fromFirestore(doc)
                        applicationList.add(app)

                        when (app.status.lowercase(Locale.ROOT)) {
                            "accepted" -> accepted++
                            "submitted", "under review", "in progress" -> inProgress++
                        }
                    }
                    Log.d(TAG, "Loaded ${applicationList.size} applications from Firestore")
                    showEmptyState(false)
                } else {
                    Log.d(TAG, "No applications found in Firestore")
                    showEmptyState(true)
                }

                adapter.notifyDataSetChanged()
                updateSummaryCards(applicationList.size, accepted, inProgress)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching applications from Firestore", e)
                Toast.makeText(this, "Failed to load applications: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                showEmptyState(applicationList.isEmpty())
            }
    }

    // Updates summary cards at top of screen
    private fun updateSummaryCards(total: Int, accepted: Int, inProgress: Int) {
        tvTotalCount.text = total.toString()
        tvAcceptedCount.text = accepted.toString()
        tvInProgressCount.text = inProgress.toString()
    }

    // Shows/hides empty state UI
    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            llEmptyState.visibility = View.VISIBLE
            rvApplications.visibility = View.GONE
        } else {
            llEmptyState.visibility = View.GONE
            rvApplications.visibility = View.VISIBLE
        }
    }

    // Shows dialog to enter new university application details
    private fun showAddApplicationDialog() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Please log in to add applications", Toast.LENGTH_SHORT).show()
            return
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 16)
        }

        val etUni = EditText(this).apply { hint = "University (e.g. University of Johannesburg)" }
        val etCourse = EditText(this).apply { hint = "Course Name (e.g. BSc Computer Science)" }
        val etDeadline = EditText(this).apply {
            hint = "Closing Deadline (e.g. 30 Sep 2025)"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        container.addView(etUni)
        container.addView(etCourse)
        container.addView(etDeadline)

        AlertDialog.Builder(this)
            .setTitle("Add Application")
            .setView(container)
            .setPositiveButton("Add") { _, _ ->
                val uni = etUni.text.toString().trim()
                val course = etCourse.text.toString().trim()
                var deadline = etDeadline.text.toString().trim()

                if (uni.isEmpty() || course.isEmpty()) {
                    Toast.makeText(this, "Please enter university and course", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (deadline.isEmpty()) {
                    deadline = "30 Sep 2025"
                }

                saveApplicationToFirestore(uid, uni, course, deadline)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Writes new application document to Firestore users/{uid}/applications
    private fun saveApplicationToFirestore(uid: String, uni: String, course: String, deadline: String) {
        val appData = hashMapOf(
            "university" to uni,
            "course" to course,
            "status" to "Submitted",
            "submittedAt" to FieldValue.serverTimestamp(),
            "deadline" to deadline,
            "docsUploaded" to 0,
            "docsTotal" to 4,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(uid).collection("applications")
            .add(appData)
            .addOnSuccessListener { ref ->
                Log.d(TAG, "Application added successfully with ID: ${ref.id}")
                Toast.makeText(this, "Application added!", Toast.LENGTH_SHORT).show()
                loadApplicationsFromFirestore()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to add application", e)
                Toast.makeText(this, "Error adding application: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    // RecyclerView Adapter for Application items
    private inner class ApplicationAdapter(
        private val list: List<ApplicationModel>,
        private val onItemClick: (ApplicationModel) -> Unit
    ) : RecyclerView.Adapter<ApplicationAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvUniversity: TextView = view.findViewById(R.id.tvUniversity)
            val tvCourse: TextView = view.findViewById(R.id.tvCourse)
            val tvStatusChip: TextView = view.findViewById(R.id.tvStatusChip)
            val tvDocsCount: TextView = view.findViewById(R.id.tvDocsCount)
            val pbDocsProgress: ProgressBar = view.findViewById(R.id.pbDocsProgress)
            val tvSubmittedAt: TextView = view.findViewById(R.id.tvSubmittedAt)
            val tvDeadline: TextView = view.findViewById(R.id.tvDeadline)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_application, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvUniversity.text = item.university
            holder.tvCourse.text = item.course

            // Configure status chip colors based on status
            holder.tvStatusChip.text = item.status
            when (item.status.lowercase(Locale.ROOT)) {
                "accepted" -> {
                    holder.tvStatusChip.setTextColor(getColor(R.color.status_accepted_fg))
                    holder.tvStatusChip.backgroundTintList = getColorStateList(R.color.status_accepted_bg)
                }
                "under review", "in review" -> {
                    holder.tvStatusChip.setTextColor(getColor(R.color.status_review_fg))
                    holder.tvStatusChip.backgroundTintList = getColorStateList(R.color.status_review_bg)
                }
                "rejected" -> {
                    holder.tvStatusChip.setTextColor(getColor(R.color.status_rejected_fg))
                    holder.tvStatusChip.backgroundTintList = getColorStateList(R.color.status_rejected_bg)
                }
                else -> { // Submitted
                    holder.tvStatusChip.setTextColor(getColor(R.color.status_submitted_fg))
                    holder.tvStatusChip.backgroundTintList = getColorStateList(R.color.status_submitted_bg)
                }
            }

            // Documents progress
            val totalDocs = if (item.docsTotal > 0) item.docsTotal else 4
            holder.tvDocsCount.text = "Documents ${item.docsUploaded}/$totalDocs"
            holder.pbDocsProgress.max = totalDocs
            holder.pbDocsProgress.progress = item.docsUploaded

            // Dates formatting
            val submittedStr = if (item.submittedAt != null) {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                "Submitted: " + sdf.format(item.submittedAt.toDate())
            } else {
                "Submitted: Recent"
            }
            holder.tvSubmittedAt.text = submittedStr
            holder.tvDeadline.text = "Deadline: ${item.deadline.ifBlank { "N/A" }}"

            holder.itemView.setOnClickListener { onItemClick(item) }
        }

        override fun getItemCount(): Int = list.size
    }
}