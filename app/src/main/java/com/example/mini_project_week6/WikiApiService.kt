package com.example.mini_project_week6

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
interface WikiApiService {
    @GET("w/api.php")
    suspend fun searchArticles(
        @Query("action") action: String = "query",
        @Query("format") format: String = "json",
        @Query("list") list: String = "search",
        @Query("srsearch") search: String
    ): Response<WikiResponse>
}
