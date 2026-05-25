package com.example.bibliounifornew.api

import com.example.bibliounifor.model.BooksResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BooksApi {

    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 10,
        @Query("key") apiKey: String
    ): BooksResponse
}