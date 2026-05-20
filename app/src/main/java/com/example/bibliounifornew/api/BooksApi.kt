package com.example.bibliounifornew.api

import com.example.bibliounifornew.model.BooksResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BooksApi {

    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("key") apiKey: String? = null
    ): BooksResponse
}