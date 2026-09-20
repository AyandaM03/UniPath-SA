package com.example.unipathsa

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore

class InstitutionsActivity : AppCompatActivity() {

    private lateinit var recyclerInstitutions: RecyclerView
    private lateinit var searchEditText: TextInputEditText
    private lateinit var adapter: InstitutionAdapter

    private val institutionList = mutableListOf<Institution>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_institutions)

        recyclerInstitutions = findViewById(R.id.recyclerInstitutions)
        searchEditText = findViewById(R.id.etSearchInstitution)
        val universitiesButton = findViewById<Button>(R.id.btnUniversities)
        val collegesButton = findViewById<Button>(R.id.btnColleges)
        val filterButton = findViewById<Button>(R.id.btnFilter)

        // RecyclerView setup
        adapter = InstitutionAdapter(institutionList) { institution ->
            val intent = Intent(this, InstitutionDetailsActivity::class.java)
            intent.putExtra("name", institution.name)
            intent.putExtra("province", institution.province)
            intent.putExtra("type", institution.type)
            intent.putExtra("description", institution.description)
            intent.putExtra("imageUrl", institution.imageUrl)
            intent.putExtra("website", institution.website)
            intent.putExtra("freeToApply", institution.freeToApply)
            startActivity(intent)
        }

        recyclerInstitutions.layoutManager = LinearLayoutManager(this)
        recyclerInstitutions.adapter = adapter

        // Load universities when page opens
        loadInstitutions("University")

        // Universities button
        universitiesButton.setOnClickListener {
            universitiesButton.setBackgroundColor(android.graphics.Color.rgb(21, 101, 192))
            universitiesButton.setTextColor(android.graphics.Color.WHITE)
            collegesButton.setBackgroundColor(android.graphics.Color.rgb(227, 242, 253))
            collegesButton.setTextColor(android.graphics.Color.rgb(21, 101, 192))
            loadInstitutions("University")
        }

        // Colleges button
        collegesButton.setOnClickListener {
            collegesButton.setBackgroundColor(android.graphics.Color.rgb(21, 101, 192))
            collegesButton.setTextColor(android.graphics.Color.WHITE)
            universitiesButton.setBackgroundColor(android.graphics.Color.rgb(227, 242, 253))
            universitiesButton.setTextColor(android.graphics.Color.rgb(21, 101, 192))
            loadInstitutions("College")
        }

        // Search
        searchEditText.setOnEditorActionListener { _, _, _ ->
            searchInstitutions(searchEditText.text.toString())
            true
        }

        // Filter
        filterButton.setOnClickListener {
            searchInstitutions(searchEditText.text.toString())
        }

        // Bottom nav
        BottomNavHelper.setup(this, R.id.nav_institutions)
    }

    private fun loadInstitutions(type: String) {
        db.collection("institutions")
            .whereEqualTo("type", type)
            .get()
            .addOnSuccessListener { documents ->
                institutionList.clear()
                for (document in documents) {
                    val institution = document.toObject(Institution::class.java)
                    institution.id = document.id
                    institutionList.add(institution)
                }
                adapter.updateList(institutionList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Unable to load institutions", Toast.LENGTH_SHORT).show()
            }
    }

    private fun searchInstitutions(searchText: String) {
        if (searchText.isBlank()) {
            adapter.updateList(institutionList)
            return
        }
        val results = institutionList.filter {
            it.name.contains(searchText, ignoreCase = true) ||
                    it.province.contains(searchText, ignoreCase = true)
        }
        adapter.updateList(results)
    }
}