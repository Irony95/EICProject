package com.example.eicproject

import retrofit2.Response
import retrofit2.http.GET

interface ReadingsService {
    @GET("/")
    suspend fun getReadings() : Response<String>
}