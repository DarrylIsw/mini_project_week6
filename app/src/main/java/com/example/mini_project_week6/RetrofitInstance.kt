package com.example.mini_project_week6

object RetrofitInstance {
    private const val BASE_URL = "https://en.wikipedia.org/"

    val api: WikiApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(WikiApiService::class.java)
    }
}
