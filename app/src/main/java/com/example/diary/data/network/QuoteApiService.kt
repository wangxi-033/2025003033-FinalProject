package com.example.diary.data.network

import retrofit2.http.GET

interface QuoteApiService {
    @GET("random")
    suspend fun getRandomQuote(): List<QuoteDto>
}
