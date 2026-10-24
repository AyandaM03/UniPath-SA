package com.example.unipathsa

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.io.Serializable

data class ApplicationModel(
    val id: String = "",
    val university: String = "",
    val course: String = "",
    val status: String = "Submitted",
    val submittedAt: Timestamp? = null,
    val deadline: String = "",
    val docsUploaded: Int = 0,
    val docsTotal: Int = 4,
    val createdAt: Timestamp? = null,
    val notes: String = "",
    val requiredDocuments: List<String> = emptyList()
) : Serializable {

    // Backward-compatible property getter for institution
    val institution: String get() = this.university

    companion object {
        fun fromFirestore(doc: DocumentSnapshot): ApplicationModel {
            val docId = doc.id
            val university = doc.getString("university") ?: doc.getString("institution") ?: ""
            val course = doc.getString("course") ?: doc.getString("courseName") ?: ""
            val status = doc.getString("status") ?: "Submitted"
            val submittedAt = doc.getTimestamp("submittedAt")
            val deadline = doc.getString("deadline") ?: doc.getString("closingDate") ?: ""
            val docsUploaded = (doc.getLong("docsUploaded") ?: 0L).toInt()
            val docsTotal = (doc.getLong("docsTotal") ?: 4L).toInt()
            val createdAt = doc.getTimestamp("createdAt")
            val notes = doc.getString("notes") ?: ""
            val rawDocs = doc.get("requiredDocuments") as? List<*>
            val docs = rawDocs?.filterIsInstance<String>() ?: emptyList()

            return ApplicationModel(
                id = docId,
                university = university,
                course = course,
                status = status,
                submittedAt = submittedAt,
                deadline = deadline,
                docsUploaded = docsUploaded,
                docsTotal = docsTotal,
                createdAt = createdAt,
                notes = notes,
                requiredDocuments = docs
            )
        }
    }
}