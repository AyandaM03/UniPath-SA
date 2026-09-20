package com.example.unipathsa



// Firestore REST API wraps every value with a type key
data class FirestoreValue(
    val stringValue: String? = null,
    val booleanValue: Boolean? = null,
    val integerValue: String? = null // Firestore returns integers as strings in REST responses
)

data class FirestoreFields(
    val name: FirestoreValue? = null,
    val province: FirestoreValue? = null,
    val type: FirestoreValue? = null,
    val freeToApply: FirestoreValue? = null,
    val university: FirestoreValue? = null,
    val apsRequired: FirestoreValue? = null,
    val faculty: FirestoreValue? = null
)

data class FirestoreDocument(
    val name: String,
    val fields: FirestoreFields
)

data class FirestoreListResponse(
    val documents: List<FirestoreDocument>?
)

// Clean models your app actually uses




