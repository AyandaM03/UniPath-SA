package com.example.unipathsa

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.io.Serializable

data class NotificationModel(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val createdAt: Timestamp? = null,
    val isRead: Boolean = false
) : Serializable {

    companion object {
        fun fromFirestore(doc: DocumentSnapshot): NotificationModel {
            val docId = doc.id
            val title = doc.getString("title") ?: "Notification"
            val message = doc.getString("message") ?: ""
            val createdAt = doc.getTimestamp("createdAt")
            val isRead = doc.getBoolean("isRead") ?: false

            return NotificationModel(
                id = docId,
                title = title,
                message = message,
                createdAt = createdAt,
                isRead = isRead
            )
        }
    }
}