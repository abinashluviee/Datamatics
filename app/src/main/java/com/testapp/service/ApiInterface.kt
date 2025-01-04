package com.testapp.service

import com.testapp.pojo.NewsHeadlinesPojo
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String,
        @Query("apiKey") apiKey: String): NewsHeadlinesPojo


}