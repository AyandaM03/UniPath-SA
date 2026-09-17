package com.example.unipathsa

import retrofit2.Call
import retrofit2.http.GET

interface FirestoreApi {
    @GET("institutions")
    fun getInstitutions(): Call<FirestoreListResponse>

    @GET("courses")
    fun getCourses(): Call<FirestoreListResponse>
}