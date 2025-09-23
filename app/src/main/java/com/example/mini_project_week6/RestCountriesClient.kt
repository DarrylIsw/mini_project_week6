package com.example.mini_project_week6

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton object → ensures only one instance of Retrofit client is created
object RestCountriesClient {

    // 🔹 Lazily initialized Retrofit API interface for REST Countries
    val api: CountryApi by lazy {
        Retrofit.Builder()
            // Base URL for REST Countries API (must end with "/")
            .baseUrl("https://restcountries.com/")

            // Gson converter → automatically converts JSON response into Kotlin objects
            .addConverterFactory(GsonConverterFactory.create())

            // Build Retrofit instance
            .build()

            // Create implementation of CountryApi interface
            .create(CountryApi::class.java)
    }
}
