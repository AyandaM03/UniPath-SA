package com.example.unipathsa

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class CacheRepository(context: Context) {

    private val db = AppDatabase.get(context)

    suspend fun refreshInstitutions(): Boolean {
        return try {
            // Source.SERVER fails when offline, so we never overwrite
            // saved data with nothing
            val snapshot = Firebase.firestore.collection("institutions")
                .get(Source.SERVER).await()

            val items = snapshot.documents.map { d ->
                InstitutionEntity(
                    id = d.id,
                    name = d.getString("name") ?: "",
                    type = d.getString("type") ?: "",
                    province = d.getString("province") ?: "",
                    description = d.getString("description") ?: "",
                    website = d.getString("website") ?: "",
                    imageUrl = d.getString("imageUrl") ?: "",
                    logoUrl = d.getString("logoUrl") ?: "",
                    heroImageUrl = d.getString("heroImageUrl") ?: "",
                    freeToApply = d.getBoolean("freeToApply") ?: false
                )
            }
            db.institutionDao().replaceAll(items)
            Log.d("Cache", "Saved ${items.size} institutions to Room")
            true
        } catch (e: Exception) {
            Log.e("Cache", "Refresh failed (probably offline)", e)
            false
        }
    }

    suspend fun refreshAll(): Boolean = refreshInstitutions()
}