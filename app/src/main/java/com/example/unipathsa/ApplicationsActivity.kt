package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Filter
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

        // Single clearly commented fallback alias map for well-known university nicknames
        private val UNIVERSITY_NICKNAMES = mapOf(
            "wits" to "University of the Witwatersrand",
            "joburg" to "University of Johannesburg",
            "uj" to "University of Johannesburg",
            "uct" to "University of Cape Town",
            "cape town" to "University of Cape Town",
            "ukzn" to "University of KwaZulu-Natal",
            "natal" to "University of KwaZulu-Natal",
            "tuks" to "University of Pretoria",
            "up" to "University of Pretoria",
            "maties" to "Stellenbosch University",
            "stellies" to "Stellenbosch University",
            "nwu" to "North-West University",
            "pukke" to "North-West University",
            "ufs" to "University of the Free State",
            "kovsies" to "University of the Free State",
            "tut" to "Tshwane University of Technology",
            "unisa" to "University of South Africa"
        )
    }

    private lateinit var tvTotalCount: TextView
    private lateinit var tvAcceptedCount: TextView
    private lateinit var tvInProgressCount: TextView

    // Estimated Fees Card Views
    private lateinit var tvEstimatedFeesTotal: TextView
    private lateinit var tvEstimatedFeesBreakdown: TextView

    private lateinit var rvApplications: RecyclerView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var fabAdd: FloatingActionButton

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val applicationList = mutableListOf<ApplicationModel>()
    private val allInstitutionsList = mutableListOf<University>()
    private val allCoursesList = mutableListOf<Course>()
    private lateinit var adapter: ApplicationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applications)
        Log.d(TAG, "onCreate initialized")

        NavigationHelper.setupDrawer(this, R.id.navApplications)

        bindViews()
        setupRecyclerView()
        setupListeners()
        preloadAutocompleteData()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: loading applications from Firestore")
        loadApplicationsFromFirestore()
    }

    // Connect XML views
    private fun bindViews() {
        tvTotalCount = findViewById(R.id.tvTotalCount)
        tvAcceptedCount = findViewById(R.id.tvAcceptedCount)
        tvInProgressCount = findViewById(R.id.tvInProgressCount)

        tvEstimatedFeesTotal = findViewById(R.id.tvEstimatedFeesTotal)
        tvEstimatedFeesBreakdown = findViewById(R.id.tvEstimatedFeesBreakdown)

        rvApplications = findViewById(R.id.rvApplications)
        llEmptyState = findViewById(R.id.llEmptyState)
        fabAdd = findViewById(R.id.fabAddApplication)
    }

    // Setup RecyclerView
    private fun setupRecyclerView() {
        adapter = ApplicationAdapter(applicationList) { selectedApp ->
            val intent = Intent(this, ApplicationDetailActivity::class.java)
            intent.putExtra("APP_ID", selectedApp.id)
            startActivity(intent)
        }
        rvApplications.layoutManager = LinearLayoutManager(this)
        rvApplications.adapter = adapter
    }

    // Attach listeners
    private fun setupListeners() {
        fabAdd.setOnClickListener {
            showAddApplicationDialog()
        }

        findViewById<Button>(R.id.btnAddApplicationHeader)?.setOnClickListener {
            showAddApplicationDialog()
        }
    }

    // Preloads institutions and courses once for memory filtering & fee calculation
    private fun preloadAutocompleteData() {
        db.collection("institutions").get().addOnSuccessListener { snapshot ->
            allInstitutionsList.clear()
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    allInstitutionsList.add(University.fromFirestore(doc))
                }
            }
            Log.d(TAG, "Preloaded ${allInstitutionsList.size} institutions for fee estimation")
            calculateEstimatedFees()
        }

        db.collection("courses").get().addOnSuccessListener { snapshot ->
            allCoursesList.clear()
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    allCoursesList.add(Course.fromFirestore(doc))
                }
            }
        }
    }

    // Reads user applications from Firestore
    private fun loadApplicationsFromFirestore() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
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
                            "submitted", "under review", "in progress", "draft" -> inProgress++
                        }
                    }
                    showEmptyState(false)
                } else {
                    showEmptyState(true)
                }

                adapter.notifyDataSetChanged()
                updateSummaryCards(applicationList.size, accepted, inProgress)
                calculateEstimatedFees()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching applications", e)
                showEmptyState(applicationList.isEmpty())
            }
    }

    // Calculates estimated application fees based on application list and preloaded institutions
    private fun calculateEstimatedFees() {
        var totalFeeSum = 0
        var knownCount = 0
        var unknownCount = 0

        for (app in applicationList) {
            val matchedInst = allInstitutionsList.firstOrNull {
                it.name.contains(app.university, ignoreCase = true) || app.university.contains(it.name, ignoreCase = true)
            }

            if (matchedInst != null) {
                if (matchedInst.freeToApply || matchedInst.applicationFee == 0) {
                    knownCount++
                } else if (matchedInst.applicationFee != null && matchedInst.applicationFee!! > 0) {
                    totalFeeSum += matchedInst.applicationFee!!
                    knownCount++
                } else {
                    unknownCount++
                }
            } else {
                unknownCount++
            }
        }

        tvEstimatedFeesTotal.text = if (totalFeeSum == 0 && knownCount > 0) "Free" else "R$totalFeeSum"
        tvEstimatedFeesBreakdown.text = "${applicationList.size} applications tracked • $unknownCount unknown fee${if (unknownCount == 1) "" else "s"}"
    }

    private fun updateSummaryCards(total: Int, accepted: Int, inProgress: Int) {
        tvTotalCount.text = total.toString()
        tvAcceptedCount.text = accepted.toString()
        tvInProgressCount.text = inProgress.toString()
    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            llEmptyState.visibility = View.VISIBLE
            rvApplications.visibility = View.GONE
        } else {
            llEmptyState.visibility = View.GONE
            rvApplications.visibility = View.VISIBLE
        }
    }

    // Shows dialog with smart autocomplete for University & Course
    private fun showAddApplicationDialog() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Please log in to add applications", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_application, null)
        val actvUni = dialogView.findViewById<AutoCompleteTextView>(R.id.actvAddUni)
        val actvCourse = dialogView.findViewById<AutoCompleteTextView>(R.id.actvAddCourse)
        val etDeadline = dialogView.findViewById<EditText>(R.id.etAddDeadline)
        val tvStatusHint = dialogView.findViewById<TextView>(R.id.tvSearchStatusHint)

        var selectedCourseId: String? = null

        val uniAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, mutableListOf()) {
            private val filteredList = mutableListOf<String>()

            override fun getCount(): Int = filteredList.size
            override fun getItem(position: Int): String = filteredList[position]

            override fun getFilter(): Filter {
                return object : Filter() {
                    override fun performFiltering(constraint: CharSequence?): FilterResults {
                        val results = FilterResults()
                        filteredList.clear()

                        if (constraint.isNullOrBlank()) {
                            filteredList.addAll(allInstitutionsList.map { it.name })
                        } else {
                            val query = constraint.toString().trim().lowercase(Locale.ROOT)
                            val matchedSet = mutableSetOf<String>()

                            val mappedNickname = UNIVERSITY_NICKNAMES[query]
                            if (mappedNickname != null) {
                                matchedSet.add(mappedNickname)
                            }

                            for (uni in allInstitutionsList) {
                                val fullName = uni.name
                                val lowerName = fullName.lowercase(Locale.ROOT)

                                val initials = fullName.split(" ")
                                    .filter { it.isNotBlank() && it[0].isUpperCase() }
                                    .map { it[0] }
                                    .joinToString("")
                                    .lowercase(Locale.ROOT)

                                if (lowerName.contains(query) || initials.contains(query)) {
                                    matchedSet.add(fullName)
                                }
                            }

                            filteredList.addAll(matchedSet)
                        }

                        results.values = filteredList
                        results.count = filteredList.size
                        return results
                    }

                    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                        if (results != null && results.count > 0) {
                            tvStatusHint.text = "Matches found. Select from list or keep typing."
                            notifyDataSetChanged()
                        } else {
                            tvStatusHint.text = "No match found. You can still type custom text."
                            notifyDataSetInvalidated()
                        }
                    }
                }
            }
        }
        actvUni.setAdapter(uniAdapter)

        val courseNamesList = allCoursesList.map { "${it.name} (${it.institution})" }
        val courseAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, courseNamesList)
        actvCourse.setAdapter(courseAdapter)

        actvCourse.setOnItemClickListener { _, _, position, _ ->
            if (position in allCoursesList.indices) {
                val matched = allCoursesList[position]
                selectedCourseId = matched.id
                actvCourse.setText(matched.name, false)
            }
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Add Application") { _, _ ->
                val uni = actvUni.text.toString().trim()
                val course = actvCourse.text.toString().trim()
                var deadline = etDeadline.text.toString().trim()

                if (uni.isEmpty() || course.isEmpty()) {
                    Toast.makeText(this, "Please enter university and course", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (deadline.isEmpty()) {
                    deadline = "30 Sep 2025"
                }

                saveApplicationToFirestore(uid, uni, course, deadline, selectedCourseId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveApplicationToFirestore(
        uid: String,
        uni: String,
        course: String,
        deadline: String,
        courseId: String? = null
    ) {
        val appData = hashMapOf(
            "university" to uni,
            "course" to course,
            "status" to "Submitted",
            "submittedAt" to FieldValue.serverTimestamp(),
            "deadline" to deadline,
            "docsUploaded" to 0,
            "docsTotal" to 4,
            "createdAt" to FieldValue.serverTimestamp(),
            "courseId" to (courseId ?: "")
        )

        db.collection("users").document(uid).collection("applications")
            .add(appData)
            .addOnSuccessListener { ref ->
                Log.d(TAG, "Application added successfully with ID: ${ref.id}")
                Toast.makeText(this, "Application added to tracker!", Toast.LENGTH_SHORT).show()
                loadApplicationsFromFirestore()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to add application", e)
                Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

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
                else -> {
                    holder.tvStatusChip.setTextColor(getColor(R.color.status_submitted_fg))
                    holder.tvStatusChip.backgroundTintList = getColorStateList(R.color.status_submitted_bg)
                }
            }

            val totalDocs = if (item.docsTotal > 0) item.docsTotal else 4
            holder.tvDocsCount.text = "Documents ${item.docsUploaded}/$totalDocs"
            holder.pbDocsProgress.max = totalDocs
            holder.pbDocsProgress.progress = item.docsUploaded

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