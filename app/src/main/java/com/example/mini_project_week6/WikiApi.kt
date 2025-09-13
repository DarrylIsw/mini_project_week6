package com.example.mini_project_week6

import retrofit2.http.GET
import retrofit2.http.Query

interface WikiApi {
    // Existing OpenSearch
    @GET("w/api.php?action=opensearch&format=json&limit=10")
    suspend fun searchDestinations(
        @Query("search") query: String
    ): List<Any>

    // New: Get pageprops for a title
    @GET("w/api.php?action=query&format=json&prop=pageprops")
    suspend fun getPageProps(
        @Query("titles") title: String
    ): WikiPagePropsResponse
}
