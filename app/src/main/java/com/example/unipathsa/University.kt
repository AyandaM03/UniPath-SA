package com.example.unipathsa

import java.io.Serializable

data class University(
    val name: String,
    val location: String,
    val description: String,
    val longDescription: String,
    val programmes: String,
    val requirements: String,
    val applicationInfo: String,
    val importantInfo: String,
    val logoResId: Int
) : Serializable
