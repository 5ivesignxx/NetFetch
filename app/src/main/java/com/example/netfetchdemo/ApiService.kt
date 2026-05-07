package com.example.netfetchdemo

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// ── 1. Define the API endpoints ──
interface TodoApiService {

    @GET("todos")
    suspend fun getTodos(): List<Todo>
}

// ── 2. Retrofit singleton ──
object RetrofitClient {

    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    val instance: TodoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TodoApiService::class.java)
    }
}
