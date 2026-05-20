package com.example.bibliounifornew.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // O endereço oficial da biblioteca do Google
    private const val BASE_URL = "https://www.googleapis.com/books/v1/"

    // A antena que o seu app vai usar para fazer as pesquisas
    val api: BooksApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Traduz os dados do Google para o Kotlin
            .build()
            .create(BooksApi::class.java)
    }
}