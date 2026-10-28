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
            val name = doc.getString("name")
                ?: doc.getString("name ")
                ?: doc.getString("title")
                ?: doc.getString("institutionName")
                ?: docId
            val province = doc.getString("province")
                ?: doc.getString("province\t")
                ?: doc.getString("provinceName")
                ?: ""
            val type = doc.getString("type")
                ?: doc.getString("category")
                ?: doc.getString("institutionType")
                ?: "University"
            val location = doc.getString("location")
                ?: doc.getString("city")
                ?: if (province.isNotEmpty()) province else "South Africa"
            val description = doc.getString("description")
                ?: doc.getString("shortDescription")
                ?: ""
            val longDescription = doc.getString("longDescription")
                ?: doc.getString("about")
                ?: description

            val progRaw = doc.get("programmes") ?: doc.get("faculties") ?: doc.get("courses")
            val programmesStr = when (progRaw) {
                is List<*> -> progRaw.filterNotNull().joinToString(", ")
                is String -> progRaw
                else -> ""
            }

            val requirements = doc.getString("requirements")
                ?: doc.getString("entryRequirements")
                ?: ""
            val applicationInfo = doc.getString("applicationInfo")
                ?: doc.getString("howToApply")
                ?: ""
            val importantInfo = doc.getString("importantInfo")
                ?: doc.getString("importantDates")
                ?: doc.getString("closingDate")
                ?: ""
            val websiteUrl = doc.getString("website")
                ?: doc.getString("websiteUrl")
                ?: doc.getString("url")
                ?: ""
            val freeToApply = doc.getBoolean("freeToApply")
                ?: doc.getBoolean("freeToApply\t")
                ?: doc.getBoolean("freeApplication")
                ?: false
            val imageUrl = doc.getString("imageUrl")
                ?: doc.getString(" imageUrl")
                ?: ""
            val logoUrl = doc.getString("logoUrl")
                ?: doc.getString("logo")
                ?: if (imageUrl.isNotBlank() && !imageUrl.contains("campus", ignoreCase = true)) imageUrl else ""
            val heroImageUrl = doc.getString("heroImageUrl")
                ?: doc.getString("heroImage")
                ?: doc.getString("campusImage")
                ?: if (imageUrl.isNotBlank() && imageUrl.contains("campus", ignoreCase = true)) imageUrl else ""

            val minAps = when (val rawAps = doc.get("minAps") ?: doc.get("aps") ?: doc.get("minApsRequirement") ?: doc.get("apsRequirement")) {
                is Number -> rawAps.toInt()
                is String -> rawAps.toIntOrNull() ?: 0
                else -> 0
            }

            // Application Fee Fields
            val rawFee = doc.get("applicationFee") ?: doc.get("fee") ?: doc.get("appFee")
            val applicationFee: Int? = when (rawFee) {
                is Number -> rawFee.toInt()
                is String -> rawFee.replace("[^0-9]".toRegex(), "").toIntOrNull()
                else -> if (freeToApply) 0 else null
            }

            val applicationFeeNote = doc.getString("applicationFeeNote") ?: doc.getString("feeNote") ?: ""
            val applicationFeeUrl = doc.getString("applicationFeeUrl") ?: doc.getString("feeUrl") ?: ""
            val applicationFeeYear = when (val y = doc.get("applicationFeeYear") ?: doc.get("feeYear")) {
                is Number -> y.toInt().toString()
                is String -> y
                else -> ""
            }

            val feeLastChecked = when (val fc = doc.get("feeLastChecked") ?: doc.get("lastChecked")) {
                is Timestamp -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(fc.toDate())
                is String -> fc
                else -> ""
            }

            Log.d(TAG, "Parsed University: $name | Fee: $applicationFee | FeeNote: $applicationFeeNote | FeeYear: $applicationFeeYear")

            return University(
                id = docId,
                name = name,
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