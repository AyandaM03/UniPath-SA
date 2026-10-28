package com.example.unipathsa

data class Institution(
    var id: String = "",
    var name: String = "",
    var province: String = "",
    var type: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var website: String = "",
    var freeToApply: Boolean = false,
    var heroImageUrl: String = "",
    var applicationFee: Int? = null,
    var applicationFeeNote: String = "",
    var applicationFeeUrl: String = "",
    var applicationFeeYear: String = "",
    var feeLastChecked: String = ""
) {
    fun toUniversity(): University {
        return University(
            id = id,
            name = name,
            location = if (province.isNotEmpty()) province else "South Africa",
            description = description,
            longDescription = description,
            province = province,
            type = if (type.isNotEmpty()) type else "University",
            freeToApply = freeToApply,
            websiteUrl = website,
            imageUrl = imageUrl,
            logoUrl = imageUrl,
            heroImageUrl = heroImageUrl,
            applicationFee = applicationFee,
            applicationFeeNote = applicationFeeNote,
            applicationFeeUrl = applicationFeeUrl,
            applicationFeeYear = applicationFeeYear,
            feeLastChecked = feeLastChecked
        )
    }
}