package com.example.unipathsa

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "institutions")
data class InstitutionEntity(
    @PrimaryKey
    val id: String, // This is the Firestore doc ID like "21jcWOMzc2VSXM5FAvvR"
    val name: String = "",
    val description: String = "",
    val freeToApply: Boolean = false,
    val heroImageUrl: String = "",
    val imageUrl: String = "",
    val logoUrl: String = "",
    val province: String = "",
    val type: String = "",
    val website: String = ""
)