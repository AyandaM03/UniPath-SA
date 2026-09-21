package com.example.unipathsa

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

        recyclerSaved = findViewById(R.id.recyclerSaved)
        tvEmpty = findViewById(R.id.tvEmptySaved)
        recyclerSaved.layoutManager = LinearLayoutManager(this)

        loadFavourites()
        BottomNavHelper.setup(this, R.id.nav_profile) // no dedicated tab, opened from Profile
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
                        val intent = android.content.Intent(this, InstitutionDetailsActivity::class.java)
                        intent.putExtra("name", institution.name)
                        intent.putExtra("province", institution.province)
                        intent.putExtra("type", institution.type)
                        intent.putExtra("description", institution.description)
                        intent.putExtra("imageUrl", institution.imageUrl)
                        intent.putExtra("website", institution.website)
                        intent.putExtra("freeToApply", institution.freeToApply)
                        startActivity(intent)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Unable to load saved institutions", Toast.LENGTH_SHORT).show()
            }
    }
}