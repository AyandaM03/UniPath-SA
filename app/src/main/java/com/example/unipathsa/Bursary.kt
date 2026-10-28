package com.example.unipathsa

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import java.io.Serializable
import java.util.Locale

data class Bursary(
    var id: String = "",
    var name: String = "",
    var provider: String = "",
    var amount: String = "",
    var closingDate: String = "",
    var category: String = "", // e.g. "IT", "Commerce", "Engineering", "Law"
    var website: String = "",
    var description: String = ""
) : Serializable {

    companion object {
        private const val TAG = "BursaryParser"

        fun fromFirestore(doc: DocumentSnapshot): Bursary {
            val docId = doc.id
            val rawData = doc.data ?: emptyMap()

            // Build lowercase key lookup map
            val lowerMap = HashMap<String, Any?>()
            for ((key, value) in rawData) {
                lowerMap[key.lowercase(Locale.ROOT).trim()] = value
            }

            // Known name keys in order
            val nameKeys = listOf("name", "bursaryname", "title", "schemename", "fundername", "bursary", "programme")
            var resolvedName = ""

            for (k in nameKeys) {
                val valStr = lowerMap[k]?.toString()?.trim()
                if (!valStr.isNullOrBlank()) {
                    resolvedName = valStr
                    break
                }
            }

            // Fallback: search any key containing "name" or "title"
            if (resolvedName.isBlank()) {
                for ((key, value) in lowerMap) {
                    if ((key.contains("name") || key.contains("title")) && value != null) {
                        val valStr = value.toString().trim()
                        if (valStr.isNotBlank()) {
                            resolvedName = valStr
                            break
                        }
                    }
                }
            }

            val finalName = if (resolvedName.isBlank()) {
                Log.w(TAG, "Bursary document missing name key! Doc ID: $docId, Keys: ${rawData.keys}")
                "Untitled bursary"
            } else {
                resolvedName
            }

            // Provider lookup
            val providerKeys = listOf("provider", "funder", "company", "sponsor", "organization", "institution")
            var resolvedProvider = ""
            for (k in providerKeys) {
                val valStr = lowerMap[k]?.toString()?.trim()
                if (!valStr.isNullOrBlank()) {
                    resolvedProvider = valStr
                    break
                }
            }

            // Amount / Funding coverage lookup
            val amountKeys = listOf("amount", "funding", "coverage", "type", "fundingtype")
            var resolvedAmount = "Full Tuition"
            for (k in amountKeys) {
                val valStr = lowerMap[k]?.toString()?.trim()
                if (!valStr.isNullOrBlank()) {
                    resolvedAmount = valStr
                    break
                }
            }

            // Closing date lookup
            val dateKeys = listOf("closingdate", "deadline", "closing", "expirydate", "duedate")
            var resolvedDate = "31 Oct 2025"
            for (k in dateKeys) {
                val valStr = lowerMap[k]?.toString()?.trim()
                if (!valStr.isNullOrBlank()) {
                    resolvedDate = valStr
                    break
                }
            }

            // Category lookup
            val catKeys = listOf("category", "field", "faculty", "department")
            var resolvedCat = "General"
            for (k in catKeys) {
                val valStr = lowerMap[k]?.toString()?.trim()
                if (!valStr.isNullOrBlank()) {
                    resolvedCat = valStr
                    break
                }
            }

            // Website URL
            val webKeys = listOf("website", "url", "link", "applicationurl")
            var resolvedWeb = ""
            for (k in webKeys) {
                val valStr = lowerMap[k]?.toString()?.trim()
                if (!valStr.isNullOrBlank()) {
                    resolvedWeb = valStr
                    break
                }
            }

            // Description
            val descKeys = listOf("description", "overview", "details", "eligibility")
            var resolvedDesc = ""
            for (k in descKeys) {
                val valStr = lowerMap[k]?.toString()?.trim()
                if (!valStr.isNullOrBlank()) {
                    resolvedDesc = valStr
                    break
                }
            }

            return Bursary(
                id = docId,
                name = finalName,
                provider = resolvedProvider,
                amount = resolvedAmount,
                closingDate = resolvedDate,
                category = resolvedCat,
                website = resolvedWeb,
                description = resolvedDesc
            )
        }
    }
}