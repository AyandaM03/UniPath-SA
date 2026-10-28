package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SavedActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "SavedActivity"
    }

    private lateinit var recyclerSaved: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var llEmptyContainer: LinearLayout
    private lateinit var btnExplorePrompt: MaterialButton
    private lateinit var btnNotif: RelativeLayout
    private lateinit var tvNotifBadge: TextView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved)
        Log.d(TAG, "onCreate initialized")

        NavigationHelper.setupDrawer(this, R.id.navSaved)

        bindViews()
        setupListeners()
        loadSavedItems()
    }

    override fun onResume() {
        super.onResume()
        loadSavedItems()
    }

    private fun bindViews() {
        recyclerSaved = findViewById(R.id.recyclerSaved)
        tvEmpty = findViewById(R.id.tvEmptySaved)
        llEmptyContainer = findViewById(R.id.llEmptySavedContainer)
        btnExplorePrompt = findViewById(R.id.btnExploreSavedPrompt)
        btnNotif = findViewById(R.id.btnNotifications)
        tvNotifBadge = findViewById(R.id.tvNotifBadge)

        recyclerSaved.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        btnNotif.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        btnExplorePrompt.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }
    }

    private fun loadSavedItems() {
        val uid = auth.currentUser?.uid ?: return

        // Read unread notifications
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

        // Read saved items
        db.collection("users").document(uid).collection("savedItems")
            .get()
            .addOnSuccessListener { documents ->
                val savedList = mutableListOf<Institution>()

                if (documents != null && !documents.isEmpty) {
                    for (doc in documents) {
                        val inst = doc.toObject(Institution::class.java)
                        inst.id = doc.id
                        if (inst.name.isBlank()) {
                            inst.name = doc.getString("title") ?: doc.getString("name") ?: "Saved Item"
                        }
                        savedList.add(inst)
                    }

                    llEmptyContainer.visibility = View.GONE
                    recyclerSaved.visibility = View.VISIBLE
                    recyclerSaved.adapter = InstitutionAdapter(savedList) { institution ->
                        val intent = Intent(this, UniversityDetailActivity::class.java)
                        intent.putExtra("UNIVERSITY_DATA", institution.toUniversity())
                        startActivity(intent)
                    }
                } else {
                    llEmptyContainer.visibility = View.VISIBLE
                    recyclerSaved.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading saved items", e)
                Toast.makeText(this, "Unable to load saved items", Toast.LENGTH_SHORT).show()
            }
    }
}