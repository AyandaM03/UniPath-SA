package com.example.unipathsa

data class Course(
    var id: String = "",
    var name: String = "",
    var university: String = "",
    var apsRequired: Int = 0,
    var duration: String = "",
    var category: String = "" // e.g. "IT", "Commerce", "Engineering", "Law"
)
