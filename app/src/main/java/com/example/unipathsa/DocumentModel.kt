package com.example.unipathsa

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.io.Serializable

data class DocumentModel(
    val id: String = "",
    val docType: String = "",
    val fileName: String = "",
    val fileUrl: String = "",
    val uploadedAt: Timestamp? = null,
    val fileSize: Long = 0L
) : Serializable {

    companion object {
        fun fromFirestore(doc: DocumentSnapshot): DocumentModel {
            val docId = doc.id
            val docType = doc.getString("docType") ?: docId
            val fileName = doc.getString("fileName") ?: ""
            val fileUrl = doc.getString("fileUrl") ?: ""
            val uploadedAt = doc.getTimestamp("uploadedAt")
            val fileSize = doc.getLong("fileSize") ?: 0L

            return DocumentModel(
                id = docId,
                docType = docType,
                fileName = fileName,
                fileUrl = fileUrl,
                uploadedAt = uploadedAt,
                fileSize = fileSize
            )
        }
    }
}