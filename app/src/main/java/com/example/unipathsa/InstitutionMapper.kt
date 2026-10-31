package com.example.unipathsa

// Turns a saved Room row back into the Institution your screens already use
fun InstitutionEntity.toInstitution() = Institution(
    id = id,
    name = name,
    type = type,
    province = province,
    description = description,
    website = website,
    imageUrl = imageUrl,
    heroImageUrl = heroImageUrl,
    freeToApply = freeToApply
)