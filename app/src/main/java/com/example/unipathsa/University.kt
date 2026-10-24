package com.example.unipathsa

import com.google.firebase.firestore.DocumentSnapshot
import java.io.Serializable

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
    val heroImageUrl: String = ""
) : Serializable {

    val importantDates: String get() = importantInfo
    val website: String get() = websiteUrl

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
                heroImageUrl = heroImageUrl
            )
        }
    }
}