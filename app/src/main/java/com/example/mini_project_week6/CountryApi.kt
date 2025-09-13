package com.example.mini_project_week6

import retrofit2.http.GET
import retrofit2.http.Path

interface CountryApi {
    @GET("v3.1/name/{name}")
    suspend fun getCountry(@Path("name") name: String): List<CountryResponse>
}

data class CountryResponse(
    val name: Name,
    val flags: Flags
)
data class Name(val common: String)
data class Flags(val png: String)

