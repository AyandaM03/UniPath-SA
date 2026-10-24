package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SavedActivity : AppCompatActivity() {

    private lateinit var recyclerSaved: RecyclerView
    private lateinit var tvEmpty: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved)

        NavigationHelper.setupDrawer(this, R.id.navSaved)

        recyclerSaved = findViewById(R.id.recyclerSaved)
        tvEmpty = findViewById(R.id.tvEmptySaved)
        recyclerSaved.layoutManager = LinearLayoutManager(this)

        loadFavourites()
    }

    private fun loadFavourites() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("favourites")
            .get()
            .addOnSuccessListener { documents ->
                val savedList = documents.map { it.toObject(Institution::class.java) }

                if (savedList.isEmpty()) {
                    tvEmpty.visibility = android.view.View.VISIBLE
                } else {
                    tvEmpty.visibility = android.view.View.GONE
                    recyclerSaved.adapter = InstitutionAdapter(savedList) { institution ->
                        val intent = Intent(this, UniversityDetailActivity::class.java)
                        intent.putExtra("UNIVERSITY_DATA", institution.toUniversity())
                        startActivity(intent)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Unable to load saved institutions", Toast.LENGTH_SHORT).show()
            }
    }
}