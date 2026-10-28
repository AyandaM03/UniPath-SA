package com.example.unipathsa

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import java.io.Serializable
import java.util.Locale

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

    // Backward-compatibility properties
    val university: String get() = this.institution
    val category: String get() = this.field
    val apsRequired: Int get() = this.apsRequirement
    val website: String get() = this.websiteUrl

    companion object {
        private const val TAG = "CourseParser"

        fun fromFirestore(doc: DocumentSnapshot): Course {
            val docId = doc.id
            val rawData = doc.data ?: emptyMap()

            // Build lowercase key lookup map
            val lowerMap = HashMap<String, Any?>()
            for ((key, value) in rawData) {
                lowerMap[key.lowercase(Locale.ROOT).trim()] = value
            }

            // Known name keys in order
            val nameKeys = listOf("name", "coursename", "title", "programmename", "degreename", "course", "programme")
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
                Log.w(TAG, "Course document missing name key! Doc ID: $docId, Keys: ${rawData.keys}")
                "Untitled course"
            } else {
                resolvedName
            }

            // Institution lookup
            val instKeys = listOf("institution", "university", "institutionname", "school", "provider")
            var resolvedInst = ""
            for (k in instKeys) {
                val valStr = lowerMap[k]?.toString()?.trim()
                if (!valStr.isNullOrBlank()) {
                    resolvedInst = valStr
                    break
                }
            }

            // Field / Faculty lookup
            val fieldKeys = listOf("field", "category", "faculty", "department")
            var resolvedField = "General"
            for (k in fieldKeys) {
                val valStr = lowerMap[k]?.toString()?.trim()
                if (!valStr.isNullOrBlank()) {
                    resolvedField = valStr
                    break
                }
            }

            // Description lookup
            val descKeys = listOf("description", "overview", "details", "summary")
            var resolvedDesc = ""
            for (k in descKeys) {
                val valStr = lowerMap[k]?.toString()?.trim()
                if (!valStr.isNullOrBlank()) {
                    resolvedDesc = valStr
                    break
                }
            }

            // Duration lookup
            val durKeys = listOf("duration", "period", "years")
            var resolvedDur = "3 Years"
            for (k in durKeys) {
                val valStr = lowerMap[k]?.toString()?.trim()
                if (!valStr.isNullOrBlank()) {
                    resolvedDur = valStr
                    break
                }
            }

            // APS requirement lookup
            val apsVal = lowerMap["apsrequirement"] ?: lowerMap["minaps"] ?: lowerMap["aps"] ?: lowerMap["apsrequired"]
            val aps = when (apsVal) {
                is Number -> apsVal.toInt()
                is String -> apsVal.toIntOrNull() ?: 0
                else -> 0
            }

            // Website URL lookup
            val webKeys = listOf("websiteurl", "website", "url", "link")
            var resolvedWeb = ""
            for (k in webKeys) {
                val valStr = lowerMap[k]?.toString()?.trim()
                if (!valStr.isNullOrBlank()) {
                    resolvedWeb = valStr
                    break
                }
            }

            // Required subjects list
            val subjectsList = mutableListOf<SubjectRequirement>()
            val rawSubjects = lowerMap["requiredsubjects"] ?: lowerMap["subjects"]
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
                name = finalName,
                institution = resolvedInst,
                field = resolvedField,
                description = resolvedDesc,
                duration = resolvedDur,
                apsRequirement = aps,
                requiredSubjects = subjectsList,
                applicationInfo = lowerMap["applicationinfo"]?.toString() ?: "",
                websiteUrl = resolvedWeb
            )
        }
    }
}

data class SubjectRequirement(
    val subjectName: String = "",
    val minPercentage: Int = 0
) : Serializable