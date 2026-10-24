package com.example.unipathsa

import com.google.firebase.firestore.DocumentSnapshot
import java.io.Serializable

data class Course(
    var id: String = "",
    val name: String = "",
    val institution: String = "",
    val field: String = "",
    val description: String = "",
    val duration: String = "",
    val apsRequirement: Int = 0,
    val requiredSubjects: List<SubjectRequirement> = emptyList(),
    val applicationInfo: String = "",
    val websiteUrl: String = ""
) : Serializable {

    // Backward-compatibility properties for legacy adapters and activities
    val university: String get() = this.institution
    val category: String get() = this.field
    val apsRequired: Int get() = this.apsRequirement
    val website: String get() = this.websiteUrl

    companion object {
        fun fromFirestore(doc: DocumentSnapshot): Course {
            val docId = doc.id
            val name = doc.getString("name")
                ?: doc.getString("courseName")
                ?: doc.getString("title")
                ?: docId
            val institution = doc.getString("institution")
                ?: doc.getString("university")
                ?: doc.getString("institutionName")
                ?: ""
            val field = doc.getString("field")
                ?: doc.getString("category")
                ?: doc.getString("faculty")
                ?: "General"
            val description = doc.getString("description")
                ?: doc.getString("overview")
                ?: ""
            val duration = doc.getString("duration")
                ?: doc.getString("period")
                ?: "3 Years"

            val aps = when (val rawAps = doc.get("apsRequirement") ?: doc.get("minAps") ?: doc.get("aps") ?: doc.get("apsRequired")) {
                is Number -> rawAps.toInt()
                is String -> rawAps.toIntOrNull() ?: 0
                else -> 0
            }

            val appInfo = doc.getString("applicationInfo")
                ?: doc.getString("howToApply")
                ?: doc.getString("requirements")
                ?: ""
            val website = doc.getString("websiteUrl")
                ?: doc.getString("website")
                ?: doc.getString("url")
                ?: ""

            val subjectsList = mutableListOf<SubjectRequirement>()
            val rawSubjects = doc.get("requiredSubjects") ?: doc.get("subjects")
            if (rawSubjects is List<*>) {
                for (item in rawSubjects) {
                    when (item) {
                        is Map<*, *> -> {
                            val subName = item["subjectName"]?.toString() ?: item["name"]?.toString() ?: ""
                            val minPct = when (val p = item["minPercentage"] ?: item["mark"] ?: item["percentage"]) {
                                is Number -> p.toInt()
                                is String -> p.toIntOrNull() ?: 50
                                else -> 50
                            }
                            if (subName.isNotEmpty()) {
                                subjectsList.add(SubjectRequirement(subName, minPct))
                            }
                        }
                        is String -> {
                            if (item.isNotEmpty()) {
                                subjectsList.add(SubjectRequirement(item, 50))
                            }
                        }
                    }
                }
            }

            return Course(
                id = docId,
                name = name,
                institution = institution,
                field = field,
                description = description,
                duration = duration,
                apsRequirement = aps,
                requiredSubjects = subjectsList,
                applicationInfo = appInfo,
                websiteUrl = website
            )
        }
    }
}

data class SubjectRequirement(
    val subjectName: String = "",
    val minPercentage: Int = 0
) : Serializable