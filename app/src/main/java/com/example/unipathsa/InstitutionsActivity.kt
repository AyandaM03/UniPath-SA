package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class InstitutionsActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "InstitutionsActivity"
    }

    private lateinit var recyclerInstitutions: RecyclerView
    private lateinit var searchEditText: TextInputEditText
    private lateinit var btnUniversities: MaterialButton
    private lateinit var btnColleges: MaterialButton
    private lateinit var btnNotif: RelativeLayout
    private lateinit var tvNotifBadge: TextView
    private lateinit var adapter: InstitutionAdapter

    private val allInstitutions = mutableListOf<Institution>()
    private val displayedList = mutableListOf<Institution>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var currentType = "University"
    private var searchQuery = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_institutions)
        Log.d(TAG, "onCreate initialized")

        NavigationHelper.setupDrawer(this, R.id.navInstitutions)

        bindViews()
        setupListeners()
        loadUnreadNotifications()
        loadInstitutions()
    }

    private fun bindViews() {
        recyclerInstitutions = findViewById(R.id.recyclerInstitutions)
        searchEditText = findViewById(R.id.etSearchInstitution)
        btnUniversities = findViewById(R.id.btnUniversities)
        btnColleges = findViewById(R.id.btnColleges)
        btnNotif = findViewById(R.id.btnNotifications)
        tvNotifBadge = findViewById(R.id.tvNotifBadge)

        adapter = InstitutionAdapter(displayedList) { institution ->
            val intent = Intent(this, UniversityDetailActivity::class.java)
            intent.putExtra("UNIVERSITY_DATA", institution.toUniversity())
            startActivity(intent)
        }

        recyclerInstitutions.layoutManager = LinearLayoutManager(this)
        recyclerInstitutions.adapter = adapter
    }

    private fun setupListeners() {
        btnNotif.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        btnUniversities.setOnClickListener {
            currentType = "University"
            highlightActiveTab()
            filterAndDisplay()
        }

        btnColleges.setOnClickListener {
            currentType = "College"
            highlightActiveTab()
            filterAndDisplay()
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                filterAndDisplay()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun highlightActiveTab() {
        if (currentType == "University") {
            btnUniversities.setBackgroundColor(getColor(R.color.primary))
            btnUniversities.setTextColor(getColor(R.color.white))
            btnColleges.setBackgroundColor(getColor(R.color.surface))
            btnColleges.setTextColor(getColor(R.color.primary))
        } else {
            btnColleges.setBackgroundColor(getColor(R.color.primary))
            btnColleges.setTextColor(getColor(R.color.white))
            btnUniversities.setBackgroundColor(getColor(R.color.surface))
            btnUniversities.setTextColor(getColor(R.color.primary))
        }
    }

    private fun loadUnreadNotifications() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("notifications")
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snap ->
                val count = snap?.size() ?: 0
                if (count > 0) {
                    tvNotifBadge.visibility = View.VISIBLE
                    tvNotifBadge.text = if (count > 9) "9+" else count.toString()
                } else {
                    tvNotifBadge.visibility = View.GONE
                }
            }
    }

    private fun loadInstitutions() {
        // 1. Show the saved copy from Room (works offline)
        lifecycleScope.launch {
            AppDatabase.get(this@InstitutionsActivity).institutionDao().getAll()
                .collect { rows ->
                    allInstitutions.clear()
                    allInstitutions.addAll(rows.map { it.toInstitution() })
                    Log.d(TAG, "Showing ${allInstitutions.size} institutions from Room")
                    filterAndDisplay()
                }
        }

        // 2. When there is internet, download fresh data in the background
        SyncScheduler.schedule(this)
    }

    private fun filterAndDisplay() {
        val filtered = allInstitutions.filter { inst ->
            val isCollegeOrTvet = inst.type.contains("College", ignoreCase = true) || inst.type.contains("TVET", ignoreCase = true)
            val matchesType = if (currentType.equals("University", ignoreCase = true)) {
                !isCollegeOrTvet // Strictly Universities only
            } else {
                isCollegeOrTvet // Colleges & TVETs only
            }

            val matchesSearch = searchQuery.isEmpty() ||
                    inst.name.contains(searchQuery, ignoreCase = true) ||
                    inst.province.contains(searchQuery, ignoreCase = true)
            matchesType && matchesSearch
        }

        displayedList.clear()
        displayedList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }
}