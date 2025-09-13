package com.example.mini_project_week6

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RestCountriesApi {

    // Search by country name
    @GET("v3.1/name/{name}")
    suspend fun getCountry(
        @Path("name") countryName: String,
        @Query("fields") fields: String = "name,capital,region,population,flags"
    ): List<RestCountryResponse>
}
