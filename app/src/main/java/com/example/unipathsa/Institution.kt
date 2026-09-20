package com.example.unipathsa

data class Institution(
    var id: String = "",
    var name: String = "",
    var province: String = "",
    var type: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var website: String = "",
    var freeToApply: Boolean = false
)
