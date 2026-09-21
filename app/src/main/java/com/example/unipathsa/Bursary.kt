package com.example.unipathsa

data class Bursary(
    var id: String = "",
    var name: String = "",
    var provider: String = "",
    var amount: String = "",
    var closingDate: String = "",
    var category: String = "" // e.g. "IT", "Commerce", "Engineering", "Law"
)
