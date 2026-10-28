package com.example.unipathsa

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Locale

data class Institution(
    var id: String = "",
    var name: String = "",
    var province: String = "",
    var type: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var website: String = "",
    var freeToApply: Boolean = false,
    var heroImageUrl: String = "",
    var applicationFee: Int? = null,
    var applicationFeeNote: String = "",
    var applicationFeeUrl: String = "",
    var applicationFeeYear: String = "",
    var feeLastChecked: String = ""
) {
    fun toUniversity(): University {
        return University(
            id = id,
            name = name,
            location = if (province.isNotEmpty()) province else "South Africa",
            description = description,
            longDescription = description,
            province = province,
            type = if (type.isNotEmpty()) type else "University",
            freeToApply = freeToApply,
            websiteUrl = website,
            imageUrl = imageUrl,
            logoUrl = imageUrl,
            heroImageUrl = heroImageUrl,
            applicationFee = applicationFee,
            applicationFeeNote = applicationFeeNote,
            applicationFeeUrl = applicationFeeUrl,
            applicationFeeYear = applicationFeeYear,
            feeLastChecked = feeLastChecked
        )
    }

    companion object {
        private const val TAG = "InstitutionParser"

        fun fromFirestore(doc: DocumentSnapshot): Institution {
            val docId = doc.id
            val rawData = doc.data ?: emptyMap()

            val lowerMap = HashMap<String, Any?>()
            for ((key, value) in rawData) {
                lowerMap[key.lowercase(Locale.ROOT).trim()] = value
            }

            // Order: name, institutionname, title, universityname
            val nameKeys = listOf("name", "institutionname", "title", "universityname", "schoolname", "institution")
            var resolvedName = ""

            for (k in nameKeys) {
                val valStr = lowerMap[k]?.toString()?.trim()
                if (!valStr.isNullOrBlank()) {
                    resolvedName = valStr
                    break
                }
            }

            if (resolvedName.isBlank()) {
                for ((key, value) in lowerMap) {
                    if (key.contains("name") && value != null) {
                        val valStr = value.toString().trim()
                        if (valStr.isNotBlank()) {
                            resolvedName = valStr
                            break
                        }
                    }
                }
            }

            val finalName = if (resolvedName.isBlank()) {
                Log.w(TAG, "Document ID $docId has no name field! Keys: ${rawData.keys}")
                docId
            } else {
                resolvedName
            }

            val province = lowerMap["province"]?.toString() ?: ""
            val type = lowerMap["type"]?.toString() ?: lowerMap["category"]?.toString() ?: "University"
            val description = lowerMap["description"]?.toString() ?: ""
            val website = lowerMap["website"]?.toString() ?: lowerMap["websiteurl"]?.toString() ?: ""
            val freeToApply = (lowerMap["freetoapply"] as? Boolean) ?: false
            val imageUrl = lowerMap["imageurl"]?.toString() ?: lowerMap["logo"]?.toString() ?: ""
            val heroImageUrl = lowerMap["heroimageurl"]?.toString() ?: lowerMap["campusimage"]?.toString() ?: imageUrl

            val rawFee = lowerMap["applicationfee"] ?: lowerMap["fee"]
            val applicationFee: Int? = when (rawFee) {
                is Number -> rawFee.toInt()
                is String -> rawFee.replace("[^0-9]".toRegex(), "").toIntOrNull()
                else -> if (freeToApply) 0 else null
            }

            val applicationFeeNote = lowerMap["applicationfeenote"]?.toString() ?: ""
            val applicationFeeUrl = lowerMap["applicationfeeurl"]?.toString() ?: ""
            val applicationFeeYear = lowerMap["applicationfeeyear"]?.toString() ?: ""

            val feeLastChecked = when (val fc = lowerMap["feelastchecked"]) {
                is Timestamp -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(fc.toDate())
                is String -> fc
                else -> ""
            }

            Log.d(TAG, "Parsed Institution from Firestore doc ID $docId: '$finalName'")

            return Institution(
                id = docId,
                name = finalName,
                province = province,
                type = type,
                description = description,
                imageUrl = imageUrl,
                website = website,
                freeToApply = freeToApply,
                heroImageUrl = heroImageUrl,
                applicationFee = applicationFee,
                applicationFeeNote = applicationFeeNote,
                applicationFeeUrl = applicationFeeUrl,
                applicationFeeYear = applicationFeeYear,
                feeLastChecked = feeLastChecked
            )
        }
    }
}