package com.example.unipathsa

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ApsCalculatorActivity : AppCompatActivity() {

    data class SubjectInput(val id: String, val name: String, var mark: Int)

    private lateinit var tvScore: TextView
    private lateinit var tvSubjectsCount: TextView
    private lateinit var pbSubjects: ProgressBar
    private lateinit var rvSubjects: RecyclerView
    private lateinit var btnAddSubject: Button
    private lateinit var btnCalculateAps: Button

    private val subjectsList = mutableListOf<SubjectInput>()
    private lateinit var adapter: SubjectInputAdapter

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aps_calculator)

        NavigationHelper.setupDrawer(this, R.id.navApsCalc)

        bindViews()
        setupRecyclerView()
        setupListeners()
        loadSubjectsFromFirestore()
    }

    private fun bindViews() {
        tvScore = findViewById(R.id.tvScore)
        tvSubjectsCount = findViewById(R.id.tvSubjectsCount)
        pbSubjects = findViewById(R.id.pbSubjects)
        rvSubjects = findViewById(R.id.rvSubjects)
        btnAddSubject = findViewById(R.id.btnAddSubject)
        btnCalculateAps = findViewById(R.id.btnCalculateAps)

        findViewById<View>(R.id.btnInfo)?.setOnClickListener {
            Toast.makeText(this, "APS is calculated from your entered NSC subject percentages (1–7 pts each).", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = SubjectInputAdapter(subjectsList,
            onMarkChanged = {
                calculateAndDisplayAps()
            },
            onDeleteClick = { subject ->
                confirmDeleteSubject(subject)
            }
        )
        rvSubjects.layoutManager = LinearLayoutManager(this)
        rvSubjects.adapter = adapter
    }

    private fun setupListeners() {
        btnAddSubject.setOnClickListener {
            showAddSubjectDialog()
        }

        btnCalculateAps.setOnClickListener {
            calculateAndSaveAps()
        }
    }

    private fun loadSubjectsFromFirestore() {
        val uid = auth.currentUser?.uid ?: run {
            loadDefaultSubjectsIfEmpty()
            return
        }

        db.collection("users").document(uid).collection("subjects")
            .get()
            .addOnSuccessListener { snapshot ->
                subjectsList.clear()
                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        val name = doc.getString("subjectName") ?: doc.id
                        val pct = (doc.getLong("percentage") ?: 0L).toInt()
                        subjectsList.add(SubjectInput(doc.id, name, pct))
                    }
                } else {
                    loadDefaultSubjectsIfEmpty()
                }
                adapter.notifyDataSetChanged()
                calculateAndDisplayAps()
            }
            .addOnFailureListener {
                loadDefaultSubjectsIfEmpty()
            }
    }

    private fun loadDefaultSubjectsIfEmpty() {
        if (subjectsList.isEmpty()) {
            subjectsList.add(SubjectInput("maths", "Mathematics", 75))
            subjectsList.add(SubjectInput("physics", "Physical Sciences", 70))
            subjectsList.add(SubjectInput("english", "English Home Language", 68))
            subjectsList.add(SubjectInput("biology", "Life Sciences", 72))
            subjectsList.add(SubjectInput("acc", "Accounting", 65))
            subjectsList.add(SubjectInput("lo", "Life Orientation", 80))
        }
        adapter.notifyDataSetChanged()
        calculateAndDisplayAps()
    }

    private fun showAddSubjectDialog() {
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 16)
        }

        val etName = EditText(this).apply { hint = "Subject Name (e.g. Geography)" }
        val etPct = EditText(this).apply {
            hint = "Mark Percentage (%)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(etName)
        layout.addView(etPct)

        AlertDialog.Builder(this)
            .setTitle("Add Subject")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString().trim()
                val pct = etPct.text.toString().toIntOrNull() ?: 0

                if (name.isNotEmpty() && pct in 0..100) {
                    val uid = auth.currentUser?.uid
                    val docId = name.lowercase().replace(" ", "_")

                    if (uid != null) {
                        val subData = hashMapOf(
                            "subjectName" to name,
                            "percentage" to pct,
                            "apsPoints" to markToPoints(pct)
                        )
                        db.collection("users").document(uid).collection("subjects").document(docId)
                            .set(subData, SetOptions.merge())
                            .addOnSuccessListener {
                                loadSubjectsFromFirestore()
                            }
                    } else {
                        subjectsList.add(SubjectInput(docId, name, pct))
                        adapter.notifyDataSetChanged()
                        calculateAndDisplayAps()
                    }
                } else {
                    Toast.makeText(this, "Enter valid subject name and percentage (0-100)", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteSubject(subject: SubjectInput) {
        AlertDialog.Builder(this)
            .setTitle("Remove Subject")
            .setMessage("Remove ${subject.name} from your list?")
            .setPositiveButton("Remove") { _, _ ->
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    db.collection("users").document(uid).collection("subjects").document(subject.id)
                        .delete()
                        .addOnSuccessListener {
                            subjectsList.removeAll { it.id == subject.id }
                            adapter.notifyDataSetChanged()
                            calculateAndSaveAps()
                        }
                } else {
                    subjectsList.removeAll { it.id == subject.id }
                    adapter.notifyDataSetChanged()
                    calculateAndDisplayAps()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun markToPoints(mark: Int): Int {
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

    private fun calculateAndDisplayAps(): Int {
        var totalPoints = 0
        var count = 0

        for (item in subjectsList) {
            if (item.mark in 0..100) {
                totalPoints += markToPoints(item.mark)
                count++
            }
        }

        tvScore.text = totalPoints.toString()
        tvSubjectsCount.text = "$count of 8 subjects entered"
        pbSubjects.progress = count
        return totalPoints
    }

    private fun calculateAndSaveAps() {
        val totalAps = calculateAndDisplayAps()
        val uid = auth.currentUser?.uid

        if (uid != null) {
            for (sub in subjectsList) {
                val subData = hashMapOf(
                    "subjectName" to sub.name,
                    "percentage" to sub.mark,
                    "apsPoints" to markToPoints(sub.mark)
                )
                db.collection("users").document(uid).collection("subjects").document(sub.id)
                    .set(subData, SetOptions.merge())
            }

            db.collection("users").document(uid)
                .set(hashMapOf("aps" to totalAps), SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "APS recalculated ($totalAps pts) & saved to profile!", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "APS recalculated ($totalAps pts)!", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class SubjectInputAdapter(
        private val list: MutableList<SubjectInput>,
        private val onMarkChanged: () -> Unit,
        private val onDeleteClick: (SubjectInput) -> Unit
    ) : RecyclerView.Adapter<SubjectInputAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvSubjectName)
            val etMark: EditText = view.findViewById(R.id.etPercentage)
            val tvPoints: TextView = view.findViewById(R.id.tvApsPoints)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_aps_input, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvName.text = item.name
            holder.etMark.setText(item.mark.toString())
            val pts = markToPoints(item.mark)
            holder.tvPoints.text = "$pts pts"

            holder.itemView.setOnLongClickListener {
                onDeleteClick(item)
                true
            }

            holder.etMark.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val input = s.toString().toIntOrNull()
                    if (input != null && input in 0..100) {
                        item.mark = input
                        val newPts = markToPoints(input)
                        holder.tvPoints.text = "$newPts pts"
                        onMarkChanged()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }

        override fun getItemCount() = list.size
    }
}