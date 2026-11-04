package com.example.unipathsa

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.io.Serializable

data class DocumentModel(
    val id: String = "",
    val docType: String = "",
    val fileName: String = "",
    val fileType: String = "",
    val fileSize: Long = 0L,
    val storagePath: String = "",
    val fileUrl: String = "",
    val uploadedAt: Timestamp? = null,
    val userId: String = ""
) : Serializable {

    companion object {
        fun fromFirestore(doc: DocumentSnapshot): DocumentModel {
            val docId = doc.id
            val docType = doc.getString("docType") ?: docId
            val fileName = doc.getString("fileName") ?: ""
            val fileType = doc.getString("fileType") ?: ""
            val fileSize = doc.getLong("fileSize") ?: 0L
            val storagePath = doc.getString("storagePath") ?: ""
            val fileUrl = doc.getString("fileUrl") ?: ""
            val uploadedAt = doc.getTimestamp("uploadedAt")
            val userId = doc.getString("userId") ?: ""

            return DocumentModel(
                id = docId,
                docType = docType,
                fileName = fileName,
                fileType = fileType,
                fileSize = fileSize,
                storagePath = storagePath,
                fileUrl = fileUrl,
                uploadedAt = uploadedAt,
                userId = userId
            )
        }
    }
}