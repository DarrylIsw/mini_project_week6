package com.example.mini_project_week6

// Retrofit annotations for defining API endpoints and query parameters
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Retrofit interface to define API endpoints for fetching country data
interface CountryApi {

    // Makes a GET request to "https://restcountries.com/v3.1/name/{name}"
    @GET("v3.1/name/{name}")
    suspend fun getCountry(
        @Path("name") countryName: String,          // Dynamic path parameter -> the country name provided in the URL
        @Query("fields") fields: String =           // Query parameter -> specify which fields to fetch
            "name,capital,region,population,flags"  // Default fields returned by API if not overridden
    ): List<RestCountryResponse>                    // Returns a list of RestCountryResponse objects (parsed JSON response)
}
