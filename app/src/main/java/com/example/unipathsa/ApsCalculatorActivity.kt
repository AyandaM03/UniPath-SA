package com.example.unipathsa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApsCalculatorActivity : AppCompatActivity() {

    // The list of subject names available in each row's dropdown
    private val subjectOptions = arrayOf(
        "Mathematics", "Mathematical Literacy", "Physical Sciences",
        "Life Sciences", "English", "Afrikaans",
        "Accounting", "Geography", "History", "Business Studies",
        "Economics", "Life Orientation", "Information Technology"
    )

    // Keeps track of every subject row currently added, so we can read their values later
    private val subjectRows = mutableListOf<View>()

    private lateinit var subjectRowsContainer: LinearLayout
    private lateinit var tvTotalScore: TextView
    private lateinit var tvSubjectsEntered: TextView
    private lateinit var recyclerMatches: RecyclerView
    private lateinit var tvResultsTitle: TextView
    private lateinit var tvNoMatches: TextView
    private lateinit var progressResults: ProgressBar

    private var currentApsScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aps_calculator)

        subjectRowsContainer = findViewById(R.id.subjectRowsContainer)
        tvTotalScore = findViewById(R.id.tvTotalScore)
        tvSubjectsEntered = findViewById(R.id.tvSubjectsEntered)
        recyclerMatches = findViewById(R.id.recyclerMatches)
        tvResultsTitle = findViewById(R.id.tvResultsTitle)
        tvNoMatches = findViewById(R.id.tvNoMatches)
        progressResults = findViewById(R.id.progressResults)

        recyclerMatches.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.btnAddSubject).setOnClickListener {
            addSubjectRow()
        }

        findViewById<Button>(R.id.btnCalculate).setOnClickListener {
            calculateAps()
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        BottomNavHelper.setup(this, R.id.nav_home)

        // Start with two empty rows so the screen isn't blank
        addSubjectRow()
        addSubjectRow()
    }

    // Inflates one subject row and adds it to the container
    private fun addSubjectRow() {
        val row = LayoutInflater.from(this)
            .inflate(R.layout.item_subject_row, subjectRowsContainer, false)

        val spinner = row.findViewById<Spinner>(R.id.spinnerSubject)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subjectOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Removing this specific row when the X is tapped
        row.findViewById<ImageButton>(R.id.btnRemoveRow).setOnClickListener {
            subjectRowsContainer.removeView(row)
            subjectRows.remove(row)
        }

        subjectRowsContainer.addView(row)
        subjectRows.add(row)
    }

    // Converts a percentage mark into APS points
    private fun percentToPoints(percent: Int): Int {
        return when {
            percent in 80..100 -> 7
            percent in 70..79 -> 6
            percent in 60..69 -> 5
            percent in 50..59 -> 4
            percent in 40..49 -> 3
            percent in 30..39 -> 2
            percent in 0..29 -> 1
            else -> 0
        }
    }

    private fun calculateAps() {
        var totalScore = 0
        var subjectsEntered = 0

        for (row in subjectRows) {
            val etPercent = row.findViewById<EditText>(R.id.etPercent)
            val text = etPercent.text.toString().trim()

            if (text.isNotEmpty()) {
                val percent = text.toIntOrNull()
                if (percent != null && percent in 0..100) {
                    totalScore += percentToPoints(percent)
                    subjectsEntered++
                }
            }
        }

        currentApsScore = totalScore
        tvTotalScore.text = totalScore.toString()
        tvSubjectsEntered.text = "$subjectsEntered subjects added"

        if (subjectsEntered == 0) {
            Toast.makeText(this, "Enter at least one subject mark", Toast.LENGTH_SHORT).show()
            return
        }

        fetchMatchingCourses(totalScore)
    }

    // Calls the Firestore REST API and filters courses the student qualifies for
    private fun fetchMatchingCourses(apsScore: Int) {
        tvResultsTitle.visibility = View.VISIBLE
        tvNoMatches.visibility = View.GONE
        progressResults.visibility = View.VISIBLE
        recyclerMatches.visibility = View.GONE

        RetrofitClient.api.getCourses().enqueue(object : Callback<FirestoreListResponse> {

            override fun onResponse(
                call: Call<FirestoreListResponse>,
                response: Response<FirestoreListResponse>
            ) {
                progressResults.visibility = View.GONE

                if (response.isSuccessful) {
                    val documents = response.body()?.documents ?: emptyList()

                    val allCourses = documents.map { doc ->
                        Course(
                            name = doc.fields.name?.stringValue ?: "",
                            university = doc.fields.university?.stringValue ?: "",
                            apsRequired = doc.fields.apsRequired?.integerValue?.toIntOrNull() ?: 0,
                            category = doc.fields.faculty?.stringValue ?: ""
                        )
                    }

                    // Only keep courses the student's score actually qualifies for
                    val matchingCourses = allCourses.filter { it.apsRequired <= apsScore }

                    if (matchingCourses.isEmpty()) {
                        tvNoMatches.text =
                            "No matching courses for a score of $apsScore yet — try adding more subjects."
                        tvNoMatches.visibility = View.VISIBLE
                        recyclerMatches.visibility = View.GONE
                    } else {
                        recyclerMatches.visibility = View.VISIBLE
                        recyclerMatches.adapter = CourseMatchAdapter(matchingCourses)
                    }
                } else {
                    Toast.makeText(
                        this@ApsCalculatorActivity,
                        "Failed to load courses",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<FirestoreListResponse>, t: Throwable) {
                progressResults.visibility = View.GONE
                Toast.makeText(
                    this@ApsCalculatorActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}