package com.example.unipathsa

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

data class University(
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val longDescription: String = "",
    val programmes: String = "",
    val requirements: String = "",
    val applicationInfo: String = "",
    val importantInfo: String = "",
    val logoResId: Int = android.R.drawable.ic_menu_gallery,
    val id: String = "",
    val province: String = "",
    val websiteUrl: String = "",
    val type: String = "University",
    val freeToApply: Boolean = false,
    val imageUrl: String = "",
    val logoUrl: String = "",
    val heroImageUrl: String = "",
    val minAps: Int = 0,
    val applicationFee: Int? = null,
    val applicationFeeNote: String = "",
    val applicationFeeUrl: String = "",
    val applicationFeeYear: String = "",
    val feeLastChecked: String = ""
) : Serializable {

    val importantDates: String get() = importantInfo
    val website: String get() = websiteUrl

    // Returns display text for application fee (e.g. "Free to apply", "R100", "Fee not listed yet")
    fun getFormattedFeeText(): String {
        return when {
            freeToApply || applicationFee == 0 -> "Free to apply"
            applicationFee != null && applicationFee > 0 -> "R$applicationFee"
            applicationFeeNote.isNotBlank() -> applicationFeeNote
            else -> "Fee not listed yet"
        }
    }

    // Returns logo URL (HTTPS only)
    fun getBestLogoUrl(): String {
        val url = when {
            logoUrl.isNotBlank() -> logoUrl
            imageUrl.isNotBlank() && !imageUrl.contains("campus", ignoreCase = true) -> imageUrl
            else -> ""
        }.trim()

        return sanitizeHttpsUrl(url)
    }

    // Returns hero campus image URL (HTTPS only)
    fun getBestHeroUrl(): String {
        val url = when {
            heroImageUrl.isNotBlank() -> heroImageUrl
            imageUrl.isNotBlank() && imageUrl.contains("campus", ignoreCase = true) -> imageUrl
            else -> ""
        }.trim()

        return sanitizeHttpsUrl(url)
    }

    private fun sanitizeHttpsUrl(url: String): String {
        if (url.isBlank()) return ""
        if (url.startsWith("http://")) {
            return "https://" + url.substring(7)
        }
        if (!url.startsWith("https://")) {
            return "https://$url"
        }
        return url
    }

    companion object {
        private const val TAG = "UniversityParser"

        fun fromFirestore(doc: DocumentSnapshot): University {
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
            val location = lowerMap["location"]?.toString() ?: lowerMap["city"]?.toString() ?: if (province.isNotEmpty()) province else "South Africa"
            val description = lowerMap["description"]?.toString() ?: ""
            val longDescription = lowerMap["longdescription"]?.toString() ?: lowerMap["about"]?.toString() ?: description

            val progRaw = lowerMap["programmes"] ?: lowerMap["faculties"] ?: lowerMap["courses"]
            val programmesStr = when (progRaw) {
                is List<*> -> progRaw.filterNotNull().joinToString(", ")
                is String -> progRaw
                else -> ""
            }

            val requirements = lowerMap["requirements"]?.toString() ?: lowerMap["entryrequirements"]?.toString() ?: ""
            val applicationInfo = lowerMap["applicationinfo"]?.toString() ?: lowerMap["howtoapply"]?.toString() ?: ""
            val importantInfo = lowerMap["importantinfo"]?.toString() ?: lowerMap["importantdates"]?.toString() ?: lowerMap["closingdate"]?.toString() ?: ""
            val websiteUrl = lowerMap["website"]?.toString() ?: lowerMap["websiteurl"]?.toString() ?: lowerMap["url"]?.toString() ?: ""
            val freeToApply = (lowerMap["freetoapply"] as? Boolean) ?: false
            val imageUrl = lowerMap["imageurl"]?.toString() ?: ""
            val logoUrl = lowerMap["logourl"]?.toString() ?: lowerMap["logo"]?.toString() ?: if (imageUrl.isNotBlank() && !imageUrl.contains("campus", ignoreCase = true)) imageUrl else ""
            val heroImageUrl = lowerMap["heroimageurl"]?.toString() ?: lowerMap["heroimage"]?.toString() ?: lowerMap["campusimage"]?.toString() ?: if (imageUrl.isNotBlank() && imageUrl.contains("campus", ignoreCase = true)) imageUrl else ""

            val minAps = when (val rawAps = lowerMap["minaps"] ?: lowerMap["aps"] ?: lowerMap["minapsrequirement"]) {
                is Number -> rawAps.toInt()
                is String -> rawAps.toIntOrNull() ?: 0
                else -> 0
            }

            // Application Fee Fields
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

            Log.d(TAG, "Parsed University from Firestore doc ID $docId: '$finalName'")

            return University(
                id = docId,
                name = finalName,
                location = location,
                description = description,
                longDescription = longDescription,
                programmes = programmesStr,
                requirements = requirements,
                applicationInfo = applicationInfo,
                importantInfo = importantInfo,
                logoResId = R.drawable.unipath_logo,
                province = province,
                websiteUrl = websiteUrl,
                type = type,
                freeToApply = freeToApply,
                imageUrl = imageUrl,
                logoUrl = logoUrl,
                heroImageUrl = heroImageUrl,
                minAps = minAps,
                applicationFee = applicationFee,
                applicationFeeNote = applicationFeeNote,
                applicationFeeUrl = applicationFeeUrl,
                applicationFeeYear = applicationFeeYear,
                feeLastChecked = feeLastChecked
            )
        }
    }
}