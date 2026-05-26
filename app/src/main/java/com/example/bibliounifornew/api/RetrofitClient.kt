package com.example.bibliounifornew.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import com.example.bibliounifornew.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://www.googleapis.com/books/v1/"

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        // Colocamos a sua chave real direto aqui na variável
        val apiKey = "AIzaSyC8t_vTp_BNj82t6X1yWOX2dJkadMCT-1A"

        val url = originalUrl.newBuilder()
            .addQueryParameter("key", apiKey)
            .build()

        val request = originalRequest.newBuilder().url(url).build()

        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val api: BooksApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BooksApi::class.java)
    }
}

