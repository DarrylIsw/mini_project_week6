package com.example.mini_project_week6

import retrofit2.http.GET
import retrofit2.http.Query

interface WikidataApi {
    @GET("w/api.php?action=wbgetentities&format=json")
    suspend fun getEntity(
        @Query("ids") id: String, // e.g. Q252
        @Query("props") props: String = "claims"
    ): WikidataResponse
}
