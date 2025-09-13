package com.example.mini_project_week6

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WikidataRetrofitClient {
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "TravelBucketListApp/1.0 (example@email.com)")
                .build()
            chain.proceed(request)
        }
        .build()

    private const val BASE_URL = "https://www.wikidata.org/"

    val instance: WikidataApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WikidataApi::class.java)
    }
}
