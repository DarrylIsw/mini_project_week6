package com.example.mini_project_week6

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RestCountriesClient {
    val api: CountryApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://restcountries.com/") // must end with /
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CountryApi::class.java)
    }
}

